package it.dtk.nlp

import java.util.concurrent._
import scala.concurrent.ExecutionContext
import it.dtk.nlp.db._

object TextProClientSpec {

  val title = "Ricercato per traffico di droga, latitante albanese arrestato a Cassano"

  val firstWordResult = Word("Ricercato", Some(1), Some(0), Some(9), Some("-"), Some("VSP"), Some("ricercare"), Some("ricercare+v+part+pass+m+nil+sing"), None, Vector.empty[String], Some("B-VX"))

  val summary = "L'uomo, che nel frattempo aveva 'cambiato identità' dotandosi di un nuovo passaporto," +
    " risiedeva da qualche tempo nel Comune dell'hinterland barese. E' stato arrestato dai finanzieri"

  val firstWordSummary = Word("L'", Some(1), Some(0), Some(2), Some("-"), Some("RS"), Some("det"), Some("det+art+_+sing"), None, Vector.empty[String], Some("B-NP"))

  val text = """Era ricercato per una condanna definitiva a quattro anni di reclusione emessa dal Tribunale di Firenze per traffico internazionale di sostanze stupefacenti. Da mesi si era reso irreperibile, dopo essersi allontanato dall’ultima residenza di San Giovanni Valdarno, in provincia di Arezzo.
Nel frattempo per il latitante - Julian Binaj, classe 1989, albanese - aveva 'cambiato identità' procurandosi un falso passaporto e si era trasferito a Cassano.
A scoprirlo ed arrestarlo sono stati i finanzieri del G.I.C.O. di Bari, che dopo una serie di attività investigative hanno accertato che il narcotrafficante si trovava proprio nel Comune dell'hinterland barese. L'uomo, tra l'altro, è stato bloccato dai militari proprio nelle piazza centrale del paese. Nel tentativo di sfuggire all'arresto ha tentato di far credere che si trattasse di uno scambio di persona, mostrando il falso passaporto e addirittura un falso tesserino per far credere di essere un agente sotto copertura di un’agenzia governativa della Repubblica albanese, la C.B.I. (Central Bureau of Investigation).
Il latitante è stato arrestato e condotto in carcere."""
    
    val firstTextWord = Word("Era",Some(1),Some(0),Some(3),Some("-"),Some("VI"),Some("essere"),Some("essere+v+indic+imperf+nil+3+sing"),None,Vector.empty[String],Some("B-VX"))

  val news = News("id", "http://www.baritoday.it/cronaca/", "http://www.baritoday.it/cronaca/narcotrafficante-albanese-latitante-arrestato-a-cassano.html",
    Option(title), Option(summary), None)

  private val executorService = Executors.newSingleThreadExecutor()
  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
}

class TextProClientSpec extends BaseTestClass {
  import TextProClientSpec._

  "A TextProClient" when {

    val client = new TextProClient

    "process the title of a news" should {
      val result = client.process(Option(title))

      whenReady(result) { res =>
        val tags = res._1
        val sentences = res._2

        "should have tags" in {
          tags.size should be > 0
        }

        "should have 7 tags" in {
          tags.size should be(7)
        }

        "should have a sentence" in {
          sentences.size should be > 0
        }

        "the sentence should be composed of 1 sentence" in {
          sentences.size should be(1)
        }

        "the first word in the sentence is " + firstWordResult in {
          val firstSentenceWord = sentences.head.words(0)
          firstSentenceWord shouldBe firstWordResult
        }
      }
    }

    "process the summary of a news" should {
      val result = client.process(Option(summary))

      whenReady(result) { res =>
        val tags = res._1
        val sentences = res._2

        "should have tags" in {
          tags.size should be > 0
        }

        "should have 10 tags" in {
          tags.size should be(10)
        }

        "should have a sentence" in {
          sentences.size should be > 0
        }

        "the sentence should be composed of 2 sentence" in {
          sentences.size should be(2)
        }

        "the first word in the sentence is " + firstWordSummary in {
          val firstSentenceWord = sentences.head.words(0)
          firstSentenceWord shouldBe firstWordSummary
        }

      }
    }

    "process the text of a news" should {
      val result = client.process(Option(text))

      whenReady(result) { res =>
        val tags = res._1
        val sentences = res._2

        "should have tags" in {
          tags.size should be > 0
        }

        "should have 15 tags" in {
          tags.size should be(15)
        }

        "should have a sentence" in {
          sentences.size should be > 0
        }

        "the sentence should be composed of 7 sentence" in {
          sentences.size should be(7)
          //println(sentences.value.length)
        }

        "the first word in the sentence is " + firstTextWord in {
          val firstSentenceWord = sentences.head.words(0)
          firstSentenceWord shouldBe firstTextWord
        }

      }
    }
  }

}