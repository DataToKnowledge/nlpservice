package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.Lemmatizer
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.db.Word

object LemmatizerActor {
  case class Process(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Word], value: NewsPart)

  def props = Props(classOf[LemmatizerActor])

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
class LemmatizerActor extends Actor with ActorLogging {

  import LemmatizerActor._

  def receive = {
    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(Lemmatizer.lemma), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(Lemmatizer.lemma), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(Lemmatizer.lemma), Corpus)
  }

}