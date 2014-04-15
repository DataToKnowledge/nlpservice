package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import akka.actor.Props
import it.dtk.nlp.TextProClient
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.routing.RoundRobinPool
import it.dtk.nlp.db._

object TextProActor {
  case class Parse(news: News)
  case class Result(news: News)
  case class Fail(news: News, ex: Throwable)

  def props = Props(classOf[TextProActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 3) =
    RoundRobinPool(nrOfInstances).props(props)

}

class TextProActor extends Actor with ActorLogging {

  implicit val exec = context.dispatcher.asInstanceOf[Executor with ExecutionContext]
  import TextProActor._

  private val textProClient = new TextProClient

  def receive = {

    case Parse(news) =>
      val send = sender()
      
      val res = for {
        (titleKeywords, titleSentences) <- textProClient.process(news.title)
        (summKeywords, summSentences) <- textProClient.process(news.summary)
        (corpKeywords, corpSentences) <- textProClient.process(news.text)
      } yield (titleSentences, summSentences, corpSentences, corpKeywords)
      
      
      res.onComplete {
        case Success((titleSentences, summSentences, corpSentences, corpKeywords)) =>
          val modNews = news.copy(nlpTitle = Option(titleSentences), nlpSummary = Option(summSentences),
              nlpText = Option(corpSentences), nlpTags = Option(corpKeywords))
          send ! Result(modNews)
        case Failure(ex) =>
          send ! Fail(news,ex)
      }
  }
}