package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import akka.actor.Props
import akka.routing.RoundRobinPool

object PosTaggerActor {

  def props = Props(classOf[PosTaggerActor])

  /**
   * @param nrOfInstances
   * @return the props for a router with a defined number of instances
   */
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)
}

class PosTaggerActor extends Actor with ActorLogging {

  def receive = ???

}