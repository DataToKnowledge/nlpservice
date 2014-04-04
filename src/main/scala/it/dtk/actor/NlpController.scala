package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.News
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext

object NlpController {

  case class Process(news: Seq[News])
  case class Processed(news: Seq[News])
  case class FailedProcess(news: News)
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

  val callInterval = 10 seconds

  def receive = waiting

  val waiting: Receive = {
    case Process(newsSeq) =>
      log.debug("start processing {} news", newsSeq.length)
      context.become(runNext(newsSeq, sender))
  }

  def runNext(newsSeq: Seq[News], send: ActorRef): Receive = {
    val setIds = newsSeq.map(_.id).toSet
    val mapNews = newsSeq.foldLeft(Map.empty[String, News])((map, news) => map + (news.id -> news))
    val send = sender
    //start pipeline
    //TODO now we use the sceduler, but we can define also a consumer policy
    newsSeq.foreach { n =>
      val textProActor = context.actorOf(TextProActor.props, "textPro_id" + n.id)
      var interval = callInterval
      n.title.map { title =>
        context.system.scheduler.scheduleOnce(interval, textProActor, Parse(n.id, title, Title))
        interval += callInterval
      }
      n.summary.map { summary =>
        context.system.scheduler.scheduleOnce(interval, textProActor, Parse(n.id, summary, Summary))
        interval += callInterval
      }

      n.summary.map { text =>
        context.system.scheduler.scheduleOnce(interval, textProActor, Parse(n.id, text, Corpus))
        interval += callInterval
      }

      //nlpActor ! 
    }
    running(setIds, mapNews, send)
  }

  def running(setIds: Set[String], mapNews: Map[String, News], send: ActorRef): Receive = {

    case TextProActor.Result(newsId, sentences, keywords, Title) =>

    case TextProActor.Result(newsId, sentences, keywords, Summary) =>

    case TextProActor.Result(newsId, sentences, keywords, Corpus) =>

    case TextProActor.Fail(newsId, text, newsPart) =>
    //TODO reschedule the message

  }

}