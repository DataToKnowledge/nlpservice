package it.dtk

import it.dtk.nlp.db.DBManager
import it.dtk.nlp.{TreeTagger, TextPreprocessor}
import it.dtk.nlp.detector.{CityDetector, DateDetector}

/**
 * Author: DataToKnowledge S.r.l.s.
 * Project: NLPService
 * Date: 20/03/14
 */
object Main {

  def main(args: Array[String]) {

    val news = DBManager.getNews(5)

    val sentences = news.map(n => TextPreprocessor.apply(n.text.get)).map(_.map(TreeTagger.apply))

    sentences.map(_.map(s => CityDetector.detect(DateDetector.detect(s))))
  }

}
