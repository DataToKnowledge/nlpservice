package it.wheretolive.nlp.pipeline.detector

import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.db._
import scala.annotation.tailrec
import scala.concurrent.Future

/**
 *
 * 1. Per ogni location mi serve GFossEntry, List[StartPosition] in titolo summary e corpus
 *
 * una funzione che prende in input le gfoss entry e mi filtra
 */

/**
 * Created by fabiofumarola on 12/01/15.
 */
trait FocusLocationDetector extends GeodataGFossIndex {

  case class LocationInfo(
    location: String,
    locationEntry: Option[Location] = Option.empty,
    locationScore: Double = 0,
    titleScore: Double = 0,
    summaryScore: Double = 0,
    corpusScore: Double = 0,
    totalScore: Double = 0)

  private def containsEntity(element: String) =
    element.contains("LOC") || element.contains("GPE")

  private def containsEntityB(element: String) =
    element.contains("B-LOC") || element.contains("B-GPE")

  /**
   * main method to detect the main location in the news
   * @param nlp
   * @return returns a list of LocationInfo sorted in reverse order of the total score
   */
  def detect(nlp: Nlp): List[LocationInfo] = {

    val totalWordsCount = nlp.title.size + nlp.summary.size + nlp.corpus.size

    // extract all the tokenStart value for the selected locations
    val titleLocations = tokenStartFromLocations(nlp.title.toList.filter(w => containsEntity(w.iobEntity) || containsEntityB(w.iobEntity)))
    val summaryLocations = tokenStartFromLocations(nlp.summary.toList.filter(w => containsEntity(w.iobEntity) || containsEntityB(w.iobEntity)))
    val corpusLocations = tokenStartFromLocations(nlp.corpus.toList.filter(w => containsEntity(w.iobEntity) || containsEntityB(w.iobEntity)))

    //compute the position scores for each discovered location
    val titlePosW = positionsScore(titleLocations, totalWordsCount)
    val summaryPosW = positionsScore(summaryLocations, totalWordsCount)
    val corpusPosW = positionsScore(corpusLocations, totalWordsCount)

    //create the initial locationinfos Map tath will be filled with other scores
    val locationInfoMap = merge(titlePosW, summaryPosW, corpusPosW)

    //extract gfoss data for each location
    val locationInfos = locationInfoMap.map { locInfo =>
      //do request and in case of error return the original value
      val entry = try {
        searchLocation(locInfo.location).headOption
      }
      catch {
        case ex: Throwable =>
          None
      }
      locInfo.copy(locationEntry = entry)
    }

    //compute location scores
    val finalScores = computeLocationScores(locationInfos)

    //compute total score and in reverse order
    finalScores.
      map(s => s.copy(totalScore = s.titleScore + s.summaryScore + s.corpusScore + s.locationScore)).
      sortWith(_.totalScore > _.totalScore)
  }

  private def computeLocationScores(locationsInfo: List[LocationInfo]): List[LocationInfo] = {

    val defined = locationsInfo.filter(_.locationEntry.isDefined)
    val filtered = mapReduce(defined)_
    val regionMap = filtered(e => e.locationEntry.get.region_name -> 1).
      map(pair => pair._1 -> pair._2.toDouble / defined.size)
    val provinceMap = filtered(e => e.locationEntry.get.province_name -> 1).
      map(pair => pair._1 -> pair._2.toDouble / defined.size)

    locationsInfo.map { l =>
      val score = l.locationEntry.map { entry =>
        val rScore: Double = regionMap.getOrElse(entry.region_name, 0)
        val pScore: Double = provinceMap.getOrElse(entry.province_name, 0)

        (rScore + pScore) / 2
      }
      l.copy(locationScore = score.getOrElse(0D))
    }
  }

  private def mapReduce[T](list: List[T])(groupingFunc: T => (String, Int)): Map[String, Int] = {
    list.map(groupingFunc).
      groupBy(_._1).
      map(pair => pair._1 -> pair._2.length).toMap
  }

  /**
   *
   * @param titlePosW
   * @param summaryPosW
   * @param corpusPosW
   * @return merge position weights in location infos
   */
  def merge(titlePosW: Map[String, Double], summaryPosW: Map[String, Double], corpusPosW: Map[String, Double]): List[LocationInfo] = {

    val keys = titlePosW.keySet union summaryPosW.keySet union corpusPosW.keySet

    val infos = for {
      key <- keys
      titleW = titlePosW.get(key).getOrElse(0D)
      summW = summaryPosW.get(key).getOrElse(0D)
      corpW = corpusPosW.get(key).getOrElse(0D)
    } yield LocationInfo(key, None, titleW, summW, corpW)

    infos.toList
  }

  /**
   *
   * @param list
   * @param totalWords
   * @return return a map with location and the score for the position in the text
   */
  def positionsScore(list: List[(String, Int)], totalWords: Int): Map[String, Double] =
    list.groupBy(_._1).map {
      case (location, list) =>
        location -> list.map(e => (totalWords.toDouble - e._2) / totalWords).reduce(_ + _)
    }

  /**
   *
   * @param words
   * @return this method returns a list of token with their position in the text
   */
  def tokenStartFromLocations(words: List[Word]): List[(String, Int)] = {

      @tailrec
      def recursion(current: List[String], tokenId: Int, others: List[Word], acc: List[(String, Int)]): List[(String, Int)] = {
        others match {
          case head :: tail =>
            if (containsEntityB(head.iobEntity))
              recursion(List(head.token), head.tokenStart, tail, acc :+ (current.mkString(" "), tokenId))
            else
              recursion(current :+ head.token, tokenId, tail, acc)
          case Nil =>
            acc :+ (current.mkString(" "), tokenId)
        }
      }
    if (words.isEmpty)
      List.empty
    else recursion(List(words.head.token), words.head.tokenId, words.tail, List.empty)
  }

}
