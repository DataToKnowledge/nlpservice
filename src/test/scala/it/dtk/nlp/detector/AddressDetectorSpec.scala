package it.dtk.nlp.detector

import it.dtk.nlp.{ TreeTagger, TextPreprocessor, BaseTestClass }
import scala.util.Success
import scala.util.Failure

object AddressDetectorSpec {

  val sentence = "Si tratta di un 57enne di Grumello Del Monte arrestato dai carabinieri con l’accusa di detenzione illegale di arma da sparo in Via De Rossi n. 63."
  val address = "Via De Rossi"
  val city = Option("Bari")

}

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
class AddressDetectorSpec extends BaseTestClass {

  import AddressDetectorSpec._

  "An AddressDetector" should {

    "tag an address" in {
      whenReady(TreeTagger.tag(TextPreprocessor.apply(sentence))) {
        s =>
          val result = AddressDetector.detect(s.toIndexedSeq)
          result match {
            case Success(res) =>
              //res.foreach(w => println(w.token + " " + w.iobEntity))
              res.count(_.iobEntity.contains(EntityType.B_ADDRESS.toString())) should be(1)
            case Failure(ex) =>
              ex.printStackTrace()
          }

      }
    }

    "tag an address of a defined city" in {
      whenReady(TreeTagger.tag(TextPreprocessor.apply(sentence))) {
        s =>
          val result = AddressDetector.detect(s.toIndexedSeq, city)

          result match {
            case Success(res) =>
              //res.foreach(w => println(w.token + " " + w.iobEntity))
              res.count(_.iobEntity.contains(EntityType.B_ADDRESS.toString())) should be(1)
              res.count(_.iobEntity.contains(EntityType.I_ADDRESS.toString())) should be(2)
            case Failure(ex) =>
              ex.printStackTrace()
          }
      }
    }

  }
}
