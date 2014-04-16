package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.CityDetector
import akka.actor.Props
import akka.routing.RoundRobinRouter
import it.dtk.nlp.detector.Detector
import scala.util.Success
import scala.util.Failure

object CityDetectorActor {
  def props = Props(classOf[CityDetectorActor])

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
class CityDetectorActor extends Actor with ActorLogging {

  import NlpController._

  def receive = {

    case Detector.Process(newsId, sentences, part) =>
      val result = CityDetector.detect(sentences)
      result match {
        case Success(sents) =>
          sender ! Detector.Result(newsId, sents , part)
          
        case Failure(ex) =>
          sender ! Detector.Failure(newsId,part,ex)
      }
  }

}