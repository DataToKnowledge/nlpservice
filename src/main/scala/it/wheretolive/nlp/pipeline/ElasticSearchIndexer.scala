package it.wheretolive.nlp.pipeline

import akka.actor.{ Props, Actor, ActorLogging }
import akka.routing.FromConfig
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.db._
import it.wheretolive.nlp.pipeline.MessageProtocol._
import it.wheretolive.nlp.utils.AnalyzedNewsUtils

import scala.util.{ Success, Failure }

object ElasticSearchIndexer {
  def props = Props[ElasticSearchIndexer]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 16/01/15.
 */
class ElasticSearchIndexer extends Actor with ActorLogging with RouteSlipFallible with WheretoliveNewsIndex with AnalyzedNewsUtils {

  def conf = context.system.settings.config.getConfig("nlpservice.elasticsearch")
  override def host: String = conf.getString("host")
  override def port: Int = conf.getInt("port")
  override def documentPath: String = conf.getString("wheretolive.news")
  override def clusterName: Option[String] = Option(conf.getString("clusterName"))

  import context.dispatcher

  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val myself = self

      searchByTitle(procNews.news.title).map(_.getHits.totalHits()).onComplete {
        case Success(numHits) =>

          if (numHits > 0)
            sendMessageToNextTask(routeSlip, procNews.copy(indexId = Option(procNews.news.id)))
          else {
            val newsToIndex = extractNewsToIndexFlatten(procNews)

            indexNews(newsToIndex, procNews.news.id).onComplete {

              case Success(res) =>
                sendMessageToNextTask(routeSlip, procNews.copy(indexId = Option(res.getId)))

              case Failure(ex) =>
                sendToEndTask(routeSlip, procNews, myself, ex)
            }
          }

        case Failure(ex) =>
          val newsToIndex = extractNewsToIndexFlatten(procNews)

          indexNews(newsToIndex, procNews.news.id).onComplete {

            case Success(res) =>
              sendMessageToNextTask(routeSlip, procNews.copy(indexId = Option(res.getId)))

            case Failure(ex) =>
              sendToEndTask(routeSlip, procNews, myself, ex)
          }

      }

  }

}
