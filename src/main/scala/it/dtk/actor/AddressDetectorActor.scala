package it.dtk.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import scala.util.{ Success, Failure }
import it.dtk.nlp.detector._
import akka.routing.RoundRobinPool
import it.dtk.nlp.db.Word
import it.dtk.nlp.detector.NewsPart._

object AddressDetectorActor {
  def props = Props(classOf[AddressDetectorActor])

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
class AddressDetectorActor extends Actor with ActorLogging {

  import AddressDetectorActor._
  
  val detector = new AddressDetector
  
  def receive = {

    case Process(newsId, sentences, part) =>
      val result = detector.detect(sentences)

      result match {
        case Success(sents) =>
          sender ! Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Failed(newsId, part, ex)

      }
  }

}
