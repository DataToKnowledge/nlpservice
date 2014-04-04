package it.dtk.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.actor.NewsPart._
import it.dtk.nlp.db.Sentence
import it.dtk.nlp.detector.AddressDetector
import akka.actor.Props
import akka.routing.RoundRobinPool

object AddressDetectorActor {
  case class Process(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  case class Result(newsId: String, sentences: Seq[Sentence], value: NewsPart)
  
  def props = Props(classOf[AddressDetectorActor])
  
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
class AddressDetectorActor extends Actor with ActorLogging {

  import AddressDetectorActor._

  def receive = {

    case Process(newsId, sentences, Title) =>
      sender() ! Result(newsId, sentences.map(AddressDetector.detect), Title)

    case Process(newsId, sentences, Summary) =>
      sender() ! Result(newsId, sentences.map(AddressDetector.detect), Summary)

    case Process(newsId, sentences, Corpus) =>
      sender() ! Result(newsId, sentences.map(AddressDetector.detect), Corpus)

  }
  
}