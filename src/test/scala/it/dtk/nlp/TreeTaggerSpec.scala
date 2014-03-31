package it.dtk.nlp

object TreeTaggerSpec {

  val words = Vector(
    ("Maxi", "NOM"),
    ("blitz", "NOM"),
    ("contro", "ADV"),
    ("i", "DET:def"),
    ("trafficanti", "NOM"),
    ("di", "PRE"),
    ("droga", "NOM"),
    ("nel", "PRE:det"),
    ("Tarantino", "NOM"),
    (".", "SENT")
  )

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TreeTaggerSpec extends BaseTestClass {

  import TreeTaggerSpec._

  "A TreeTagger" when {

    "tags a sequence of words" should {

      words.foreach {
        w =>
          s"return the corrent posTag for '${w._1}'" in {
            TreeTagger.tag(w._1).posTag.get should be(w._2)
          }
      }

    }

  }

}
