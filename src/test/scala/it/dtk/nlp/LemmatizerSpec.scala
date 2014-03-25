package it.dtk.nlp

object LemmatizerSpec {

  val words = Array("finisce", "scampati", "rivisto", "mangiato", "dormito")

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerSpec extends BaseTestClass {

  import LemmatizerSpec._

  "A Lemmatizer" when {

    val lemmatizer = new Lemmatizer("10.1.0.62", "morph")

    "lemmatize a word" should {

      "return the lemma for each word" in {

        val results = words.map(lemmatizer.lemma)

        results.foreach(
          _ shouldBe a [Some[String]]
        )
      }
    }
  }

}
