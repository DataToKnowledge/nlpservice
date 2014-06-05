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
import akka.actor.ActorRef
import akka.actor.PoisonPill

object Receptionist {

  case object Start
  case object Next
  case class Finished(count: Int)
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
  var count = 0

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
        if (DBManager.findNlpNews(n.id).isEmpty) {
          context.system.scheduler.scheduleOnce(nextCall.seconds, controllerActor, Controller.Process(n))
          countProcessing += 1
          count += 1
        } else {
          log.info("skipping processed news with id {} and title {}", n.id, n.title)
        }
      }

      if (countProcessing == 0)
        self ! Start

      if (newsSeq.isEmpty) {
        log.info("Processed {} news", count)
        self ! PoisonPill
      }

    case Next =>

      nextBatch = if (nextBatch.isEmpty) newsIterator.next else nextBatch

      if (nextBatch.isEmpty) {
        log.info("Processed {} news", count)
        self ! PoisonPill
      } else {
        val h = nextBatch.head
        if (DBManager.findNlpNews(h.id).isEmpty) {
          context.system.scheduler.scheduleOnce(waitTime.seconds, controllerActor, Controller.Process(nextBatch.head))
          countProcessing += 1
          count += 1
          nextBatch = nextBatch.tail
        } else {
          log.info("skipping processed news with id {} and title {}", h.id, h.title)
        }

        if (countProcessing == 0)
          self ! Start
      }

    case Controller.Processed(news) =>
      try {
        val r = DBManager.saveNlpNews(news)
        if (r == 0)
          log.error("error saving news with id {}", news.id)
        log.info("succesfully saved news with id {} and title {}", news.id, news.title)
        shouldIProcess()
      } catch {
        case ex: Throwable =>
          log.error("failed saving news with id {}", news.id)
          ex.printStackTrace()
      }

    case Controller.FailProcess(newsId, ex) =>
      log.error("fail process news with id {} with exception {}", newsId, ex.getStackTrace().mkString("  "))
      shouldIProcess()
    //ex.printStackTrace()

    case ReceiveTimeout =>
      log.error(s"timeout from text pro actor, with running actors {}", countProcessing)
      shouldIProcess()
  }

  def shouldIProcess(): Unit = {
    countProcessing -= 1
    if (countProcessing == 0)
      self ! Start
    else {
      self ! Next
      self ! Next
    }

  }
}