package it.dtk.nlp

import com.ning.http.client._
import java.util.concurrent.Executor
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors
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
  def process(text: String): Future[(Map[String, Double], Seq[Sentence])] = {
    val params = Map("text" -> text)
    val res = client.post(urlPost, params).map { response =>
      val body = response.getResponseBody
      parseText(body)
    }
    res
  }

  private def parseText(text: String): (Map[String, Double], Seq[Sentence]) = {

    //remove the first line that represents the header # FILE: input/prova.in
    val lines = text.split("\n")

    lines.toList match {
      //split the list base on the lines
      case fileName :: keys :: fieldsHeader :: fields =>

        val keywords = extractKeywords(keys)
        val seqSentences = extractSentences(fields)
        (keywords, seqSentences)
      case _ =>
        (Map.empty[String, Double], Vector.empty[Sentence])
    }
  }

  private def extractKeywords(text: String): Map[String, Double] = {

    val clean = text.split(":")(1).trim

    val split = clean.split(">")
    val keyValuePair = split.map(_.split("<")).map { array =>
      val key = array(0).trim
      val elemArray = array(1).trim.split(" ")
      key -> elemArray(0).toDouble
    }

    keyValuePair.foldLeft(Map.empty[String, Double])((map, elem) => map + elem)
  }

  private def extractSentences(lines: List[String]): Seq[Sentence] = {

    @tailrec
    def extractSentencesTail(acc: Seq[Sentence], curr: Seq[Word], head: String, tail: List[String]): Seq[Sentence] = {
      //extract the word
      val split = head.split("\t")

      val word = split match {
        case split10 if split10.size == 10 =>
          var iobEntity = Vector.empty[String]
          if (!split(8).equals("O"))
            iobEntity = Vector(split(8))
            Option(Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, iobEntity, Option(split(9))))
            
        case split8 if split8.size == 8 =>
          Option(Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
          Option(split(3).toInt), Option(split(4)), Option(split(5)),
          Option(split(6)), Option(split(7)), None, Vector.empty[String],None))
            
        case _ =>
          None
      }

      //FIXME: this adds empty words
      val pair = if (word.getOrElse(Word("")).sentence.getOrElse("") == "<eos>") {
        (acc :+ Sentence(curr :+ word.getOrElse(Word(""))), Vector.empty[Word])
      } else {
        (acc, curr :+ word.getOrElse(Word("")))
      }
      if (tail == Nil)
        pair._1
      else
        extractSentencesTail(pair._1, pair._2, tail.head, tail.tail)
    }
    extractSentencesTail(Vector.empty[Sentence], Vector.empty[Word], lines.head, lines.tail)
  }

}

case class BadStatus(url: String, status: Int) extends Throwable(s"HTTP status code: ${status.toString}")
case class GetException(url: String, innerException: Throwable) extends Throwable(innerException.getMessage)

object AsyncWebClient {
  val builder = new AsyncHttpClientConfig.Builder()
  builder.setFollowRedirects(true)
  builder.setCompressionEnabled(false)
  builder.setConnectionTimeoutInMs(240.seconds.toMillis.toInt)
  builder.setRequestTimeoutInMs(240.seconds.toMillis.toInt)
  builder.setMaximumConnectionsPerHost(2)
  builder.setAllowPoolingConnection(true)

  private val client = new AsyncHttpClient(builder.build())

  def get(url: String)(implicit exec: Executor): Future[Response] = {
    val u = url
    val f = client.prepareGet(url).execute()
    val p = Promise[Response]()
    f.addListener(new Runnable {
      def run() = {
        try {
          val response = f.get()
          if (response.getStatusCode < 400)
            p.success(response)
          else p.failure(BadStatus(u, response.getStatusCode))
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
      def run() = {
        try {
          val response = post.get()
          if (response.getStatusCode < 400)
            p.success(response)
          else p.failure(BadStatus(u, response.getStatusCode))
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
