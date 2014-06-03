package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.TextPreprocessor
import akka.actor.Props
import it.dtk.nlp.detector.NewsPart._
import akka.routing.RoundRobinPool

object SentenceDetectorActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentences: Seq[String], value: NewsPart)

  def props = Props(classOf[SentenceDetectorActor])

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
class SentenceDetectorActor extends Actor with ActorLogging {

  import SentenceDetectorActor._
  
  val detector = new TextPreprocessor

  def receive = {

    case Process(newsId, text, Title) =>
      sender ! Result(newsId, detector.getSentences(text), Title)

    case Process(newsId, text, Summary) =>
      sender ! Result(newsId, detector.getSentences(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender ! Result(newsId, detector.getSentences(text), Corpus)

  }

}