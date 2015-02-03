package it.wheretolive.nlp.pipeline

import akka.actor.{Props, Actor, ActorLogging}
import akka.routing.FromConfig
import it.wheretolive.akka.pattern.{RouteSlip, RouteSlipFallible, RouteSlipMessage}
import it.wheretolive.nlp.pipeline.MessageProtocol.ProcessItem
import it.wheretolive.nlp.pipeline.detector.NaiveCrimeDetector

object CrimesExtractor {

  def props = Props[CrimesExtractor]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 11/01/15.
 */
class CrimesExtractor extends Actor with ActorLogging with RouteSlipFallible with NaiveCrimeDetector {

  def conf = context.system.settings.config.getConfig("nlpservice.mongo")
  override def host = conf.getString("host")
  override def port = conf.getInt("port")
  override def username: String = conf.getString("username")
  override def password: String = conf.getString("password")
  override def dbName = conf.getString("dbName")
  override def collectionName = conf.getString("crimes")

  override def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      try {
        val nlp = procNews.nlp.get

        val wordsTitle = detect(nlp.title)
        val wordsSummary = detect(nlp.summary)
        val wordCorpus = detect(nlp.corpus)

        val procCopy = procNews.copy(
          nlp = Option(nlp.copy(
            title = wordsTitle,
            summary = wordsSummary,
            corpus = wordCorpus)))

        sendMessageToNextTask(routeSlip, procCopy)
      }
      catch {
        case ex: Throwable =>
          ex.printStackTrace()
          sendToEndTask(routeSlip, procNews, self, ex)
      }

  }

}
