package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.News
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.actor.Props
import akka.actor.PoisonPill

object NlpController {

  case class Process(news: Seq[News])
  case class Processed(news: Seq[News])
  case class FailedProcess(news: News, ex: Throwable)

  def props = Props(classOf[NlpController])
}

object NewsPart extends Enumeration {
  type NewsPart = Value
  val Title, Summary, Corpus = Value
}

class NlpController extends Actor with ActorLogging {

  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  import NlpController._
  import NewsPart._
  import TextProActor._

  /*
   * These are all the routers 
   */
  val addressRouter = context.actorOf(AddressDetectorActor.routerProps(), "addressRouter")
  val cityRouter = context.actorOf(CityDetectorActor.routerProps(), "cityRouter")
  val crimeRouter = context.actorOf(CrimeDetectorActor.routerProps(), "crimeRouter")
  val dateRouter = context.actorOf(DateDetectorActor.routerProps(), "dateRouter")
  val lemmatizerRouter = context.actorOf(LemmatizerActor.routerProps(), "lemmatizerRouter")
  val postagRouter = context.actorOf(PosTaggerActor.routerProps(), "postagRouter")
  val sentenceRouter = context.actorOf(SentenceDetectorActor.routerProps(), "sentenceDetectorRouter")
  val stemmerRouter = context.actorOf(StemmerActor.routerProps(), "stemmerRouter")
  val textProRouter = context.actorOf(TextProActor.routerProps(), "textProRouter")
  val tokenizerActor = context.actorOf(TokenizerActor.routerProps(), "tokenizerRouter")

  val callInterval = 20 seconds

  def receive = waiting

  val waiting: Receive = {
    case Process(newsSeq) =>
      log.debug("start processing {} news", newsSeq.length)
      context.become(runNext(newsSeq, sender))
  }

  def runNext(newsSeq: Seq[News], send: ActorRef): Receive = {
    val mapNewsProcessed = newsSeq.map(_.id).map(_ -> false).toMap
    val mapNews = newsSeq.foldLeft(Map.empty[String, News])((map, news) => map + (news.id -> news))
    val send = sender

    newsSeq.foreach { n =>
      var interval = callInterval
      context.system.scheduler.scheduleOnce(interval, textProRouter, Parse(n))
      interval += callInterval
    }
    running(mapNewsProcessed, mapNews, newsSeq.length, send)
  }

  def running(mapProcessed: Map[String, Boolean], mapNews: Map[String, News], counter: Int, send: ActorRef): Receive = {

    case TextProActor.Result(news) =>

      //update the maps
      val modMap = mapNews + (news.id -> news)
      val modNewsProcessed = mapProcessed + (news.id -> true)

      //TODO call the detectors
      
      val count = counter - 1

      val nextStatus = if (count == 0) {
        send ! Processed(mapNews.values.toSeq)
        waiting
      } else {
        running(modNewsProcessed, modMap, count, send)
      }

      context.become(nextStatus)

    case TextProActor.Fail(news, ex) =>
      //TODO reschedule the message
      val count = counter - 1
      send ! FailedProcess(news, ex)

      val nextStatus = if (count == 0) {
        send ! Processed(mapNews.values.toSeq)
        waiting
      } else {
        running(mapProcessed, mapNews, count, send)
      }
  }

  override def postStop: Unit = {
    textProRouter ! PoisonPill
  }

}