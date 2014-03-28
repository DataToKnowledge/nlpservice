package it.dtk.nlp

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class Lemmatizer(host: String, database: String) {

  val mongoClient = MongoClient(host, 27017)
  val db = mongoClient(database)
  val lemmas = db("morph-it")

  /**
   * Lemmatiza a given token using morph-it
   *
   * @param word input token
   * @return its lemma
   */
  def lemma(word: String): Option[String] = {
    lemmas.findOne(MongoDBObject( "word" -> word )) match {
      case Some(res) =>
        Option(res.get("lemma").asInstanceOf[String])
      case None =>
        None
    }
  }

  /**
   * Convenience method to lemmatize a Word
   *
   * @param word
   * @return
   */
  def lemma(word: Word): Word = {
    word.copy(lemma = lemma(word.token))
  }

}
