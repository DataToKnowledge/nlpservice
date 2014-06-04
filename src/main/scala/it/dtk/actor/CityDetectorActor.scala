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

  case class Process(newsId: String, words: Seq[Word], value: NewsPart)
  case class Result(newsId: String, words: Seq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class CityDetectorActor extends Actor with ActorLogging {

  import CityDetectorActor._

  val detector = new CityDetector

  def receive = {

    case Process(newsId, word, part) =>
      log.info("START CityDetectorActor {} with part {}", newsId, part)
      val send = sender
      val result = detector.detect(word)

      result match {
        case Success(sents) =>
          send ! Result(newsId, sents, part)
          log.info("END CityDetectorActor {} with part {}", newsId, part)

        case Failure(ex) =>
          send ! Failed(newsId, part, ex)
          log.info("END CityDetectorActor {} with part {}", newsId, part)
      }
  }

}