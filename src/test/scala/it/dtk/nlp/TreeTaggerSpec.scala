package it.dtk.nlp

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object TreeTaggerSpec {

  val executorService = Executors.newSingleThreadExecutor()

  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

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
            whenReady(TreeTagger.tag(w._1)) {
              _.get should be(w._2)
            }
          }
      }

    }

  }

}
