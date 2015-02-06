package it.wheretolive.nlp.pipeline.detector

import it.wheretolive.nlp.Model.EntityType.EntityType
import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.db.CrimeMongoCollection

import scala.collection.immutable.TreeMap

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait NaiveCrimeDetector extends NERDetector with CrimeMongoCollection {

  val range = 3

  def detect(words: Seq[Word]): Seq[Word] = {
    //create a map of words ordered by tokenId
    var mapWords = words.map(w => w.tokenId -> w).toMap
    var taggedTokenId = Set.empty[Int]

      def tag(slice: Seq[Word], pos: Int, value: EntityType): Option[Word] = {
        val tokenId = slice(pos).tokenId
        mapWords.get(tokenId).map(w => w.copy(iobEntity = EntityType.stringValue(value)))
      }

    for (sizeNGram <- range to 1 by -1) {
      val sliding = words.sliding(sizeNGram)

      //all chunks of verbs of nouns
      for (slide <- sliding; if (slide.forall(w => w.chunk != EmptyEntity && w.iobEntity == EmptyEntity))) {
        val candidate = slide.map(_.token).mkString(" ")

        val result = findCrimeText(candidate).filter(c => c.name.split(" ").length == slide.length)

        if (result.nonEmpty) {

          val bType = if (result.head._type == "crime")
            EntityType.B_CRIME
          else EntityType.B_RELATED

          val iType = if (result.head._type == "crime")
            EntityType.I_CRIME
          else EntityType.I_RELATED

          for (j <- 0 until slide.size) {

            val word = if (j == 0)
              tag(slide, j, bType)
            else
              tag(slide, j, iType)

            if (word.isDefined && !taggedTokenId.contains(word.get.tokenId)) {
              mapWords += (word.get.tokenId -> word.get)
              taggedTokenId += word.get.tokenId
            }
          }
        }
      }
    }
    //return the sequence of the words where some words are annotated with entity
    TreeMap(mapWords.toArray: _*).values.toIndexedSeq
  }
}
