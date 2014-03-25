package it.dtk.nlp

object TreeTaggerSpec {

  val words = Array("Maxi", "blitz", "contro", "i", "trafficanti", "di", "droga", "nel", "Tarantino",
    "in", "manette", "finisce", "anche", "la", "convivente", "dello", "zio", "dei", "due", "fratellini",
    "scampati", "all'agguato", ".")

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TreeTaggerSpec extends BaseTestClass {

  import TreeTaggerSpec._

  "A TreeTagger" when {

    val treeTagger = new TreeTagger()

    "tags a sequence of words" should {

      "return the posTag for each word" in {

        val results = words.map(treeTagger.tag)

        results.foreach(
          _.posTag shouldBe a [Some[String]]
        )
      }
    }
  }

}
