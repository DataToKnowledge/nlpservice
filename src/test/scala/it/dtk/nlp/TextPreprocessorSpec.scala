package it.dtk.nlp
import scala.language.postfixOps

object TextPreprocessorSpec {
  val docWithHtml = """
Madonnella, aperta la seconda metà della piazza. Ma i lavori non riprenderanno
Nessuna falsa partenza. Alle prime ore di stamattina i tecnici dell’impresa incaricata della riqualificazione di piazza Madonnella hanno ripreso i lavori. Dopo nove mesi di stop, per residenti e commercianti della zona è ora di tirare un sospiro di sollievo.
<a>Piazza Carabellese</a> è stata rinnovata solo per metà, da via Positano fino all’ingresso scuola elementare ‘Balilla’. Sul posto presente anche il presidente della VII circoscrizione  Micaela Paparella, che in questi mesi non ha fatto mancare critiche nei confronti dell’assessorato comunale ai Lavori Pubblici per un cantiere che si sarebbe dovuto riaprire già nel mese di gennaio: “Siamo felici per questa ripartenza, ora ci auguriamo che i lavori possano procedere con celerità al fine di concludere tutto entro 3-4 mesi al massimo”.
I lavori sono partiti nel luglio del 2012 con la promessa di chiudere l’operazione entro sei mesi. Il conto alla rovescia è iniziato. “Attendiamo giugno”, commenta un commerciante della zona, “stiamo pagando un prezzo eccessivo per questo cantiere, ci sentiamo abbandonati”. “Era ora – sospira Maria L., 62 anni residente su corso Sonnino -. C’è poco da festeggiare, siamo stanchi delle solite promesse”. 
Ma i tempi si sono allungati in modo drastico fino al blocco totale dei lavori, alla rimozione del materiale di scarto lasciato dall’azienda al centro della piazza (e fatto rimuovere dal Comune, ndr),  all’apertura di un corridoio pedonale e all’abbandono di rifiuti di ogni genere attorno alla storica effigie della Madonna. Il tutto a causa del Patto di Stabilità che ha impedito al Comune di procedere con la liquidazione dei pagamenti intermedi, anche se esponenti dell’opposizione di via Vaccaro lamentano una scarsa programmazione da parte della giunta Emiliano. Ora non rimane che aspettare. A meno dell'ennesimo intoppo.
Annuncio promozionale
    """.replaceAll("\\n", "")

  val expectedDocNoHtml = """
Madonnella, aperta la seconda metà della piazza. Ma i lavori non riprenderanno
Nessuna falsa partenza. Alle prime ore di stamattina i tecnici dell’impresa incaricata della riqualificazione di piazza Madonnella hanno ripreso i lavori. Dopo nove mesi di stop, per residenti e commercianti della zona è ora di tirare un sospiro di sollievo.
Piazza Carabellese è stata rinnovata solo per metà, da via Positano fino all’ingresso scuola elementare ‘Balilla’. Sul posto presente anche il presidente della VII circoscrizione  Micaela Paparella, che in questi mesi non ha fatto mancare critiche nei confronti dell’assessorato comunale ai Lavori Pubblici per un cantiere che si sarebbe dovuto riaprire già nel mese di gennaio: “Siamo felici per questa ripartenza, ora ci auguriamo che i lavori possano procedere con celerità al fine di concludere tutto entro 3-4 mesi al massimo”.
I lavori sono partiti nel luglio del 2012 con la promessa di chiudere l’operazione entro sei mesi. Il conto alla rovescia è iniziato. “Attendiamo giugno”, commenta un commerciante della zona, “stiamo pagando un prezzo eccessivo per questo cantiere, ci sentiamo abbandonati”. “Era ora – sospira Maria L., 62 anni residente su corso Sonnino -. C’è poco da festeggiare, siamo stanchi delle solite promesse”. 
Ma i tempi si sono allungati in modo drastico fino al blocco totale dei lavori, alla rimozione del materiale di scarto lasciato dall’azienda al centro della piazza (e fatto rimuovere dal Comune, ndr),  all’apertura di un corridoio pedonale e all’abbandono di rifiuti di ogni genere attorno alla storica effigie della Madonna. Il tutto a causa del Patto di Stabilità che ha impedito al Comune di procedere con la liquidazione dei pagamenti intermedi, anche se esponenti dell’opposizione di via Vaccaro lamentano una scarsa programmazione da parte della giunta Emiliano. Ora non rimane che aspettare. A meno dell'ennesimo intoppo.
Annuncio promozionale
    """.replaceAll("\\n", "")

  val oneSentenceDocument = "Madonnella, aperta la seconda metà della piazza."

  val nineWordsSentence = "Madonnella, aperta la seconda metà della piazza."

  val sentenceWithTab = "Madonnella,		 aperta la seconda metà 		della piazza."
}

class TextPreprocessorSpec extends BaseTestClass {

  import TextPreprocessorSpec._

  "The TextPreprocessing" when {
    "is called the method removeHtmlTags" should {
      "correctly remove html tags" in {
        val result = TextPreprocessor.removeHtmlTags(docWithHtml)
        result should be(expectedDocNoHtml)
      }

      "do not modify a correct document" in {
        TextPreprocessor.removeHtmlTags(expectedDocNoHtml) should be(expectedDocNoHtml)
      }
    }

    "is called the method get sentence" should {
      "correctly split a document into sentences" in {
        val res = TextPreprocessor.getSentences(expectedDocNoHtml)
        //todo this is only for output printing
        //val str = res.mkString("\n")
        //println(str)
        res.size should be > 0
      }

      "return an empty sequence for an empty string" in {
        val res = TextPreprocessor.getSentences("")
        res should have size 0
      }

      "return a sentence for a document containing only a sentence" in {
        val res = TextPreprocessor.getSentences(oneSentenceDocument)
        res should have size 1
      }
    }

    "is called the method get Words" should {
      "for a sentence with 9 words should return 9 words" in {
        val res = TextPreprocessor.getTokens(nineWordsSentence)
        res should have size 9
      }

      "for a sentence with 9 words and tabs should return 9 words" in {
        val res = TextPreprocessor.getTokens(nineWordsSentence)
        res should have size 9
      }
    }
    
    "is called the method TextPreprocessing" should {
      "return a document with sentences and words" in {
        val sentences = TextPreprocessor(expectedDocNoHtml)
        sentences.size should be > 0
        for (sentence <- sentences){
          sentence.words.size should be > 0
          for (word <- sentence.words){
            word.token.size should be > 0
          }
        }
      }
    }
  }
}