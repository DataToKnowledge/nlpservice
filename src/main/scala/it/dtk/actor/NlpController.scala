package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.{ Word, News }
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.actor.Props
import akka.actor.PoisonPill
import it.dtk.nlp.detector._
import it.dtk.nlp.detector.NewsPart._

object NlpController {

  case class Process(news: Seq[News])
  case class Processed(news: Seq[News])
  case class FailedProcess(news: News, ex: Throwable)
  case class FailedProcessPart(idNews: String, part: NewsPart, ex: Throwable)

  def props = Props(classOf[NlpController])
}

class NlpController(textProHost: String) extends Actor with ActorLogging {

  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]

  import NlpController._
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
  val textProRouter = context.actorOf(TextProActor.routerProps(host = textProHost), "textProRouter")
  val tokenizerActor = context.actorOf(TokenizerActor.routerProps(), "tokenizerRouter")

  val callInterval = 20.seconds

  def receive = waiting

  val waiting: Receive = {
    case Process(newsSeq) =>
      log.info("start processing {} news", newsSeq.length)
      context.become(runNext(newsSeq, sender))
  }

  def runNext(newsSeq: Seq[News], send: ActorRef): Receive = {
    val mapNews = newsSeq.foldLeft(Map.empty[String, News])((map, news) => map + (news.id -> news))
    val send = sender

    newsSeq.foreach { n =>
      var interval = callInterval
      context.system.scheduler.scheduleOnce(interval, textProRouter, Parse(n))
      interval += callInterval
    }

    running(mapNews, 0, send)
  }

  def running(mapNews: Map[String, News], jobs: Int, send: ActorRef): Receive = {

    case TextProActor.Result(news) =>

      var j = jobs

      cityRouter ! new Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
      cityRouter ! new Detector.Process(news.id, news.nlpSummary.get, NewsPart.Summary)
      cityRouter ! new Detector.Process(news.id, news.nlpCorpus.get, NewsPart.Corpus)
      j += 3

      crimeRouter ! new Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
      crimeRouter ! new Detector.Process(news.id, news.nlpSummary.get, NewsPart.Summary)
      crimeRouter ! new Detector.Process(news.id, news.nlpCorpus.get, NewsPart.Corpus)
      j += 3

      dateRouter ! new Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
      dateRouter ! new Detector.Process(news.id, news.nlpSummary.get, NewsPart.Summary)
      dateRouter ! new Detector.Process(news.id, news.nlpCorpus.get, NewsPart.Corpus)
      j += 3

      addressRouter ! new Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
      addressRouter ! new Detector.Process(news.id, news.nlpSummary.get, NewsPart.Summary)
      addressRouter ! new Detector.Process(news.id, news.nlpCorpus.get, NewsPart.Corpus)
      j += 3

      //update the news and change the context
      val modMap = mapNews.updated(news.id, news)
      context.become(running(modMap, j, send))

    case TextProActor.Fail(news, ex) =>
      //TODO reschedule the message
      send ! FailedProcess(news, ex)

    case Detector.Result(newsId, sentences, NewsPart.Title) =>
      val n = mapNews.get(newsId).get
      val merge = mergeIOBEntity(n.nlpTitle.get, sentences)
      val modMap = mapNews + (newsId -> mapNews.get(newsId).get.copy(nlpTitle = Option(merge)))
      context.become(nextStatus(modMap, jobs - 1, send))

    case Detector.Result(newsId, sentences, NewsPart.Summary) =>
      val n = mapNews.get(newsId).get
      val merge = mergeIOBEntity(n.nlpSummary.get, sentences)
      val modMap = mapNews + (newsId -> mapNews.get(newsId).get.copy(nlpSummary = Option(merge)))
      context.become(nextStatus(modMap, jobs - 1, send))

    case Detector.Result(newsId, sentences, NewsPart.Corpus) =>
      val n = mapNews.get(newsId).get
      val merge = mergeIOBEntity(n.nlpCorpus.get, sentences)
      val modMap = mapNews + (newsId -> mapNews.get(newsId).get.copy(nlpCorpus = Option(merge)))
      context.become(nextStatus(modMap, jobs - 1, send))

    case Detector.Failure(newsId, part, ex) =>
      send ! FailedProcessPart(newsId, part, ex)
      context.become(nextStatus(mapNews, jobs - 1, send))
  }

  private def nextStatus(mapNews: Map[String, News], jobs: Int, send: ActorRef): Receive = {
    if (jobs == 0) {
      send ! Processed(mapNews.values.toSeq)
      waiting
    } else {
      running(mapNews, jobs, send)
    }
  }

  private def mergeIOBEntity(sentences: Seq[Word], annotated: Seq[Word]): Seq[Word] = {
    sentences.zip(annotated).map(w => w._1.copy(iobEntity = w._1.iobEntity ++ w._2.iobEntity))
  }

  override def postStop(): Unit = {
    textProRouter ! PoisonPill
  }

}