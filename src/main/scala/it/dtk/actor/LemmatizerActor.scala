package it.dtk.actor

import akka.actor.{Actor, ActorLogging}
import it.dtk.actor.NewsPart._
import it.dtk.nlp.db.Sentence
import it.dtk.nlp.Lemmatizer

object LemmatizerActor {
  case class Process(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], value: NewsPart)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerActor extends Actor with ActorLogging {

  import LemmatizerActor._

  def receive = {
    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(Lemmatizer.lemma))), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(Lemmatizer.lemma))), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(Lemmatizer.lemma))), Corpus)
  }
  
}