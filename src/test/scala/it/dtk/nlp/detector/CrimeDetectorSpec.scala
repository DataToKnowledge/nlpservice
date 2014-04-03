package it.dtk.nlp.detector

import it.dtk.nlp.BaseTestClass
import it.dtk.nlp.db.Word
import it.dtk.nlp.db.Sentence
object CrimeDetectorSpec {
  val sentence2Crimes = Vector(new Word(token = "oggi"), new Word(token = "illegale"), new Word(token = "inadempienza"), new Word(token = "di"), new Word(token = "contratto"))
  val sentenceCaseInsensitive = Vector(new Word(token = "oggi"), new Word(token = "è"), new Word(token = "tutto"), new Word(token = "Illegale"))
 // val sentence0Crimes = Vector(new Word(token = "oggi"), new Word(token = "è"), new Word(token = "tutto"), new Word(token = "ok"), new Word(token = "contratto"))

}
class CrimeDetectorSpec extends BaseTestClass {
  import CrimeDetectorSpec._

  "A CrimeDetector" when {

    "finds crimes" should {

      "tag a compound-compound" in {

        val result = CrimeDetectorGVE.detect(Sentence(sentence2Crimes)).words

        result.count(_.iobEntity.contains("B-CRIME")) should be(2)
        result.count(_.iobEntity.contains("I-CRIME")) should be(2)

      }
      "be case insensitive" in {
        val result = CrimeDetectorGVE.detect(Sentence(sentenceCaseInsensitive)).words
        result.count(_.iobEntity.contains("B-CRIME")) should be(1)
      }
    }
    
  }
}