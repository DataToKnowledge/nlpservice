package it.dtk.actor

import akka.actor.{Actor, ActorLogging}
import it.dtk.actor.NewsPart._
import it.dtk.nlp.TextPreprocessor
import it.dtk.nlp.db.Sentence

object TokenizerActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentence: Sentence, value: NewsPart)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TokenizerActor extends Actor with ActorLogging {

  import TokenizerActor._

  def receive = {

    case Process(newsId, text, Title) =>
      sender() ! Result(newsId, TextPreprocessor.getTokens(text), Title)

    case Process(newsId, text, Summary) =>
      sender() ! Result(newsId, TextPreprocessor.getTokens(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender() ! Result(newsId, TextPreprocessor.getTokens(text), Corpus)

  }
  
}