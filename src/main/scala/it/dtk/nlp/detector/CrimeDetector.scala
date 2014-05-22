package it.dtk.nlp.detector

import it.dtk.nlp.db.DBManager
import it.dtk.nlp.db.Crime
import scala.util.Try
import it.dtk.nlp.db.Word
import EntityType._
import scala.collection.immutable.TreeMap

object CrimeDetector {

  val range = 3

  def detect(words: IndexedSeq[Word]): Try[Seq[Word]] = Try {

    //create a map of words ordered by tokenId
    var mapWords = words.map(w => w.tokenId.get -> w).toMap
    var taggedTokenId = Set.empty[Int]

    def tag(slice: IndexedSeq[Word], pos: Int, value: EntityType): Option[Word] = {
      val tokenId = slice(pos).tokenId.get
      mapWords.get(tokenId).map(w => w.copy(iobEntity = w.iobEntity :+ value.toString()))
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

  def detect2(sentence: Seq[Word]): Try[Seq[Word]] = Try {
    var result = Vector.empty[Word]
    var i = 0
    def setEntity(sentence: Seq[Word], start: Int, end: Int): Unit = {
      var currentWord = sentence.apply(start)
      result :+= currentWord.copy(iobEntity = currentWord.iobEntity :+ "B-CRIME")
      for (i <- (start + 1) to end) {
        currentWord = sentence.apply(i)
        result :+= currentWord.copy(iobEntity = currentWord.iobEntity :+ "I-CRIME")
      }
    }

    while (i < sentence.size) {
      val subSeq = sentence.slice(i, i + range)
      var entityBool = false
      for (j <- range to 1 by -1) {
        val candidate = subSeq.slice(0, j)
        DBManager.findCrime(getString(candidate).trim) match {
          case Some(token: Crime) =>
            //setting crimeEntity only when the end is less then the sentence size
            val end = i + j - 1
            if (end < sentence.size) {
              setEntity(sentence, i, end)
              entityBool = true
              i = i + j
            }
          case None =>
            None

        }
      }
      //una volta calcolate tutte le possibili combinazioni si sceglie la migliore per quella slice
      if (!entityBool) {
        result :+= sentence(i)
        i = i + 1
      }

    }

    result.toSeq
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

  private def getString(list: Seq[Word]): String = {
    list.map(elem => elem.token.toLowerCase).mkString(" ")
  }

}