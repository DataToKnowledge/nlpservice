package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.CrimeDetector
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.db.Word

object CrimeDetectorActor {
  case class Process(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Word], value: NewsPart)

  def props = Props(classOf[CrimeDetectorActor])

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
class CrimeDetectorActor extends Actor with ActorLogging {

  import CrimeDetectorActor._

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, CrimeDetector.detect(sentences), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, CrimeDetector.detect(sentences), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, CrimeDetector.detect(sentences), Corpus)

  }

}