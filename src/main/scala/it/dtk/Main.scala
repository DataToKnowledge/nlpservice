package it.dtk

import it.dtk.nlp.db.DBManager
import it.dtk.nlp.{ TreeTagger, TextPreprocessor }
import it.dtk.nlp.detector.{ CityDetector, DateDetector }
import it.dtk.nlp.TextProClient
import scala.util._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import it.dtk.nlp.db.News
import scala.concurrent.Future
import it.dtk.nlp.detector.CrimeDetector
import it.dtk.nlp.detector.AddressDetector
import scala.concurrent.Await
import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.actor.NlpController
import akka.pattern._
import akka.actor.ActorSystem
import akka.actor.Props

object NlpReceptionist {
  case object Start
  case object Pause
  case object Stop
  case class Finished(processedNews: Int)

  def props = Props[NlpReceptionist]
}

class NlpReceptionist extends Actor with ActorLogging {

  import NlpReceptionist._

  //db configurations
  def host = "10.1.0.62"
  def db = "dbNews"

  var processed = 0

  val nlpControllerActor = context.actorOf(NlpController.props)

  val newsIterator = DBManager.iterateOverNews(10)

  def receive = {
    case Start =>
      //process the news 10 by 10
      val newsSeq = newsIterator.next

      if (newsSeq.isEmpty)
        context.parent ! Finished(processed)
      else
        nlpControllerActor ! NlpController.Process(newsSeq)

    case NlpController.Processed(newsSeq) =>
      //save the news in the db collection nlpNews
      newsSeq.foreach(DBManager.saveNlpNews(_))
      processed += newsSeq.size
      //process the next ten news
      self ! Start

    case NlpController.FailedProcess(news, ex) =>
      //save a reference to the news and the error in a log file
      val stacktraceString = ex.getStackTrace().map(_.toString()).mkString(" ")
      log.error("error for the news with id {} title {} and stacktrace", news.id,
        news.title.getOrElse("no title"), stacktraceString)

    case Pause =>

    case Stop =>
  }
}

/**
 * Author: DataToKnowledge S.r.l.s.
 * Project: NLPService
 * Date: 20/03/14
 */
object Main {

//  private val executorService = Executors.newCachedThreadPool()
//  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

  def main(args: Array[String]) {
    
    //Use the system's dispatcher as ExecutionContext
    import system.dispatcher

    val system = ActorSystem("NewsExtractor")
    val receptionist = system.actorOf(NlpReceptionist.props, "NLPReceptionist")
    
    receptionist ! NlpReceptionist.Start
    
  }

}
