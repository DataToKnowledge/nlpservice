package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.DateDetector
import akka.actor.Props
import it.dtk.nlp.detector.Detector
import scala.util._
import akka.routing.RoundRobinPool
import org.joda.time.DateTime

object DateDetectorActor {
  def props = Props(classOf[DateDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 10) =
    RoundRobinPool(nrOfInstances).props(props)
    
    case class ExtractDate(url: String)
    case class ExtractedDate(date: Option[DateTime])
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DateDetectorActor extends Actor with ActorLogging {

  def receive = {

    case Detector.Process(newsId, sentences, part) =>
      val result = DateDetector.detect(sentences)
      result match {
        case Success(sents) =>
          sender ! Detector.Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Detector.Failure(newsId, part, ex)
      }
  }

}