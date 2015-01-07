package it.dtk.actornlp

import java.util.concurrent.Executor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import com.mongodb.casbah.MongoCursorBase
import com.typesafe.config.ConfigFactory
import it.dtk.nlp.db.{DBManager, MongoDBMapper, News}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Receptionist {

  case object Start

  case object IndexNotAnalyzed

  private case object IndexBatch

  case class Finished(count: Int)

  def props = Props(classOf[Receptionist])
}

/**
 * @author fabiofumarola
 *
 */
class Receptionist extends Actor with ActorLogging {

  import it.dtk.actornlp.Receptionist._
  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  val conf = context.system.settings.config
  val dbHost = conf.getString("akka.nlp.dbHost")
  val batchNewsSize = conf.getInt("akka.nlp.batch.size")
  val waitTime = conf.getLong("akka.nlp.wait.call")
  val time = conf.getLong("akka.nlp.wait.timeout")
  val timeout = time.seconds
  val dbManager = new DBManager(dbHost)
  context.setReceiveTimeout(timeout)

  var countToProcess = 0
  var countProcessed = 0
  var countProcessing = 0
  var receiveTimeout = false

  val controllerActor = context.actorOf(Controller.props(), "controller")
  var geoNewsIterator = Option.empty[MongoCursorBase]

  def receive = {

    case Start =>
      receiveTimeout = false
      geoNewsIterator = Option(dbManager.geoNewsIterator(batchNewsSize))
      countToProcess = geoNewsIterator.get.count()
      countProcessed = 0
      countProcessing = 0
      log.info("start processing {} news", countToProcess)
      self ! IndexBatch

    case IndexNotAnalyzed =>
      geoNewsIterator = Option(dbManager.geoNewsNotAnalyzedIterator(batchNewsSize))
      countToProcess = geoNewsIterator.get.count()
      countProcessed = 0
      countProcessing = 0
      log.info("start processing {} news", countToProcess)
      self ! IndexBatch

    case IndexBatch =>
      log.info("processing from {} to {} news from db of {}", countProcessed, countProcessed + batchNewsSize, countToProcess)
      var nextCall: Long = 0

      1 to batchNewsSize foreach { i =>
        geoNewsIterator.foreach{ cursor =>
          if (cursor.hasNext) {
            val news: News = MongoDBMapper.dBOToNews(geoNewsIterator.get.next())

            if (dbManager.findNlpNews(news.id).isEmpty) {
              context.system.scheduler.scheduleOnce(nextCall.seconds, controllerActor, Controller.Process(news))
              countProcessing += 1
              nextCall += waitTime
              log.debug("processing news with id {}", news.id)
            } else {
              dbManager.setGeoNewsAnalyzed(news)
              countProcessed += 1
              log.debug("news already analyzed {}", news.id)
            }
          }
        }
      }

      if (countProcessing == 0) {
        if (geoNewsIterator.isEmpty)
          self ! Finished(countProcessed)
        else {
          if (geoNewsIterator.get.hasNext)
            self ! IndexBatch
          else
            self ! Finished(countProcessed)
        }
      }

    case Controller.Processed(news) =>
      try {
        dbManager.setGeoNewsAnalyzed(news)
        shouldProcess()
        val r = dbManager.saveNlpNews(news)
        if (r == 0)
          log.error("error saving news with id {}", news.id)
        log.info("succesfully saved news with id {} and title {}", news.id, news.title)
      } catch {
        case ex: Throwable =>
          log.error("failed saving news with id {} with exception {}", news.id, ex.getStackTrace().mkString(" %% "))
      }

    case Controller.FailProcess(newsId, ex) =>
      log.error("fail process news with id {} with exception {}", newsId, ex.getStackTrace().mkString(" %% "))
      shouldProcess()

    case ReceiveTimeout =>
      log.error(s"timeout from text pro actor, with running actors {}", countProcessing)
      receiveTimeout = true
      shouldProcess()

    case Finished(processed) =>
      log.info("processed {} news", processed)
      //start processing only notAnalyzed every time it finishes after one hour
      log.info("scheduling the next iteration at {}", DateTime.now().plus(1.hour.toMillis))
      context.system.scheduler.scheduleOnce(1.hour, self, IndexNotAnalyzed)
  }

  def shouldProcess(): Unit = {
    countProcessing -= 1
    countProcessed += 1
    if (countProcessing <= Math.round(batchNewsSize / 4))
      self ! IndexBatch
  }
}
