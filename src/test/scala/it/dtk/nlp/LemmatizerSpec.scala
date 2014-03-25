package it.dtk.nlp

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerSpec extends BaseTestClass {

  "A Lemmatizer" when {

    val lemmatizer = new Lemmatizer("10.1.0.62", "morph")

    "lemmatize a word" should {

      "return the lemma for each word" in {
        val words = Array("finisce", "scampati", "rivisto", "mangiato", "dormito")

        val results = words.map(lemmatizer.lemma)

        results.foreach(
          _ shouldBe a [Some[String]]
        )
      }
    }
  }

}
