package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.Lemmatizer
import akka.actor.Props
import it.dtk.nlp.detector.Detector
import akka.routing.RoundRobinPool

object LemmatizerActor {

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

  def receive = {
    case Detector.Process(newsId, sentences, part) =>
      
      val result = sentences.map(Lemmatizer.lemma)
      sender ! Detector.Result(newsId, result, part)

  }

}