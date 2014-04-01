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
import it.dtk.nlp.detector.CrimeDetectorGVE

/**
 * Author: DataToKnowledge S.r.l.s.
 * Project: NLPService
 * Date: 20/03/14
 */
object Main {

  private val executorService = Executors.newCachedThreadPool()
  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

  def main(args: Array[String]) {

    val news = DBManager.getNews(10)

    val textProClient = new TextProClient

    for (n <- news) {

      //title summary text
      val nlpTitle = textProClient.process(n.title.get)
      val nlpSummary = textProClient.process(n.summary.get)
      val nlpText = textProClient.process(n.text.get)

      //call city, date and crime detector
      val pipeline = nlpTitle.map { keysSents =>
        val sentences = keysSents._2
        val citySentences = sentences.map(CityDetector.detect)
        val crimeSentences = citySentences.map(CrimeDetectorGVE.detect)
        val dateSentences = crimeSentences.map(DateDetector.detect)
        keysSents._1 -> dateSentences
      }

      pipeline.onComplete {
        case Success((tags, sents)) =>
          val entities = for {
            s <- sents
            w <- s.words
//            if w.iobEntity.nonEmpty
          } yield w

          println("\n" + n.title.get)
          println(tags)
          val strEntities = entities.map(w => w.token + " " + w.iobEntity)
          println(strEntities.mkString("\n"))
        case Failure(ex) =>
          println("\n" + n.title.get)
          ex.printStackTrace()
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
