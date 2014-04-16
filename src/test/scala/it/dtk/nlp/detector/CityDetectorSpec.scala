package it.dtk.nlp.detector

import it.dtk.nlp.{ TreeTagger, TextPreprocessor, BaseTestClass }
import scala.util.Success
import scala.util.Failure

object CityDetectorSpec {

  val sentence = "Si tratta di un 57enne di Grumello Del Monte arrestato dai carabinieri con l’accusa di detenzione illegale di arma da sparo."

  val city = "BARI"

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetectorSpec extends BaseTestClass {

  import CityDetectorSpec._

  "A CityDetector" when {

    "finds cities" should {

      "tag a compound-city" in {

        whenReady(TreeTagger.tag(TextPreprocessor.apply(sentence))) {
          sentence =>
            val result = CityDetector.detect(sentence)
            result match {
              case Success(res) =>
                res.count(_.iobEntity.contains("B-CITY")) should be(1)
                res.count(_.iobEntity.contains("I-CITY")) should be(2)
              case Failure(ex) =>
                ex.printStackTrace()
            }
        }
      }

      "tag a city" in {

        whenReady(TreeTagger.tag(TextPreprocessor.apply(city))) {
          sentence =>
            val result = CityDetector.detect(sentence)

            result match {
              case Success(res) =>
                res.count(_.iobEntity.contains("B-CITY")) should be(1)
                res.count(_.iobEntity.contains("I-CITY")) should be(0)
              case Failure(ex) =>
                ex.printStackTrace()
            }

        }
      }

    }

  }
}
