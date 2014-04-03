package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.Sentence
import NewsPart._

object TextProActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], keywords: Map[String, Double], value: NewsPart)
}

class TextProActor extends Actor with ActorLogging {

  import TextProActor._

  def receive = {

    case Process(newsId, text, Title) =>

    case Process(newsId, text, Summary) =>

    case Process(newsId, text, Corpus) =>

  }

}