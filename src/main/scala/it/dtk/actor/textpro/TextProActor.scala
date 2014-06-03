package it.dtk.actor.textpro

import it.dtk.nlp.db.News
import akka.actor.Props
import akka.routing.FromConfig
import it.dtk.nlp.db.Word
import akka.actor.{ Actor, ActorLogging, ActorRef }
import scala.util.{ Success, Failure }
import it.dtk.nlp.db.Nlp

object TextProActor {

  case class Parse(news: News)

  case class Result(news: News)

  case class FailProcessingLine(ex: Throwable)

  case class Fail(newsId: String, ex: Throwable)

  def props() = Props[TextProActor]

  /**
   * the name should be TextProRouter
   * @param nrOfInstances
   * @return
   */
  def routerProps(nrOfInstances: Int = 1) =
    FromConfig.props(props)
  //    //RoundRobinPool(nrOfInstances).props(props)

  private case class ElementPair(keywords: Map[String, Double], words: Seq[Word])
}

class TextProActor extends Actor with ActorLogging {

  import TextProActor._

  def receive: Receive = {

    case Parse(news) =>

      val send = sender

      val titleTry = TextProCaller.tagText(news.title).
        flatMap(TextProResultProcessor.parseText).map(filterWrongWords(_, send))

      val summaryTry = TextProCaller.tagText(news.summary).
        flatMap(TextProResultProcessor.parseText).map(filterWrongWords(_, send))

      val corpusTry = TextProCaller.tagText(news.corpus).
        flatMap(TextProResultProcessor.parseText).map(filterWrongWords(_, send))

      val descriptionTry = TextProCaller.tagText(news.metaDescription).
        flatMap(TextProResultProcessor.parseText).map(filterWrongWords(_, send))


      val nlpNews = Nlp(title = titleTry.map(_.words).toOption,
        summary = summaryTry.map(_.words).toOption,
        corpus = corpusTry.map(_.words).toOption,
        description = descriptionTry.map(_.words).toOption,
        nlpTags = corpusTry.map(_.keywords).toOption)

      val modNews = news.copy(nlp = Option(nlpNews))

      send ! Result(modNews)
      
      if (corpusTry.isFailure)
        send ! Fail(news.id, corpusTry.failed.get)
  }

  /**
   * process the try of words and send the errors back to the sender
   * @param element
   * @param send
   * @return
   */
  private def filterWrongWords(element: KeywordsWordsPair, send: ActorRef): ElementPair =
    ElementPair(element.keywords,
      element.words.foldLeft(List.empty[Word]) { (acc, tryW) =>
        tryW match {
          case Success(w) =>
            w :: acc //prepend for performance reason
          case Failure(ex) =>
            send ! FailProcessingLine(ex)
            acc
        }
      }.reverse)

}