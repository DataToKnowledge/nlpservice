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
import it.dtk.nlp.db.NLPTitle
import it.dtk.nlp.db.NLPSummary
import it.dtk.nlp.db.NLPSummary
import it.dtk.nlp.db.NLPText

object NlpController {

  case class Process(news: Seq[News])
  case class Processed(news: Seq[News])
  case class FailedProcess(news: News)

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
      var interval = callInterval
      n.title.map { title =>
        context.system.scheduler.scheduleOnce(interval, textProRouter, Parse(n.id, title, Title))
        interval += callInterval
      }
      n.summary.map { summary =>
        context.system.scheduler.scheduleOnce(interval, textProRouter, Parse(n.id, summary, Summary))
        interval += callInterval
      }

      n.summary.map { text =>
        context.system.scheduler.scheduleOnce(interval, textProRouter, Parse(n.id, text, Corpus))
        interval += callInterval
      }
    }
    running(setIds, mapNews, send)
  }

  def running(setIds: Set[String], mapNews: Map[String, News], send: ActorRef): Receive = {

    case TextProActor.Result(newsId, sentences, keywords, Title) =>

      val news = mapNews(newsId)
      //add the tags
      val modTags = news.nlpTags.map(_ ++ keywords)
      //create the mod news with nlpTitle and updatedTags
      val modNews = news.copy(nlpTitle = Option(NLPTitle(sentences)), nlpTags = modTags)
      //update the map
      val modMap = mapNews + (modNews.id -> modNews)
      context.become(running(setIds, modMap, send))

    case TextProActor.Result(newsId, sentences, keywords, Summary) =>
      val news = mapNews(newsId)
      //add the tags
      val modTags = news.nlpTags.map(_ ++ keywords)
      //create the mod news with nlpTitle and updatedTags
      val modNews = news.copy(nlpSummary = Option(NLPSummary(sentences)), nlpTags = modTags)
      //update the map
      val modMap = mapNews + (modNews.id -> modNews)
      context.become(running(setIds, modMap, send))

    case TextProActor.Result(newsId, sentences, keywords, Corpus) =>
      val news = mapNews(newsId)
      //add the tags
      val modTags = news.nlpTags.map(_ ++ keywords)
      //create the mod news with nlpTitle and updatedTags
      val modNews = news.copy(nlpText = Option(NLPText(sentences)), nlpTags = modTags)
      //update the map
      val modMap = mapNews + (modNews.id -> modNews)
      val modSetIds = setIds - modNews.id
      
      
      context.become(running(modSetIds, modMap, send))

    case TextProActor.Fail(newsId, text, newsPart) =>
    //TODO reschedule the message

  }

  def postStop: Unit = {
    textProRouter ! PoisonPill
  }

}