package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.Lemmatizer
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.db.Word

object LemmatizerActor {

  def props = Props(classOf[LemmatizerActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)

  case class Process(newsId: String, words: Seq[Word], value: NewsPart)
  case class Result(newsId: String, words: Seq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerActor extends Actor with ActorLogging {

  import LemmatizerActor._

  def receive = {
    case Process(newsId, sentences, part) =>

      val result = sentences.map(Lemmatizer.lemma)
      sender ! Result(newsId, result, part)

  }

}