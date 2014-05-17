package it.dtk.actornlp

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import it.dtk.nlp.db.DBManager
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.actor.ReceiveTimeout
import it.dtk.nlp.db.News

object Receptionist {

  case object Start
  case object Next
  def props = Props(classOf[Receptionist])
}

/**
 * @author fabiofumarola
 *
 */
class Receptionist extends Actor with ActorLogging {
  import Receptionist._
  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  val conf = ConfigFactory.load("nlp");
  val dbHost = conf.getString("nlp.dbHost")
  val batchNewsSize = conf.getInt("nlp.batch.size")
  val waitTime = conf.getLong("nlp.wait.call")
  val time = conf.getLong("nlp.wait.timeout")
  val timeout = time.seconds
  context.setReceiveTimeout(timeout)

  def db = "dbNews"

  var countProcessing = 0

  val controllerActor = context.actorOf(Controller.props(), "controller")

  DBManager.dbHost = dbHost
  val newsIterator = DBManager.iterateOverNews(batchNewsSize)

  var nextBatch = IndexedSeq.empty[News]

  def receive = {
    case Start =>
      log.info("processing {} news from db {}", batchNewsSize, dbHost)
      var nextCall: Long = 0

      val newsSeq = newsIterator.next
      newsSeq.foreach { n =>
        nextCall += waitTime
        context.system.scheduler.scheduleOnce(nextCall.seconds, controllerActor, Controller.Process(n))
        countProcessing += 1
      }

    case Next =>
      nextBatch = if (nextBatch.isEmpty) newsIterator.next else nextBatch

      context.system.scheduler.scheduleOnce(waitTime.seconds, controllerActor, Controller.Process(nextBatch.head))
      countProcessing += 1
      nextBatch = nextBatch.tail

    case Controller.Processed(news) =>
      try {
        val r = DBManager.saveNlpNews(news)
        if (r == 0)
          log.error("error saving news with id {}",news.id)
        log.info("succesfully saved news with id {} and title", news.id, news.title)
        shouldIProcess()
      } catch {
        case ex: Throwable =>
          log.error("failed saving news with id {}", news.id)
          ex.printStackTrace()
      }

    case Controller.FailProcess(newsId, ex) =>
      log.error("fail process news with id {} with exception {}", newsId, ex.getStackTrace().mkString("  "))
      ex.printStackTrace()

    case ReceiveTimeout =>
      log.error("timeout from text pro actor")
      shouldIProcess()
  }

  def shouldIProcess(): Unit = {
    countProcessing -= 1
    if (countProcessing == 0)
      self ! Start
    else
      self ! Next
  }
}