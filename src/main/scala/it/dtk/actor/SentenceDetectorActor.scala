package it.dtk.actor

import akka.actor.{Actor, ActorLogging}
import it.dtk.actor.NewsPart._
import it.dtk.nlp.TextPreprocessor

object SentenceDetectorActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentences: Seq[String], value: NewsPart)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class SentenceDetectorActor extends Actor with ActorLogging {

  import SentenceDetectorActor._

  def receive = {

    case Process(newsId, text, Title) =>
      sender() ! Result(newsId, TextPreprocessor.getSentences(text), Title)

    case Process(newsId, text, Summary) =>
      sender() ! Result(newsId, TextPreprocessor.getSentences(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender() ! Result(newsId, TextPreprocessor.getSentences(text), Corpus)

  }
  
}