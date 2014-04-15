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

object NlpReceptionist {
  case object Start
  case object Pause
  case object Stop
}

class NlpReceptionist extends Actor with ActorLogging {

  import NlpReceptionist._

  //db configurations
  def host = "10.1.0.62"
  def db = "dbNews"

  val nlpControllerActor = context.actorOf(NlpController.props)

  val newsIterator = DBManager.iterateOverNews(10)

  def receive = {
    case Start =>
      //process the news 10 by 10
      val newsSeq = newsIterator.next
      nlpControllerActor ! NlpController.Process(newsSeq)
      
    case NlpController.Processed(newsSeq) =>
    //save the news in the db collection nlpNews
    newsSeq.foreach(DBManager.saveNlpNews(_))

    case NlpController.FailedProcess(news, ex) =>
    //save a reference to the news and the error in a log file
    val stacktraceString = ex.getStackTrace().map(_.toString()).mkString(" ")
    log.error("error for the news with id {} title {} and stacktrace",news.id, 
        news.title.getOrElse("no title"), stacktraceString)
  }
}

/**
 * Author: DataToKnowledge S.r.l.s.
 * Project: NLPService
 * Date: 20/03/14
 */
object Main {

  private val executorService = Executors.newCachedThreadPool()
  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

  def main(args: Array[String]) {

    val news = DBManager.getNews(5)

    val textProClient = new TextProClient

    for (n <- news) {

      //title summary text
      val nlpTitle = textProClient.process(n.title)
      val nlpSummary = textProClient.process(n.summary)
      val nlpText = textProClient.process(n.text)

      //call city, date and crime detector
      val pipeline = nlpText.map { keysSents =>
        val sentences = keysSents._2
        val citySentences = sentences.map(CityDetector.detect)
        val crimeSentences = citySentences.map(CrimeDetector.detect)
        val dateSentences = crimeSentences.map(DateDetector.detect)
        val addressSentences = dateSentences.map(AddressDetector.detect)
        keysSents._1 -> addressSentences
      }

      pipeline.onComplete {
        case Success((tags, sents)) =>
          val entities = for {
            s <- sents
            w <- s.words
            //            if w.iobEntity.nonEmpty
          } yield w

          println("\n" + n.text.get)
          println(tags)
          val strEntities = entities.map { w =>
            if (w.iobEntity.nonEmpty)
              w.token + " / " + w.iobEntity.mkString(" ")
            else w.token
          }
          println(strEntities.mkString(" "))
        case Failure(ex) =>
          println("\n" + n.nlpText.getOrElse("No Text!!!"))
        //ex.printStackTrace()
      }
    }

    //    //Using TreeTagger
    //    val sentences = news.map(n => TextPreprocessor(n.text.get)).map(_.map(TreeTagger.apply))
    //    //sentences.map(_.map(s => CityDetector.detect(DateDetector.detect(s))))
    //
    //    for (n <- news) {
    //      val futureResult = textProClient.process(n.text.get)
    //      futureResult.onComplete {
    //        case Success((tags, sents)) =>
    //          println(tags)
    //          val entities = for {
    //            s <- sents
    //            w <- s.words
    //            if w.iobEntity.nonEmpty
    //          } yield w
    //
    //          entities.foreach(println)
    //        case Failure(ex) =>
    //          ex.printStackTrace()
    //      }
    //    }

  }

}
