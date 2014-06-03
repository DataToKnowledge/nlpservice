package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.CityDetector
import akka.actor.Props
import scala.util.Success
import scala.util.Failure
import akka.routing.RoundRobinPool
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.db.Word

object CityDetectorActor {
  def props = Props(classOf[CityDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)

  case class Process(newsId: String, words: IndexedSeq[Word], value: NewsPart)
  case class Result(newsId: String, words: IndexedSeq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetectorActor extends Actor with ActorLogging {
  
  import CityDetectorActor._

  def receive = {

    case Process(newsId, word, part) =>
      val result = CityDetector.detect(word)
      result match {
        case Success(sents) =>
          sender ! Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Failed(newsId, part, ex)
      }
  }

}