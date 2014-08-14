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
      log.info("START AddressDetect {} with part {}", newsId, part)
      val send = sender
      val result = detector.detect(sentences)

      result match {
        case Success(sents) =>
          send ! Result(newsId, sents, part)
          log.info("END AddressDetect {} with part {}", newsId, part)

        case Failure(ex) =>
          send ! Failed(newsId, part, ex)
          log.info("END AddressDetect {} with part {}", newsId, part)

      }
  }

}
