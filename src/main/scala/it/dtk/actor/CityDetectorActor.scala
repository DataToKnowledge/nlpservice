package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.Sentence
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.CityDetector
import akka.actor.Props
import akka.routing.RoundRobinPool

object CityDetectorActor {
  case class Process(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], value: NewsPart)

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

  import CityDetectorActor._

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(CityDetector.detect), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(CityDetector.detect), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(CityDetector.detect), Corpus)

  }

}