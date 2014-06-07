package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.DateDetector
import akka.actor.Props
import scala.util._
import akka.routing.RoundRobinPool
import org.joda.time.DateTime
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.db.Word
import java.net.URL
import it.dtk.nlp.detector.DateDetector

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

  case class Process(newsId: String, words: Seq[Word], value: NewsPart)
  case class Result(newsId: String, words: Seq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DateDetectorActor extends Actor with ActorLogging {

  import DateDetectorActor._

  val detector = DateDetector

  def receive = {

    case Process(newsId, words, part) =>
      log.info("START DateDetectorActor {} with part {}", newsId, part)
      val send = sender
      val result = detector.detect(words)

      result match {
        case Success(sents) =>
          send ! Result(newsId, sents, part)
          log.info("END DateDetectorActor {} with part {}", newsId, part)

        case Failure(ex) =>
          send ! Failed(newsId, part, ex)
          log.info("END DateDetectorActor {} with part {}", newsId, part)
      }

    case ExtractDate(url) =>
      val result = detector.getDateFromURL(new URL(url))
      sender ! ExtractedDate(result)
  }

}