package it.wheretolive.akka.pattern

import akka.actor.ActorRef

case class RouteSlipMessage(routeSlip: Seq[ActorRef], message: AnyRef)

/**
 * Created by fabiofumarola on 09/01/15.
 *
 * in this pattern the last ActorRef is the actor that will get the final message
 */
trait RouteSlip {

  def sendMessageToNextTask(routeSlip: Seq[ActorRef], message: AnyRef): Unit = {

    val nextTask = routeSlip.head
    val newSlip = routeSlip.tail

    if (newSlip.isEmpty) {
      nextTask ! message
    }
    else {
      nextTask ! RouteSlipMessage(routeSlip = newSlip, message = message)
    }
  }
}

/**
 * it is based on the idea that the last actor is the one in charge of processing the errors
 * @param originalMessage
 * @param failedTask
 * @param failure
 */
case class RouteSlipMessageFailure(originalMessage: AnyRef, failedTask: ActorRef, failure: Throwable)

trait RouteSlipFallible extends RouteSlip {
  def sendToEndTask(routeSlip: Seq[ActorRef], message: AnyRef, failedTask: ActorRef, failure: Throwable): Unit = {
    val lastTask = routeSlip.last
    lastTask ! RouteSlipMessageFailure(message, failedTask, failure)
  }
}
