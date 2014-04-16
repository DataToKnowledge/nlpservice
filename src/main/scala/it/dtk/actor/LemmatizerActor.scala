package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.Lemmatizer
import akka.actor.Props
import it.dtk.nlp.db.Word
import akka.routing.RoundRobinRouter
import it.dtk.nlp.detector.Detector

object LemmatizerActor {

  def props = Props(classOf[LemmatizerActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    props.withRouter(RoundRobinRouter(nrOfInstances = nrOfInstances))
  //TODO akka 2.3.2
  //RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class LemmatizerActor extends Actor with ActorLogging {

  import LemmatizerActor._

  def receive = {
    case Detector.Process(newsId, sentences, part) =>
      
      val result = sentences.map(Lemmatizer.lemma)
      sender ! Detector.Result(newsId, result, part)

  }

}