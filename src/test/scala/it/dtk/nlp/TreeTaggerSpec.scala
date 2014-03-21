package it.dtk.nlp

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TreeTaggerSpec extends BaseTestClass {

  "A TreeTagger" when {

    val treeTagger = new TreeTagger()

    "tags a sequence of words" should {

      "return the posTag for each word" in {
        val words = Array("Maxi", "blitz", "contro", "i", "trafficanti", "di", "droga", "nel", "Tarantino",
          "in", "manette", "finisce", "anche", "la", "convivente", "dello", "zio", "dei", "due", "fratellini",
          "scampati", "all'agguato", ".")

        val f = treeTagger.tag(words)

        f onComplete {
          case Success(results) =>
            results.foreach(_.posTag shouldBe a [Some[String]])
          case Failure(ex) => throw ex
        }
      }
    }
  }

}
