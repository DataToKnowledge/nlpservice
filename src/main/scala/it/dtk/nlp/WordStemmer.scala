package it.dtk.nlp

import org.tartarus.snowball.ext.italianStemmer
import org.tartarus.snowball.SnowballStemmer
import it.dtk.nlp.db.Word

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object WordStemmer {

  private val stemmer: SnowballStemmer = new italianStemmer

  /**
   * Returns the stem of the single token given in input
   *
   * @param token a SINGLE token
   * @return stem of token
   */
  def stem(token: String): String = {
    stemmer.setCurrent(token)
    stemmer.stem
    stemmer.getCurrent
  }

  /**
   * Convenience method for type Word
   *
   * @param word
   * @return
   */
  def stem(word: Word): Word = {
    word.copy(stem = Option(stem(word.token)))
  }

}
