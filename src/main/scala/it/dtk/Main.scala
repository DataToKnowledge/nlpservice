package it.dtk

import it.dtk.nlp.db.DBManager
import it.dtk.nlp.{ TreeTagger, TextPreprocessor }
import it.dtk.nlp.detector.{ CityDetector, DateDetector }
import it.dtk.nlp.TextProClient
import scala.util._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

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

    /*
    Using TreeTagger
    val sentences = news.map(n => TextPreprocessor.apply(n.text.get)).map(_.map(TreeTagger.apply))
    sentences.map(_.map(s => CityDetector.detect(DateDetector.detect(s))))
    */

    val textProClient = new TextProClient

    for (n <- news) {
      val futureResult = textProClient.process(n.text.get)
      futureResult.onComplete {
        case Success((tags, sents)) =>
          println(tags)
          val values = sents.get
          val entities = for {
            s <- values
            w <- s.words
            if w.iobEntity.nonEmpty
          } yield Option(w)
          
          entities.foreach(optWord => println(optWord.get))
        case Failure(ex) =>
          ex.printStackTrace()
      }
    }

  }

}
