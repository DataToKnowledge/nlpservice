package it.wheretolive.nlp.detector

import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.detector.textpro.{TextProExecutor, TextProResultProcessor}
import scala.util.Try

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait TextProNlpDetector {

  def textProPath: String

  val textProCaller = new TextProExecutor(textProPath)

  def process(news: CrawledNews): Try[(Nlp, Seq[Tag])] = {

    val title = textProCaller.tagText(news.title).
      flatMap(TextProResultProcessor.parseText)
      .flatMap(_.words).getOrElse(Seq[Word]())

    val summary = textProCaller.tagText(news.summary).
      flatMap(TextProResultProcessor.parseText)
      .flatMap(_.words).getOrElse(Seq[Word]())

    val description = textProCaller.tagText(news.metaDescription).
      flatMap(TextProResultProcessor.parseText)
      .flatMap(_.words).getOrElse(Seq[Word]())

    println("corpus " + news.corpus)
    val result = textProCaller.tagText(news.corpus)
    result.foreach(l => println(l.mkString(" \n ")))

    val corpusTry = result.flatMap(TextProResultProcessor.parseText)


    corpusTry.map { corpus =>
      val nlp = Nlp(
        title = title.toList,
        summary = summary.toList,
        corpus = corpus.words.getOrElse(Seq()).toList,
        description = description.toList)

      val tags = corpus.keywords.
        map(_.map(kv => Tag(kv._1, kv._2)).toSeq).
        getOrElse(Seq())

      (nlp, tags)
    }
  }
}
