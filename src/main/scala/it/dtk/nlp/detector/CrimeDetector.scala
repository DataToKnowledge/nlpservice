package it.dtk.nlp.detector

import it.dtk.nlp.db.DBManager
import it.dtk.nlp.db.Crime
import scala.util.Try
import it.dtk.nlp.db.Word
import EntityType._
import scala.collection.immutable.TreeMap

class CrimeDetector {

  val range = 3

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

        DBManager.findCrime(candidate).foreach { city =>
          for (j <- 0 until slide.size) {
            val word = if (j == 0)
              tag(slide, j, EntityType.B_CRIME)
            else
              tag(slide, j, EntityType.I_CRIME)

            if (word.isDefined && !taggedTokenId.contains(word.get.tokenId.get)) {
              mapWords += (word.get.tokenId.get -> word.get)
              taggedTokenId += word.get.tokenId.get
            }
          }
        }
      }
    }
    //return the sequence of the words where some words are annotated with entity
    TreeMap(mapWords.toArray: _*).values.toIndexedSeq
  }

 
  //TODO remove if it not needed
  //  /**
  //   * load crime dictionary from file
  //   */
  //  private def downloadCrimeDictionary(): Unit = {
  //    val dictionary = getClass().getResource("/CrimeDictionary").getPath()
  //    val lines = scala.io.Source.fromFile(dictionary).getLines.drop(0).map(_.split(","))
  //
  //    lines.foreach { a =>
  //      (a(0).trim() :: crimeWords)
  //    }
  //
  //  }

}