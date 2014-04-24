package it.dtk.actor

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.annotation.tailrec
import scala.collection.mutable
import it.dtk.textpro.TextProActor
import it.dtk.nlp.db.Word
import it.dtk.nlp.db.News

object TextProActorWrapper {

  case class Parse(news: News)

  case class Result(news: News)

  case class Fail(newsId: String, ex: Throwable)

  def props() = Props[TextProActorWrapper]

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 1) =
    props().withRouter(RoundRobinRouter(nrOfInstances = nrOfInstances))

  //TODO akka 2.3.2
  //RoundRobinPool(nrOfInstances).props(props)

}

class TextProActorWrapper extends Actor with ActorLogging {

  import TextProActorWrapper._

  private val textProClient = context.system.actorOf(Props[TextProActor], "TextProActor")

  private val queue: mutable.Stack[News] = new scala.collection.mutable.Stack[News]

  private var jobs: Int = 0

  private var currentNews: News = null

  private var send: ActorRef = null

  def receive: Receive = {

    case Parse(news) =>
      send = sender()

      if (queue.isEmpty) {
        processNews(news)
      } else {
        queue :+ news
      }

    case TextProActor.Result(id, nlpText) =>
      jobs = jobs - 1

      val res = parseText(nlpText)

      var updatedNews: News = currentNews
      id.split("_")(1) match {
        case "title" =>
          updatedNews = updatedNews.copy(nlpTitle = Option(res._2))
        case "summary" =>
          updatedNews = updatedNews.copy(nlpSummary = Option(res._2))
        case "corpus" =>
          updatedNews = updatedNews.copy(nlpCorpus = Option(res._2))
      }
      updatedNews = updatedNews.copy(nlpTags = Option(updatedNews.nlpTags.getOrElse(Map.empty[String, Double]) ++ res._1))

      if (jobs == 0) {
        send ! Result(currentNews)
        if (queue.nonEmpty) {
          processNews(queue.pop())
        }
      }

    case TextProActor.Fail(id, ex) =>
      jobs = jobs - 1
      send ! Fail(id.split("_")(0), ex)

      if (jobs == 0) {
        if (queue.nonEmpty) {
          processNews(queue.pop())
        }
      }
    }

  private def processNews(news: News): Unit = {
    currentNews = news
    jobs = jobs + 3

    textProClient ! TextProActor.Parse(news.id + "_title", news.title.getOrElse(""))
    textProClient ! TextProActor.Parse(news.id + "_summary", news.summary.getOrElse(""))
    textProClient ! TextProActor.Parse(news.id + "_corpus", news.corpus.getOrElse(""))
  }

  private def parseText(text: String): (Map[String, Double], Seq[Word]) = {

    //remove the first line that represents the header # FILE: input/prova.in
    val lines = text.split("\n")

    lines.toList match {
      //split the list base on the lines
      case fileName :: keys :: fieldsHeader :: fields =>

        val keywords = extractKeywords(keys)
        val seqSentences = extractSentences(fields)
        (keywords, seqSentences)
      case _ =>
        (Map.empty[String, Double], Vector.empty[Word])
    }
  }

  private def extractKeywords(text: String): Map[String, Double] = {

    val clean = text.split(":")(1).trim

    val split = clean.split(">")
    val keyValuePair = split.map(_.split("<")).map {
      array =>
        val key = array(0).trim
        val elemArray = array(1).trim.split(" ")
        key -> elemArray(0).toDouble
    }

    keyValuePair.foldLeft(Map.empty[String, Double])((map, elem) => map + elem)
  }

  private def extractSentences(lines: List[String]): Seq[Word] = {

    @tailrec
    def extractSentencesTail(acc: Seq[Word], curr: Seq[Word], head: String, tail: List[String]): Seq[Word] = {
      //extract the word
      val split = head.split("\t")

      val word = split match {
        case split10 if split10.size == 10 =>
          val iobEntity = if (split(8).equals("O")) Set.empty[String] else Set(split(8))

          Option(Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, iobEntity, Option(split(9))))

        case split8 if split8.size == 8 =>
          Option(Word(split(0), Option(split(1).toInt), Option(split(2).toInt),
            Option(split(3).toInt), Option(split(4)), Option(split(5)),
            Option(split(6)), Option(split(7)), None, Set.empty[String], None))

        case _ =>
          None
      }

      //FIXME: this adds empty words
      val pair = if (word.getOrElse(Word("")).sentence.getOrElse("") == "<eos>") {
        (acc ++ (curr :+ word.getOrElse(Word(""))), Vector.empty[Word])
      } else {
        (acc, curr :+ word.getOrElse(Word("")))
      }
      if (tail == Nil)
        pair._1
      else
        extractSentencesTail(pair._1, pair._2, tail.head, tail.tail)
    }

    extractSentencesTail(Vector.empty[Word], Vector.empty[Word], lines.head, lines.tail)
  }

}