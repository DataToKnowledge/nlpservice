package it.wheretolive.nlp.pipeline

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.FromConfig
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.detector.TextProNlpDetector
import it.wheretolive.nlp.pipeline.MessageProtocol._

import scala.util.{ Failure, Success }

object TextPro {

  def props() = Props[TextPro]

  /**
   * the name should be TextProRouter
   * @return
   */
  def routerProps() =
    FromConfig.props(props)

}

/**
 *
 */
class TextPro extends Actor with ActorLogging with TextProNlpDetector with RouteSlip with RouteSlipFallible {

  def config = context.system.settings.config

  override def textProPath = config.getString("nlpservice.textproPath")

  def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val nlpTry = process(procNews.news)
      val mySelf = self

      nlpTry match {
        case Success(result) =>
          sendMessageToNextTask(routeSlip, procNews.copy(
            nlp = Option(result._1),
            tags = Option(result._2))
          )

        case Failure(ex) =>
          ex.printStackTrace()
          sendToEndTask(routeSlip, procNews, mySelf, ex)
      }
  }
}