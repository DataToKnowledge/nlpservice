package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.Sentence
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.DateDetector
import akka.actor.Props
import akka.routing.RoundRobinPool

object DateDetectorActor {
  case class Process(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], value: NewsPart)

  def props = Props(classOf[DateDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 10) =
    RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DateDetectorActor extends Actor with ActorLogging {

  import DateDetectorActor._

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(DateDetector.detect), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(DateDetector.detect), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(DateDetector.detect), Corpus)

  }

}