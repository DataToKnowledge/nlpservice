package it.dtk.nlp.detector

import it.dtk.nlp.BaseTestClass
import it.dtk.nlp.db.Word
import scala.util.Success
import scala.util.Failure
import it.dtk.nlp.TreeTagger
import it.dtk.nlp.TextPreprocessor

object CrimeDetectorSpec {
  val sentence2Crimes = Vector(new Word(token = "oggi", tokenId = Option(0)), new Word(token = "illegale", tokenId = Option(1)),
    new Word(token = "inadempienza", tokenId = Option(2)), new Word(token = "di", tokenId = Option(3)), new Word(token = "contratto", tokenId = Option(4)))
  val sentenceCaseInsensitive = Vector(new Word(token = "oggi", tokenId = Option(0)), new Word(token = "è", tokenId = Option(1)),
    new Word(token = "tutto", tokenId = Option(2)), new Word(token = "Illegale", tokenId = Option(3)))
  // val sentence0Crimes = Vector(new Word(token = "oggi"), new Word(token = "è"), new Word(token = "tutto"), new Word(token = "ok"), new Word(token = "contratto"))

  val text = "BARI - Agguato giovedì sera in piazza Santa Maria del campo a Ceglie alla periferia di Bari. Luigi Boffo 21 anni è stato ferito con almeno sei colpi di pistola fatti esplodere. Il giovane, con precedenti per droga - è stato anche arrestato nel settembre 2010 (con altre quattro persone per spaccio davanti a una discoteca di Castellaneta Marina) - è ritenuto vicino al clan Di Cosola.\n\nLA SITUAZIONE - Il ragazzo è arrivato al pronto soccorso dell'ospedale Di Venere intorno a mezzanotte. Il 21enne è ancora ricoverato in prognosi riservata in gravi condizioni nel reparto di rianimazione. La polizia indaga sull'episodio. Secondo una ricostruzione fatta dalla polizia, il sicario ha raggiunto la piazza dove si trovava Boffo a bordo di un'automobile condotta da un'altra persona. Ha quindi sparato con una pistola a tamburo per poi fuggire. L'agguato è stato ripreso da alcune telecamere di sicurezza che si trovano nella zona, che di sera è un luogo di ritrovo frequentato da giovani."
}
class CrimeDetectorSpec extends BaseTestClass {
  import CrimeDetectorSpec._

  "A CrimeDetector" when {

    val detector = CrimeDetector
    val textPreprocessor = new TextPreprocessor

    "finds crimes" should {

      "tag a compound-compound" in {

        val result = detector.detect(sentence2Crimes)

        result match {
          case Success(res) =>
            //res.foreach(w => println(w.token + " " + w.iobEntity))
            res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.B_CRIME))) should be(2)
            res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.I_CRIME))) should be(2)

          case Failure(ex) =>
            ex.printStackTrace()
        }

      }
      "be case insensitive" in {
        val result = detector.detect(sentenceCaseInsensitive)

        result match {
          case Success(res) =>
            //res.foreach(w => println(w.token + " " + w.iobEntity))
            res.count(_.iobEntity.contains(EntityType.stringValue(EntityType.B_CRIME))) should be(1)

          case Failure(ex) =>
            ex.printStackTrace()
        }
      }
    }

    "tag crimes in the give text" in {
      whenReady(TreeTagger.tag(textPreprocessor.apply(text))) { s =>

        val result = detector.detect(s.toIndexedSeq)
        result.foreach(_.foreach(println))
      }
    }

  }
}