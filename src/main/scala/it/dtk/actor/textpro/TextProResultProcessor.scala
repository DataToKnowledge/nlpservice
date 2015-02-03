package it.dtk.actor.textpro

import scala.annotation.tailrec
import it.dtk.nlp.db.Word
import scala.util.Try
import scala.util.Success

case class KeywordsWordsPair(keywords: Map[String, Double], words: Seq[Try[Word]])

private object TextProResultProcessor {

  def parseText(text: String): Try[KeywordsWordsPair] = {

    //remove the first line that represents the header # FILE: input/prova.in
    val lines = text.split("\n")

    lines.toList match {
      //split the list base on the lines
      case fileName :: keys :: fieldsHeader :: fields =>
        for {
          keywords <- extractKeywords(keys)
          words <- extractWords(fields)
        } yield KeywordsWordsPair(keywords, words)

      case _ =>
        throw new Throwable("TextPro does not output any result")
    }
  }

  private def extractKeywords(text: String): Try[Map[String, Double]] = Try {

    val clean = text.split(":")(1).trim

    val split = clean.split(">")
    val keyValuePair = split.map(_.split("<")).map {
      array =>
        val key = array(0).trim
        val elemArray = array(1).trim.split(" ")
        key -> elemArray(0).toDouble
    }

    keyValuePair.foldLeft(Map.empty[String, Double])((map, elem) => map + elem)
  }

  private def extractWords(lines: List[String]): Try[Seq[Try[Word]]] = Try {

    /*
     * this is really hard coded
     * @param line
     * @return 
     */
    def parseLine(line: String): Try[Word] = Try {

      val split = line.trim.split("\t")




      split match {
        case split10 if split10.size == 10 =>
          val iobEntity = if (split(8).equals("O")) Vector.empty[String] else Vector(split(8))

          Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, iobEntity, Option(split(9)))

        case split8 if split8.size == 8 =>
          Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, Vector.empty[String], None)

        case _ =>
          throw new Throwable(s"wrong size ${split.size} for $line")
      }

    }
    lines.map(parseLine(_))
  }
}