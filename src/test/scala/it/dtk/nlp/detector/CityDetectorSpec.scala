package it.dtk.nlp.detector

import it.dtk.nlp.{TreeTagger, TextPreprocessor, BaseTestClass}

object CityDetectorSpec {

  val sentence = "Si tratta di un 57enne di Grumello Del Monte arrestato dai carabinieri con l’accusa di detenzione illegale di arma da sparo."

  val city = "Bari"

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetectorSpec extends BaseTestClass {

  import CityDetectorSpec._

  "A CityDetector" when {

    val detector = CityDetector

    "finds cities" should {

      "tag a compound-city" in {

        val result = detector.detect(TreeTagger.apply(TextPreprocessor.apply(sentence).head)).words

        result.count(_.iobEntity.contains("B-CITY")) should be (1)
        result.count(_.iobEntity.contains("I-CITY")) should be (2)
      }

      "tag a city" in {

        val result = detector.detect(TreeTagger.apply(TextPreprocessor.apply(city).head)).words

        result.count(_.iobEntity.contains("B-CITY")) should be (1)
        result.count(_.iobEntity.contains("I-CITY")) should be (0)
      }

    }

  }
}
