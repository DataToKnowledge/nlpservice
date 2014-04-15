package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.actor.NewsPart._
import akka.actor.Props
import it.dtk.nlp.TreeTagger
import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global
import it.dtk.nlp.db.Word
import akka.routing.RoundRobinRouter

object PosTaggerActor {
  case class Process(newsId: String, text: Seq[Word], value: NewsPart)
  case class Result(newsId: String, sentence: Seq[Word], value: NewsPart)
  case class Fail(newsId: String, ex: Throwable, value: NewsPart)

  def props = Props(classOf[PosTaggerActor])

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
class PosTaggerActor extends Actor with ActorLogging {

  import PosTaggerActor._

  def receive = {

    case Process(newsId, text, Title) =>
      TreeTagger.tag(text) onComplete {
        case Success(res) =>
          sender ! Result(newsId, res, Title)
        case Failure(ex) =>
          sender ! Fail(newsId, ex, Title)
      }

  }

}
