package it.dtk.nlp

object LemmatizerSpec {

  val words = Vector(
    ("finisce", Option("finire")),
    ("scampati", Option("scampare")),
    ("rivisto", Option("rivisto")),
    ("mangiato", Option("mangiare")),
    ("dormito", Option("dormire"))
  )

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerSpec extends BaseTestClass {

  import LemmatizerSpec._

  "A Lemmatizer" when {

    val lemmatizer = new Lemmatizer("10.1.0.62", "wheretolive")

    "lemmatize a word" should {

      words.foreach {
        w =>
          s"return the corrent lemma for '${w._1}'" in {
            lemmatizer.lemma(w._1) should be(w._2)
          }
      }
    }
  }

}
