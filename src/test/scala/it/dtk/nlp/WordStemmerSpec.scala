package it.dtk.nlp

object WordStemmerSpec {

  val words = Vector(
    ("rapinare", "rapin"),
    ("rubare", "rub"),
    ("aggredire", "aggred"),
    ("assassinare", "assassin"),
    ("stuprare", "stupr")
  )

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class WordStemmerSpec extends BaseTestClass {

  import WordStemmerSpec._

  "A WordStemmer" when {

    val stemmer = new WordStemmer

    "stems a token" should {

      words.foreach {
        w =>
          s"return the corrent stem for '${w._1}'" in {
            stemmer.stem(w._1) should be(w._2)
          }
      }

    }

  }

}
