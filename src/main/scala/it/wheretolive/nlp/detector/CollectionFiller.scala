package it.wheretolive.nlp.pipeline.detector

import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.detector.StringUtils

import scala.annotation.tailrec

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait CollectionFiller extends StringUtils {

  def fillPersons(words: Seq[Word]) = fill(words, "PER").toList

  def fillGPEs(words: Seq[Word]) = fill(words, "GPE").toList

  def fillOrganizations(words: Seq[Word]) = fill(words, "ORG").toList

  def fillLocations(words: Seq[Word]) = fill(words, "LOC").toList

  def fillCrimes(words: Seq[Word]) = fill(words, "CRIME").toList

  /**
   *
   * @param words
   * @param eType
   * @return the entities as string of lemmas
   */
  private def fill(words: Seq[Word], eType: String) = {
    val filtered = words.filter(_.iobEntity.contains(eType))

    mergeIOB(filtered, "B-" + eType).map(standardiseName)
  }

  def mergeIOB(words: Seq[Word], entityType: String): Set[String] = {

      @tailrec
      def mergeIOB0(words: Seq[Word], entity: String, acc: Set[String]): Set[String] = {
        if (words.isEmpty)
          acc ++ List(entity)
        else if (words.head.iobEntity.contains(entityType))
          mergeIOB0(words.tail, words.head.lemma, acc + entity)
        else
          mergeIOB0(words.tail, entity + " " + words.head.lemma, acc)

      }
    if (words.isEmpty)
      Set.empty
    else
      mergeIOB0(words.tail,words.head.lemma, Set())
  }
}
