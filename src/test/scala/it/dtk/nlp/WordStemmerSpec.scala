package it.dtk.nlp

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class WordStemmerSpec extends BaseTestClass {

  "A WordStemmer" when {

    val stemmer = new WordStemmer

    "stems a token" should {

      "return the stemmed token" in {
        val rapin = stemmer.stem("rapinare")

        assert(rapin === "rapin")
      }

    }

  }

}
