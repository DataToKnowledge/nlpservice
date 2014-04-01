package it.dtk.nlp.detector

import it.dtk.nlp.{TreeTagger, TextPreprocessor, BaseTestClass}
import org.scalatest.time.{Millis, Seconds, Span}

object AddressDetectorSpec {

  val sentence = "Si tratta di un 57enne di Grumello Del Monte arrestato dai carabinieri con l’accusa di detenzione illegale di arma da sparo in Via De Rossi n. 63."
  val address = "Via De Rossi"
  val city = "Bari"

}

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
class AddressDetectorSpec extends BaseTestClass {

  import AddressDetectorSpec._

  "An AddressDetector" should {

    "tag an address" in {
      whenReady(TreeTagger.apply(TextPreprocessor.apply(address).head)) {
        sentence =>
          val result = AddressDetectorGVE.detect(sentence).words

          result.count(_.iobEntity.contains("B-ADDRESS")) should be(1)
          result.count(_.iobEntity.contains("I-ADDRESS")) should be(2)
      }
    }

    "tag an address of a defined city" in {
      whenReady(TreeTagger.apply(TextPreprocessor.apply(address).head)) {
        sentence =>
          val result = AddressDetectorGVE.detect(sentence, city).words

          result.count(_.iobEntity.contains("B-ADDRESS")) should be(1)
          result.count(_.iobEntity.contains("I-ADDRESS")) should be(2)
      }
    }

  }
}
