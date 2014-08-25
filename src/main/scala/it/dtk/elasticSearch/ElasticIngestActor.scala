package it.dtk.elasticSearch

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import ElasticIngestActor._
import com.sksamuel.elastic4s.ElasticClient
import it.dtk.nlp.db._
import com.sksamuel.elastic4s.ElasticDsl._
import akka.routing.RoundRobinPool
import scala.util.{ Failure, Success }
import com.sksamuel.elastic4s.source.ObjectSource
import com.typesafe.config.ConfigFactory

object ElasticIngestActor {

  type Host = String
  type Port = Int

  def props(node: (Host, Port), indexDocumentPath: String, webServiceAddress: String) =
    Props(classOf[ElasticIngestActor], node, indexDocumentPath, webServiceAddress)

  def routerProps(nrOfInstances: Int = 5, node: (Host, Port), indexDocumentPath: String, webServiceAddress: String) =
    RoundRobinPool(nrOfInstances).props(props(node, indexDocumentPath, webServiceAddress))

  case class Index(news: News)
  case class Indexed(id: String)
  case class ErrorIndexing(title: String, e: Throwable)
  case class ErrorConvertingNews(news: News)
}

class ElasticIngestActor(node: (Host, Port), indexDocumentPath: String, geocodingCacheAddress: String) extends Actor with ActorLogging {

  val indexUtil = new IndexingUtils(geocodingCacheAddress)

  implicit val executor = context.dispatcher

  val client = ElasticClient.remote(node)

  def receive = {

    case Index(news) =>

      val send = sender
      indexUtil.newsToNewsEs(news) match {

        case Some(n) =>
          val result = client.execute {
            index into indexDocumentPath doc ObjectSource(n) id news.id
          }

          result.onComplete {
            case Success(v) =>
              send ! Indexed(v.getId())

            case Failure(e) =>
              send ! ErrorIndexing(n.title.getOrElse(""), e)
          }

        case None =>
          send ! ErrorConvertingNews(news)
      }

  }
}

object ElasticReceptionist {

  def props = Props(classOf[ElasticReceptionist])
  case object Start
  case class Finished(count: Int)
}

class ElasticReceptionist extends Actor with ActorLogging {

  import context._
  import ElasticReceptionist._
  import ElasticIngestActor._
  import MongoDBMapper._

  val conf = ConfigFactory.load("elastic");
  val geoCondingCacheUrl = conf.getString("akka.nlp.geocodingCache.url")
  val node = (conf.getString("akka.nlp.elasticSearch.host"), conf.getInt("akka.nlp.elasticSearch.port"))
  val indexDocumentPath = conf.getString("akka.nlp.elasticSearch.path")
  val batchSize = conf.getInt("akka.nlp.batch.size") * 10
  val dbHost = conf.getString("akka.nlp.dbHost")

  val routerIndexer = actorOf(ElasticIngestActor.routerProps(5, node, indexDocumentPath, geoCondingCacheUrl))
  val dbManager = new DBManager(dbHost)
  val newsCollection = dbManager.nlpNewsIterator(batchSize)

  var countRunning = 0
  var countIndexed = 0

  def receive = {

    case Start =>
      log.info("{} indexed news", countIndexed)
      1 until 50 foreach { i =>
        if (newsCollection.hasNext) {
          routerIndexer ! Index(dBOToNews(newsCollection.next()))
          countRunning += 1
          countIndexed += 1
        }
      }

      if (countRunning == 0) {
        log.info("Successfully indexed {} news", countIndexed)
        context.system.shutdown()
      }

    case Indexed(id) =>
      log.info("indexed news with id {}", id)
      decrement()

    case ErrorIndexing(title, ex) =>
      log.error("error processing news with title {} with exception {}", title, ex.getStackTrace().mkString(" "))
      decrement()

    case ErrorConvertingNews(news) =>
      log.error("error converting news with id {}", news.id)
      decrement()

  }

  def decrement(): Unit = {
    countRunning -= 1
    if (countRunning == 0)
      self ! Start
  }
}