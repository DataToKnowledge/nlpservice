package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.WordStemmer
import akka.actor.Props
import it.dtk.nlp.db.Word
import akka.routing.RoundRobinPool

object StemmerActor {
  case class Process(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Word], value: NewsPart)

  def props = Props(classOf[StemmerActor])
  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)
}
/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class StemmerActor extends Actor with ActorLogging {

  import StemmerActor._
  
  val detector = new WordStemmer

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender ! Result(newsId, sentences.map(detector.stem), Title)

    case Process(newsId, sentences, Summary) =>
      sender ! Result(newsId, sentences.map(detector.stem), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender ! Result(newsId, sentences.map(detector.stem), Corpus)

  }

}