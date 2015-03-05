package it.wheretolive.nlp.db

import com.sksamuel.elastic4s.{ElasticsearchClientUri, ElasticClient}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.ObjectSource
import it.wheretolive.nlp.Model._
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.MatchQueryBuilder
import org.json4s.{DefaultFormats, _}
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future
import scala.concurrent.duration._
import org.elasticsearch.common.settings.ImmutableSettings


/**
 * Created by fabiofumarola on 11/01/15.
 */
trait ElasticSearchConnection {

  def host: String

  def port: Int

  def clusterName: Option[String]

  val settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName.getOrElse("")).build()
  val uri = ElasticsearchClientUri(s"elasticsearch://$host:$port")
  val client = ElasticClient.remote(settings,uri)
}

trait GeodataGFossIndex extends ElasticSearchConnection {

  implicit val formats = DefaultFormats
  // Brings in default date formats etc.
  implicit val maxDuration = 4.second

  import scala.concurrent.ExecutionContext.Implicits.global

  def documentPath: String

  //TODO change to future
  def searchLocation(name: String, maxCount: Int = 1): Array[Location]= {
      val result = client.execute {
        search in documentPath limit maxCount query {
          matchQuery("city_name", name).operator(MatchQueryBuilder.Operator.AND)
        }
      }.map(_.getHits.hits().map(r => parse(r.getSourceAsString).extract[Location]))

      result.await
  }
}

trait WheretoliveNewsIndex extends ElasticSearchConnection {

  def documentPath: String

  def indexNews(news: IndexedNews, key: String): Future[IndexResponse] = {
    client.execute {
      index into documentPath doc ObjectSource(news) id key
    }
  }

  def indexNews(news: IndexedNewsFlatten, key: String): Future[IndexResponse] = {
    client.execute {
      index into documentPath doc ObjectSource(news) id key
    }
  }
}
