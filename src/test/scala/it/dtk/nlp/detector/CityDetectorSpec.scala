package it.dtk.nlp.detector

import it.dtk.nlp.{ TreeTagger, TextPreprocessor, BaseTestClass }
import scala.util.Success
import scala.util.Failure

object CityDetectorSpec {

  val sentence = "Si tratta di un 57enne di Grumello Del Monte arrestato dai carabinieri con l’accusa di detenzione illegale di arma da sparo."

  val city = "la citta in cui sono nato BARI"

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetectorSpec extends BaseTestClass {

  import CityDetectorSpec._

  "A CityDetector" when {

    val detector = new CityDetector
    val textPreprocessor = new TextPreprocessor

    "finds cities" should {

      "tag a compound-city" in {

        whenReady(TreeTagger.tag(textPreprocessor(sentence))) {
          sentence =>
            val result = detector.detect(sentence.toIndexedSeq)
            result match {
              case Success(res) =>
                //res.foreach(w => println(w.token + " " + w.iobEntity))
                res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.B_CITY))) should be(1)
                res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.I_CITY))) should be(2)
              case Failure(ex) =>
                ex.printStackTrace()
            }
        }
      }

      "tag a city" in {

        whenReady(TreeTagger.tag(textPreprocessor.apply(city))) {
          sentence =>
            val result = detector.detect(sentence.toIndexedSeq)

            result match {
              case Success(res) =>
                //res.foreach(w => println(w.token + " " + w.iobEntity))
                res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.B_CITY))) should be(1)
                res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.I_CITY))) should be(0)
              case Failure(ex) =>
                ex.printStackTrace()
            }

        }
      }

    }

  }
}
