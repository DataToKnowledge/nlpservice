package it.dtk.nlp

object LemmatizerSpec {

  val words = Vector(
    ("finisce", Option("finire")),
    ("scampati", Option("scampare")),
    ("rivisto", Option("rivisto")),
    ("mangiato", Option("mangiare")),
    ("dormito", Option("dormire"))
  )

  val wrong = Vector(
    ("dormito", Option("dormi"))
  )

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerSpec extends BaseTestClass {

  import LemmatizerSpec._

  "A Lemmatizer" when {
    
    val detector = new Lemmatizer

    "lemmatize a word" should {

      words.foreach {
        w =>
          s"return the corrent lemma for '${w._1}'" in {
            detector.lemma(w._1) should be(w._2)
          }
      }

      wrong.foreach {
        w =>
          s"recognize the wrong lemma for '${w._1}'" in {
            detector.lemma(w._1) should not be w._2
          }
      }
    }
  }

}
