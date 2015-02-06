package it.wheretolive.nlp.pipeline

import akka.actor.{ Props, Actor, ActorLogging }
import akka.routing.FromConfig
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.NewsPaperMapper
import it.wheretolive.nlp.db._
import it.wheretolive.nlp.pipeline.MessageProtocol._

import scala.util.{ Success, Failure }

object ElasticSearchIndexer {
  def props = Props[ElasticSearchIndexer]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 16/01/15.
 */
class ElasticSearchIndexer extends Actor with ActorLogging with RouteSlipFallible with WheretoliveNewsIndex with NewsPaperMapper {

  def conf = context.system.settings.config.getConfig("nlpservice.elasticsearch")
  override def host: String = conf.getString("host")
  override def port: Int = conf.getInt("port")
  override def documentPath: String = conf.getString("wheretolive.news")
  override def clusterName: Option[String] = Option(conf.getString("clusterName"))

  import context.dispatcher

  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val myself = self

      val newspaper = map(procNews.news.urlWebSite)

      val filteredEntities = procNews.namedEntities.map { ent =>

        ent.copy(
          crimes = ent.crimes.distinct,
          addresses = ent.addresses.distinct,
          persons = ent.persons.distinct,
          locations = ent.locations.distinct,
          geopoliticals = ent.geopoliticals.distinct,
          organizations = ent.organizations.distinct
        )
      }

      val newsToIndex = IndexedNews(
        newspaper = Option(newspaper),
        urlWebSite = procNews.news.urlWebSite,
        urlNews = procNews.news.urlNews,
        imageLink = procNews.news.topImage,
        title = procNews.news.title,
        summary = procNews.news.summary,
        corpus = procNews.news.corpus,
        focusDate = procNews.focusDate,
        focusLocation = procNews.focusLocation,
        namedEntities = filteredEntities,
        tags = procNews.tags
      )

      indexNews(newsToIndex).onComplete {

        case Success(res) =>
          sendMessageToNextTask(routeSlip, procNews.copy(indexId = Option(res.getId)))

        case Failure(ex) =>
          sendToEndTask(routeSlip, procNews, myself, ex)
      }
  }

}
