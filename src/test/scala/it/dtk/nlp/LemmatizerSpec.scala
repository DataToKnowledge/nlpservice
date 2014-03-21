package it.dtk.nlp

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerSpec extends BaseTestClass {

  "A Lemmatizer" when {

    val lemmatizer = new Lemmatizer("10.1.0.62", "morph")

    "lemmatize a word" should {

      "return the lemma for each word" in {
        val words = Array("Maxi", "blitz", "contro", "i", "trafficanti", "di", "droga", "nel", "Tarantino",
          "in", "manette", "finisce", "anche", "la", "convivente", "dello", "zio", "dei", "due", "fratellini",
          "scampati", "all'agguato", ".")

        words.foreach(
          x => lemmatizer.getLemma(x).map(_ shouldBe a [Some[String]])
        )
      }
    }
  }

}
