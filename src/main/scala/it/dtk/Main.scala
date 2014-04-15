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
      val nlpCorpus = textProClient.process(n.corpus)

      //call city, date and crime detector
      val pipeline = nlpCorpus.map { keysSents =>
        val sentences = keysSents._2
        val citySentences = CityDetector.detect(sentences)
        val crimeSentences = CrimeDetector.detect(sentences)
        val dateSentences = DateDetector.detect(sentences)
        val addressSentences = AddressDetector.detect(sentences)
        keysSents._1 -> addressSentences
      }
      
      for (i <- 1 to 4000){
        
      }

      pipeline.onComplete {
        case Success((tags, sents)) =>

          println("\n" + n.corpus.get)
          println(tags)
          val strEntities = sents.map{w =>
            if (w.iobEntity.nonEmpty)
            	w.token + " / " + w.iobEntity.mkString(" ")
            else w.token	
          }
          println(strEntities.mkString(" "))
        case Failure(ex) =>
          println("\n" + n.nlpCorpus.getOrElse("No Text!!!"))
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
