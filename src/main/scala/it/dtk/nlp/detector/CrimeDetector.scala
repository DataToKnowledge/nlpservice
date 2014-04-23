package it.dtk.nlp.detector

import it.dtk.nlp.db.Word
import it.dtk.nlp.db.DBManager
import it.dtk.nlp.db.Crime
import scala.util.Try

object CrimeDetector extends Detector {

  //  var crimeWords: List[String] = List()
  //  var result = Vector.empty[Word]
  val offset = 3
  override def detect(sentence: Seq[Word]): Try[Seq[Word]] = Try {
    var result = Vector.empty[Word]
    var i = 0
    def setEntity(sentence: Seq[Word], start: Int, end: Int): Unit = {
      var currentWord = sentence.apply(start)
      result :+= currentWord.copy(iobEntity = currentWord.iobEntity + "B-CRIME")
      for (i <- (start + 1) to end) {
        currentWord = sentence.apply(i)
        result :+= currentWord.copy(iobEntity = currentWord.iobEntity + "I-CRIME")
      }
    }

    while (i < sentence.size) {
      val subSeq = sentence.slice(i, i + offset)
      var entityBool = false
      for (j <- offset to 1 by -1) {
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