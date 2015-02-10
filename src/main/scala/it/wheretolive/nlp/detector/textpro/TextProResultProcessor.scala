package it.wheretolive.nlp.detector.textpro

import it.wheretolive.nlp.Model._

import scala.util.{Failure, Success, Try}

case class KeywordsWords(keywords: Try[Map[String, Double]], words: Try[Seq[Word]])

object TextProResultProcessor {

  def parseText(lines: List[String]): Try[KeywordsWords] = {

    lines match {
      //split the list base on the lines
      case fileName :: keys :: fieldsHeader :: fields =>

        Success(
          KeywordsWords(
            keywords = extractKeywords(keys),
            words = extractWords(fieldsHeader, fields)
          )
        )

      case fileName :: fieldsHeader :: fields =>
        Success(
          KeywordsWords(
              keywords = Success(Map.empty[String,Double]),
              words = extractWords(fieldsHeader,fields)
          )
        )

      case _ =>
        Failure(new Throwable("TextPro does not output any result"))
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
    keyValuePair.toMap
  }

  private def extractWords(header: String, lines: List[String]): Try[Seq[Word]] = Try {

    val featuresSize = header.split(":")(1).trim.split("\t").size

    lines.map(_.split("\t")).
      filter(_.size == featuresSize).map { split =>
        Word(
          token = split(0),
          tokenId = split(1).toInt,
          tokenStart = split(2).toInt,
          tokenEnd = split(3).toInt,
          sentence = split(4),
          posTag = split(5),
          wordNetPos = split(6),
          lemma = split(7),
          iobEntity = split(8),
          chunk = split(9)
        )
      }
  }
}