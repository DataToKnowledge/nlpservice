package it.dtk.nlp.detector

import it.dtk.nlp.db.{ City, DBManager, Word }
import org.slf4j.LoggerFactory
import scala.util.Try
import EntityType._
import scala.collection.immutable.TreeMap

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetector {

  /**
   * Maximum number of tokens for a city
   */
  val range = 4
  val step = 1

  /**
   * City name regular expression
   */
  private val CITIES_R = "^[A-Z](\\w+|\\')[\\w\\s\\']*"

  private val log = LoggerFactory.getLogger("CityDetector")

  def detect(words: Seq[Word]): Try[Seq[Word]] = Try {

    //create a map of words ordered by tokenId
    var mapWords = words.map(w => w.tokenId.get -> w).toMap
    var taggedTokenId = Set.empty[Int]

    def tag(slice: Seq[Word], pos: Int, value: EntityType): Option[Word] = {
      val tokenId = slice(pos).tokenId.get
      mapWords.get(tokenId).map(w => w.copy(iobEntity = w.iobEntity :+ EntityType.stringValue(value)))
    }

    for (sizeNGram <- range to 1 by -1) {
      val sliding = words.sliding(sizeNGram)

      for (slide <- sliding) {
        val candidate = slide.map(_.token).mkString(" ")
        if (candidate.matches(CITIES_R)) {
          DBManager.findCity(candidate).foreach { city =>
            for (j <- 0 until slide.size) {
              val word = if (j == 0)
                tag(slide, j, EntityType.B_CITY)
              else
                tag(slide, j, EntityType.I_CITY)

              if (word.isDefined && !taggedTokenId.contains(word.get.tokenId.get)) {
                mapWords += (word.get.tokenId.get -> word.get)
                taggedTokenId += word.get.tokenId.get
              }
            }
          }
        }
      }
    }
    //return the sequence of the words where some words are annotated with entity
    TreeMap(mapWords.toArray: _*).values.toIndexedSeq
  }
}
