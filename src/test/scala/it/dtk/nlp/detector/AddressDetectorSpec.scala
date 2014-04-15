package it.dtk.nlp.detector

import it.dtk.nlp.{TreeTagger, TextPreprocessor, BaseTestClass}

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
      whenReady(TreeTagger.tag(TextPreprocessor.apply(address))) {
        sentence =>
          val result = AddressDetector.detect(sentence)

          result.count(_.iobEntity.contains("B-ADDRESS")) should be(1)
          result.count(_.iobEntity.contains("I-ADDRESS")) should be(2)
      }
    }

    "tag an address of a defined city" in {
      whenReady(TreeTagger.tag(TextPreprocessor.apply(address))) {
        sentence =>
          val result = AddressDetector.detect(sentence, city)

          result.count(_.iobEntity.contains("B-ADDRESS")) should be(1)
          result.count(_.iobEntity.contains("I-ADDRESS")) should be(2)
      }
    }

  }
}
