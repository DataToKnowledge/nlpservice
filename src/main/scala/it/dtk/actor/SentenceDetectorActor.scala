package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.TextPreprocessor
import akka.actor.Props
import akka.routing.RoundRobinRouter

object SentenceDetectorActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentences: Seq[String], value: NewsPart)

  def props = Props(classOf[SentenceDetectorActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 10) =
    props.withRouter(RoundRobinRouter(nrOfInstances = nrOfInstances))
  //TODO akka 2.3.2
  //RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class SentenceDetectorActor extends Actor with ActorLogging {

  import SentenceDetectorActor._

  def receive = {

    case Process(newsId, text, Title) =>
      sender ! Result(newsId, TextPreprocessor.getSentences(text), Title)

    case Process(newsId, text, Summary) =>
      sender ! Result(newsId, TextPreprocessor.getSentences(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender ! Result(newsId, TextPreprocessor.getSentences(text), Corpus)

  }

}