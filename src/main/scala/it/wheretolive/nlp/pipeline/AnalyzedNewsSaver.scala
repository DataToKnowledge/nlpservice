package it.wheretolive.nlp.pipeline

import akka.actor._
import akka.routing.FromConfig
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.db.AnalyzedNewsMongoCollection
import it.wheretolive.nlp.pipeline.MessageProtocol.ProcessItem
import org.joda.time.format.ISODateTimeFormat

object AnalyzedNewsSaver {
  def props = Props[AnalyzedNewsSaver]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 02/02/15.
 */
class AnalyzedNewsSaver extends Actor with ActorLogging with AnalyzedNewsMongoCollection with RouteSlipFallible {

  def conf = context.system.settings.config.getConfig("nlpservice.mongo")

  override def host = conf.getString("host")

  override def port = conf.getInt("port")

  override def dbName = conf.getString("dbName")

  override def username = conf.getString("username")

  override def password = conf.getString("password")

  override def collectionName = conf.getString("analyzedNews")

  override def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val myself = self

      try {

        val analyzedNews = AnalyzedNews(
          news = procNews.news,
          nlp = procNews.nlp,
          namedEntities = procNews.namedEntities,
          tags = procNews.tags,
          focusLocation = procNews.focusLocation,
          focusDate = procNews.news.newsDate.map(_.toString(ISODateTimeFormat.basicDateTime()))
        )

        val result = save(analyzedNews)
        sendMessageToNextTask(routeSlip, procNews.copy(analyzedNewsSaved = Option(result)))
      }
      catch {
        case ex: Throwable =>
          sendToEndTask(routeSlip, procNews, myself, ex)
      }
  }

}
