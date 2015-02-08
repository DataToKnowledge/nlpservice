package it.wheretolive.nlp.pipeline.detector

import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.detector.StringUtils

import scala.annotation.tailrec

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait CollectionFiller extends StringUtils {

  def fillPersons(words: List[Word]) = fill(words, "PER")

  def fillGPEs(words: List[Word]) = fill(words, "GPE")

  def fillOrganizations(words: List[Word]) = fill(words, "ORG")

  def fillLocations(words: List[Word]) = fill(words, "LOC")

  def fillCrimes(words: List[Word]) = fill(words, "CRIME")

  def fillRelated(words: List[Word]) = fill(words, "RELATED")

  /**
   *
   * @param words
   * @param eType
   * @return the entities as string of lemmas
   */
  private def fill(words: List[Word], eType: String) = {
    val filtered = words.filter(_.iobEntity.contains(eType))

    mergeIOB(filtered, "B-" + eType).map(standardiseName)
  }

  def mergeIOB(words: List[Word], entityType: String): List[String] = {

      @tailrec
      def mergeIOB0(words: Seq[Word], entity: String, acc: List[String]): List[String] = {
        if (words.isEmpty)
          acc ++ List(entity)
        else if (words.head.iobEntity.contains(entityType))
          mergeIOB0(words.tail, words.head.lemma, acc :+ entity)
        else
          mergeIOB0(words.tail, entity + " " + words.head.lemma, acc)

      }
    if (words.isEmpty)
      List.empty
    else
      mergeIOB0(words.tail,words.head.lemma, List())
  }
}
