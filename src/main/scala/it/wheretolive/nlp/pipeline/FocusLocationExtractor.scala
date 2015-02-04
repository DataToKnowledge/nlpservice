package it.wheretolive.nlp.pipeline

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.FromConfig
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.pipeline.MessageProtocol.ProcessItem
import it.wheretolive.nlp.pipeline.detector.FocusLocationDetector
import org.joda.time.format.ISODateTimeFormat

object FocusLocationExtractor {
  def props = Props[FocusLocationExtractor]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 12/01/15.
 */
class FocusLocationExtractor extends Actor with ActorLogging with FocusLocationDetector with RouteSlipFallible {

  def conf = context.system.settings.config.getConfig("nlpservice.elasticsearch")
  override def host: String = conf.getString("host")
  override def port: Int = conf.getInt("port")
  override def clusterName: Option[String] = Option(conf.getString("clusterName"))
  override def documentPath: String = conf.getString("geodata.gfoss")


  override def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val myself = self

      try {
        val locations = detect(procNews.nlp.get)
        val focusLocation = locations.headOption.flatMap(_.locationEntry)
        val date = procNews.news.newsDate.map(_.toString(ISODateTimeFormat.basicDateTimeNoMillis()))

        sendMessageToNextTask(routeSlip, procNews.copy(focusLocation = focusLocation, focusDate = date))
      }catch {
        case ex: Throwable =>
          sendToEndTask(routeSlip,procNews,myself,ex)
      }
  }


}
