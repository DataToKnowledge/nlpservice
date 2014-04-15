package it.dtk.actor

import akka.actor.{Actor, ActorLogging}
import it.dtk.actor.NewsPart._
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.TreeTagger
import it.dtk.nlp.db.Sentence
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object PosTaggerActor {
  case class Process(newsId: String, text: Sentence, value: NewsPart)
  case class Result(newsId: String, sentence: Sentence, value: NewsPart)
  case class Fail(newsId: String, ex: Throwable, value: NewsPart)

  def props = Props(classOf[PosTaggerActor])

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
class PosTaggerActor extends Actor with ActorLogging {

  import PosTaggerActor._

  def receive = {

    case Process(newsId, text, Title) =>
      TreeTagger(text) onComplete {
        case Success(res) =>
          sender() ! Result(newsId, res, Title)
        case Failure(ex) =>
          sender() ! Fail(newsId, ex, Title)
      }

  }
 
}
