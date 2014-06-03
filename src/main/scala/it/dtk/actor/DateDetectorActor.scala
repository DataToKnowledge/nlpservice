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

  case class Process(newsId: String, words: IndexedSeq[Word], value: NewsPart)
  case class Result(newsId: String, words: IndexedSeq[Word], value: NewsPart)
  case class Failed(newsId: String, part: NewsPart, ex: Throwable)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DateDetectorActor extends Actor with ActorLogging {

  import DateDetectorActor._
  
  def receive = {

    case Process(newsId, words, part) =>
      val result = DateDetector.detect(words)
      result match {
        case Success(sents) =>
          sender ! Result(newsId, sents, part)

        case Failure(ex) =>
          sender ! Failed(newsId, part, ex)
      }
      
    case ExtractDate(url) =>
      val result = DateDetector.getDateFromURL(new URL(url))
      sender ! ExtractedDate(result)
  }

}