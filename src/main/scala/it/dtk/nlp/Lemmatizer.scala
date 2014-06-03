package it.dtk.nlp

import it.dtk.nlp.db.{Lemma, DBManager, Word}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class Lemmatizer {

  /**
   * Lemmatiza a given token using morph-it
   *
   * @param word input token
   * @return its lemma
   */
  def lemma(word: String): Option[String] = {
    DBManager.findLemma(word) match {
      case Some(lemma: Lemma) =>
        lemma.lemma
      case None =>
        None
    }
  }

  /**
   * Convenience method to lemmatize a Word
   *
   * @param word input token
   * @return its lemma
   */
  def lemma(word: Word): Word = {
    word.copy(lemma = lemma(word.token))
  }

}
