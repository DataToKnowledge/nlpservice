package it.dtk.nlp.detector

import it.dtk.nlp.BaseTestClass
import it.dtk.nlp.db.Word
import scala.util.Success
import scala.util.Failure

object CrimeDetectorSpec {
  val sentence2Crimes = Vector(new Word(token = "oggi", tokenId = Option(0)), new Word(token = "illegale",tokenId = Option(1)), 
      new Word(token = "inadempienza", tokenId = Option(2)), new Word(token = "di", tokenId = Option(3)), new Word(token = "contratto", tokenId = Option(4)))
  val sentenceCaseInsensitive = Vector(new Word(token = "oggi",tokenId = Option(0)), new Word(token = "è", tokenId = Option(1)), 
      new Word(token = "tutto", tokenId = Option(2)), new Word(token = "Illegale",tokenId = Option(3)))
  // val sentence0Crimes = Vector(new Word(token = "oggi"), new Word(token = "è"), new Word(token = "tutto"), new Word(token = "ok"), new Word(token = "contratto"))

}
class CrimeDetectorSpec extends BaseTestClass {
  import CrimeDetectorSpec._

  "A CrimeDetector" when {

    "finds crimes" should {

      "tag a compound-compound" in {

        val result = CrimeDetector.detect(sentence2Crimes)

        result match {
          case Success(res) =>
            //res.foreach(w => println(w.token + " " + w.iobEntity))
            res.count(_.iobEntity.contains(EntityType.B_CRIME.toString())) should be(2)
            res.count(_.iobEntity.contains(EntityType.B_CRIME.toString())) should be(2)

          case Failure(ex) =>
            ex.printStackTrace()
        }

      }
      "be case insensitive" in {
        val result = CrimeDetector.detect(sentenceCaseInsensitive)

        result match {
          case Success(res) =>
            //res.foreach(w => println(w.token + " " + w.iobEntity))
            res.count(_.iobEntity.contains(EntityType.B_CRIME.toString())) should be(1)

          case Failure(ex) =>
            ex.printStackTrace()
        }
      }
    }

  }
}