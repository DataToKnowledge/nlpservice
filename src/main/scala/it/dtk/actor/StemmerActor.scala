package it.dtk.actor

import akka.actor.{Actor, ActorLogging}
import it.dtk.actor.NewsPart._
import it.dtk.nlp.db.Sentence
import it.dtk.nlp.WordStemmer

object StemmerActor {
  case class Process(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], value: NewsPart)
}
/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class StemmerActor extends Actor with ActorLogging {

  import StemmerActor._

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(WordStemmer.stem))), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(WordStemmer.stem))), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(s => Sentence(s.words.map(WordStemmer.stem))), Corpus)

  }
  
}