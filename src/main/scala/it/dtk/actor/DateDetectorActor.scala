package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.detector.DateDetector
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.db.Word

object DateDetectorActor {
  case class Process(newsId: String, sentences: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Word], value: NewsPart)

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
      sender() ! Result(newsId, DateDetector.detect(sentences), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, DateDetector.detect(sentences), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, DateDetector.detect(sentences), Corpus)

  }

}