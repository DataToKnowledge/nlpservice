package it.dtk.nlp.detector

import it.dtk.nlp.db.Word
import scala.util.Try

object NewsPart extends Enumeration {
  type NewsPart = Value
  val Title, Summary, Corpus = Value
}

object Detector {
  import NewsPart._
  case class Process(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Failure(newsId: String, part: NewsPart, ex: Throwable)
}

object EntityType extends Enumeration {
  type EntityType = Value
  val B_CITY, I_CITY, B_ADDRESS, I_ADDRESS, B_CRIME, I_CRIME, B_DATE, I_DATE = Value
}

/**
 * Entry point for detector classes
 *
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 *
 */
trait Detector {
  
  def detect(sentence: Seq[Word]): Try[Seq[Word]]

}
