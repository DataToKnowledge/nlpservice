package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.CrimeDetector
import akka.actor.Props
import akka.routing.RoundRobinRouter

object CrimeDetectorActor {
  def props = Props(classOf[CrimeDetectorActor])

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
class CrimeDetectorActor extends Actor with ActorLogging {

  import NlpController._

  def receive = {

    case DetectorProcess(newsId, sentences, Title) =>
      sender ! DetectorResult(newsId, CrimeDetector.detect(sentences), Title)

    case DetectorProcess(newsId, sentences, Summary) =>
      sender ! DetectorResult(newsId, CrimeDetector.detect(sentences), Summary)

    case DetectorProcess(newsId, sentences, Corpus) =>
      sender ! DetectorResult(newsId, CrimeDetector.detect(sentences), Corpus)

  }

}