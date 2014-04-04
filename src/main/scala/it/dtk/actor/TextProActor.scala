package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.Sentence
import NewsPart._
import akka.actor.Props
import it.dtk.nlp.TextProClient
import scala.util.Success
import scala.util.Failure
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import akka.routing.RoundRobinPool
import akka.routing.RoundRobinPool
import it.dtk.nlp.db.News

object TextProActor {
  case class Parse(newsId: String, news: News)
  case class Result(newsId: String, news: Seq[Sentence], keywords: Map[String, Double], newsPart: NewsPart)
  case class Fail(newsId: String, text: String, value: NewsPart)

  def props =
    Props(classOf[TextProActor])

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

    case Parse(newsId, text, newsPart) =>
      val send = sender
      textProClient.process(text).onComplete {
        case Success((keywords, sentences)) =>
          send ! Result(newsId, sentences, keywords, newsPart)
        case Failure(ex) =>
          send ! Fail(newsId, text, newsPart)
      }
  }
}