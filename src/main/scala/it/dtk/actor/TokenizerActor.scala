package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import it.dtk.nlp.TextPreprocessor
import akka.actor.Props
import it.dtk.nlp.db.Word
import akka.routing.RoundRobinRouter

object TokenizerActor {
  case class Process(newsId: String, text: String, value: NewsPart)
  case class Result(newsId: String, sentence: Seq[Word], value: NewsPart)

  def props = Props(classOf[TokenizerActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    props.withRouter(RoundRobinRouter(nrOfInstances = nrOfInstances))
  //TODO akka 2.3.2
  //RoundRobinPool(nrOfInstances).props(props)
}

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class TokenizerActor extends Actor with ActorLogging {

  import TokenizerActor._

  def receive = {

    case Process(newsId, text, Title) =>
      sender ! Result(newsId, TextPreprocessor.getTokens(text), Title)

    case Process(newsId, text, Summary) =>
      sender ! Result(newsId, TextPreprocessor.getTokens(text), Summary)

    case Process(newsId, text, Corpus) =>
      sender ! Result(newsId, TextPreprocessor.getTokens(text), Corpus)

  }

}