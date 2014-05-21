package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.CityDetector
import akka.actor.Props
import it.dtk.nlp.detector.Detector
import scala.util.Success
import scala.util.Failure
import akka.routing.RoundRobinPool

object CityDetectorActor {
  def props = Props(classOf[CityDetectorActor])

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
class CityDetectorActor extends Actor with ActorLogging {

  def receive = {

    case Detector.Process(newsId, sentences, part) =>
      val result = CityDetector.detect(sentences.toIndexedSeq)
      result match {
        case Success(sents) =>
          sender ! Detector.Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Detector.Failure(newsId, part, ex)
      }
  }

}