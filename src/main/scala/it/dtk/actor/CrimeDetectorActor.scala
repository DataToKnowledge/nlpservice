package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.CrimeDetector
import akka.actor.Props
import scala.util.Success
import scala.util.Failure
import akka.routing.RoundRobinPool
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.db.Word

object CrimeDetectorActor {
  def props = Props(classOf[CrimeDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)

  case class Process(newsId: String, words: Seq[Word], value: NewsPart)
  case class Result(newsId: String, words: Seq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CrimeDetectorActor extends Actor with ActorLogging {
  
  import CrimeDetectorActor._
  
  val detector = new CrimeDetector

  def receive = {

    case Process(newsId, words, part) =>
      val result = detector.detect(words)

      result match {
        case Success(sents) =>
          sender ! Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Failed(newsId, part, ex)
      }
  }

}