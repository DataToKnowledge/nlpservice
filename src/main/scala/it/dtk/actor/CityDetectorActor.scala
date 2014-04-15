package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.CityDetector
import akka.actor.Props
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

  import NlpController._

  def receive = {

    case DetectorProcess(newsId, sentences, Title) =>
      sender() ! DetectorResult(newsId, CityDetector.detect(sentences), Title)

    case DetectorProcess(newsId, sentences, Summary) =>
      sender() ! DetectorResult(newsId, CityDetector.detect(sentences), Summary)

    case DetectorProcess(newsId, sentences, Corpus) =>
      sender() ! DetectorResult(newsId, CityDetector.detect(sentences), Corpus)

  }

}