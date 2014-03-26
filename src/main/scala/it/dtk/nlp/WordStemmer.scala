package it.dtk.nlp

import org.tartarus.snowball.ext.italianStemmer
import org.tartarus.snowball.SnowballStemmer

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object WordStemmer {

  val stemmer: SnowballStemmer = new italianStemmer

}

class WordStemmer {

  import WordStemmer._

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
    word.copy(stem = Some(stem(word.token)))
  }

}
