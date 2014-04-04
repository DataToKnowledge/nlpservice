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

object TextProActor {
  case class Parse(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], keywords: Map[String, Double], newsPart: NewsPart)
  case class Fail(newsId: String, text: String, value: NewsPart)
  
  def props = Props(classOf[TextProActor])
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