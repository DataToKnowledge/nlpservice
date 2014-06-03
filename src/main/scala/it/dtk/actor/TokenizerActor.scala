package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.detector.NewsPart._
import it.dtk.nlp.TextPreprocessor
import akka.actor.Props
import it.dtk.nlp.db.Word
import akka.routing.RoundRobinPool

object TokenizerActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentence: Seq[Word], value: NewsPart)

  def props = Props(classOf[TokenizerActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TokenizerActor extends Actor with ActorLogging {

  import TokenizerActor._
  
  val detector = new TextPreprocessor
  
  def receive = {

    case Process(newsId, text, Title) =>
      sender ! Result(newsId, detector.getTokens(text), Title)

    case Process(newsId, text, Summary) =>
      sender ! Result(newsId, detector.getTokens(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender ! Result(newsId, detector.getTokens(text), Corpus)

  }

}