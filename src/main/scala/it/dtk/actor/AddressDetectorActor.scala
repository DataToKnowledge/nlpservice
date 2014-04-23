package it.dtk.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.util.Success
import scala.util.Failure
import it.dtk.nlp.detector._

object AddressDetectorActor {
  def props = Props(classOf[AddressDetectorActor])
  
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
class AddressDetectorActor extends Actor with ActorLogging {

  def receive = {

    case Detector.Process(newsId, sentences, part) =>
      val result = AddressDetector.detect(sentences)
      
      result match {
        case Success(sents) =>
          sender ! Detector.Result(newsId, sents , part)
          
        case Failure(ex) =>
          sender ! Detector.Failure(newsId,part,ex)
      }
  }
  
}