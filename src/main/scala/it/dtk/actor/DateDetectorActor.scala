package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.DateDetector
import akka.actor.Props
import akka.routing.RoundRobinRouter

object DateDetectorActor {
  def props = Props(classOf[DateDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 10) =
    props.withRouter(RoundRobinRouter(nrOfInstances = nrOfInstances))
  //TODO akka 2.3.2
  //RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DateDetectorActor extends Actor with ActorLogging {

  import NlpController._

  def receive = {

    case DetectorProcess(newsId, sentences, Title) =>
      sender ! DetectorResult(newsId, DateDetector.detect(sentences), Title)

    case DetectorProcess(newsId, sentences, Summary) =>
      sender ! DetectorResult(newsId, DateDetector.detect(sentences), Summary)

    case DetectorProcess(newsId, sentences, Corpus) =>
      sender ! DetectorResult(newsId, DateDetector.detect(sentences), Corpus)

  }

}