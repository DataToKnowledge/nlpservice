package it.dtk.nlp

import com.ning.http.client._
import java.util.concurrent.Executor
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scala.util.Success
import scala.util.Failure
import scala.annotation.tailrec
import it.dtk.nlp.db._

class TextProClient {

  private val executorService = Executors.newCachedThreadPool()
  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

  private val client = AsyncWebClient

  private val urlPost = "http://10.1.0.61:7070/nlp"

  /**
   * @param text
   * @return a tuple of tags and sequence of sentences
   */
  def process(text: String): Future[(Option[Map[String, Double]], Option[Seq[Sentence]])] = {
    val params = Map("text" -> text)
    val res = client.post(urlPost, params).map { response =>
      val body = response.getResponseBody()
      parseText(body)
    }
    res
  }

  private def parseText(text: String): (Option[Map[String, Double]], Option[Seq[Sentence]]) = {

    //remove the first line that represents the header # FILE: input/prova.in
    val lines = text.split("\n")

    lines.toList match {
      //split the list base on the lines
      case fileName :: keys :: fieldsHeader :: fields =>

        val keywords = extractKeywords(keys)
        val seqSentences = extractSentences(fields)
        (keywords, seqSentences)
      case _ =>
        (None, None)
    }
  }

  private def extractKeywords(text: String): Option[Map[String, Double]] = {

    val clean = text.split(":")(1).trim

    val split = clean.split(">")
    val keyValuePair = split.map(_.split("<")).map { array =>
      val key = array(0).trim
      val elemArray = array(1).trim.split(" ")
      key -> elemArray(0).toDouble
    }
    val mapKeywords = keyValuePair.foldLeft(Map.empty[String, Double])((map, elem) => map + elem)

    if (mapKeywords.isEmpty) None
    else
      Option(mapKeywords)
  }

  private def extractSentences(lines: List[String]): Option[Seq[Sentence]] = {

    @tailrec
    def extractSentencesTail(acc: Seq[Sentence], curr: Seq[Word], head: String, tail: List[String]): Option[Seq[Sentence]] = {
      //extract the word
      val split = head.split("\t")

      val word = split match {
        case split10 if (split10.size == 10) =>
          Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, Vector(split(8)), Option(split(9)))
            
        case split8 if (split8.size == 8) =>
           Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, Vector.empty[String],None)
            
        case _ =>
          Word("")
      }

      val pair = if (word.sentence.getOrElse("") == "<eos>") {
        (acc :+ Sentence(curr :+ word), Vector.empty[Word])
      } else {
        (acc, curr :+ word)
      }
      if (tail == Nil)
        Option(pair._1)
      else
        extractSentencesTail(pair._1, pair._2, tail.head, tail.tail)
    }
    extractSentencesTail(Vector.empty[Sentence], Vector.empty[Word], lines.head, lines.tail)
  }

  def process(news: News): News = ???

}

case class BadStatus(url: String, status: Int) extends Throwable(s"HTTP status code: ${status.toString}")
case class GetException(url: String, innerException: Throwable) extends Throwable(innerException.getMessage)

object AsyncWebClient {
  val builder = new AsyncHttpClientConfig.Builder()
  builder.setFollowRedirects(true)
  builder.setCompressionEnabled(false)
  builder.setConnectionTimeoutInMs(240.seconds.toMillis.toInt)
  builder.setRequestTimeoutInMs(240.seconds.toMillis.toInt)
  //builder.setMaximumConnectionsPerHost(2)
  builder.setAllowPoolingConnection(true)

  private val client = new AsyncHttpClient(builder.build())

  def get(url: String)(implicit exec: Executor): Future[Response] = {
    val u = url
    val f = client.prepareGet(url).execute()
    val p = Promise[Response]()
    f.addListener(new Runnable {
      def run = {
        try {
          val response = f.get()
          if (response.getStatusCode() / 100 < 4)
            p.success(response)
          else p.failure(BadStatus(u, response.getStatusCode()))
        } catch {
          case t: Throwable =>
            p.failure(GetException(u, t))
        }
      }

    }, exec)
    p.future
  }

  def post(url: String, params: Map[String, String])(implicit exec: Executor): Future[Response] = {
    val u = url
    val f = client.preparePost(url).addHeader("Content-Type", "application/x-www-form-urlencoded ")
    //add post parameters
    params.foreach(pair => f.addParameter(pair._1, pair._2))
    val post = f.execute()
    val p = Promise[Response]()
    post.addListener(new Runnable {
      def run = {
        try {
          val response = post.get()
          if (response.getStatusCode() < 400)
            p.success(response)
          else p.failure(BadStatus(u, response.getStatusCode()))
        } catch {
          case t: Throwable =>
            p.failure(t)
        }
      }
    }, exec)
    p.future
  }

  def shutdown(): Unit = client.close()
}

import ExecutionContext.Implicits.global

object Main extends App {
  val client = new TextProClient
  //val text = "Ricercato per traffico di droga, latitante albanese arrestato a Cassano"
  val text = """Era ricercato per una condanna definitiva a quattro anni di reclusione emessa dal Tribunale di Firenze per traffico internazionale di sostanze stupefacenti. Da mesi si era reso irreperibile, dopo essersi allontanato dall’ultima residenza di San Giovanni Valdarno, in provincia di Arezzo.
Nel frattempo per il latitante - Julian Binaj, classe 1989, albanese - aveva 'cambiato identità' procurandosi un falso passaporto e si era trasferito a Cassano.
A scoprirlo ed arrestarlo sono stati i finanzieri del G.I.C.O. di Bari, che dopo una serie di attività investigative hanno accertato che il narcotrafficante si trovava proprio nel Comune dell'hinterland barese. L'uomo, tra l'altro, è stato bloccato dai militari proprio nelle piazza centrale del paese. Nel tentativo di sfuggire all'arresto ha tentato di far credere che si trattasse di uno scambio di persona, mostrando il falso passaporto e addirittura un falso tesserino per far credere di essere un agente sotto copertura di un’agenzia governativa della Repubblica albanese, la C.B.I. (Central Bureau of Investigation).
Il latitante è stato arrestato e condotto in carcere."""
  val result = client.process(text)

  result.onComplete {
    case Success((tags, sentences)) =>
      println(tags)
      println(sentences)

    case Failure(ex) =>
      println(ex)
      ex.printStackTrace()
  }
}