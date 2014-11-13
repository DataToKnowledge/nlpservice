package it.dtk.elasticSearch

import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor._
import akka.routing.RoundRobinPool
import com.mongodb.casbah.MongoCursorBase
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.ObjectSource
import com.typesafe.config.ConfigFactory
import it.dtk.elasticSearch.ElasticIngestActor._
import it.dtk.nlp.db._
import it.dtk.nlp.db.MongoDBMapper._
import scala.language.postfixOps

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

class ElasticIngestActor(node: (Host, Port), indexDocumentPath: String, geocodingcacheAddress: String) extends Actor with ActorLogging {

  val indexUtil = new IndexingUtils(geocodingcacheAddress)

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

  case object Reindex

  case object Index

  case class Finished(count: Int)

}

class ElasticReceptionist extends Actor with ActorLogging {

  import it.dtk.elasticSearch.ElasticReceptionist._
  import context.dispatcher

  val conf = ConfigFactory.load("elastic");
  val geoCondingCacheUrl = conf.getString("akka.nlp.geocodingCache.url")
  val node = (conf.getString("akka.nlp.elasticSearch.host"), conf.getInt("akka.nlp.elasticSearch.port"))
  val indexDocumentPath = conf.getString("akka.nlp.elasticSearch.path")
  val batchSize = conf.getInt("akka.nlp.batch.size") * 10
  val dbHost = conf.getString("akka.nlp.dbHost")
  val schedulingTime = conf.getInt("akka.nlp.scheduler.time")

  val routerIndexer = context.actorOf(ElasticIngestActor.routerProps(1, node, indexDocumentPath, geoCondingCacheUrl))
  val dbManager = new DBManager(dbHost)


  def receive = {

    case Reindex =>
      val newsIterator = dbManager.nlpNewsIterator(batchSize)
      val worker = context.actorOf(ElasticIndexerWorker.props(dbManager, newsIterator, routerIndexer))
      worker ! ElasticIndexerWorker.Start

    case Index =>
      val newsIterator = dbManager.nlpNewsIteratorNotIndexed(batchSize)
      val worker = context.actorOf(ElasticIndexerWorker.props(dbManager, newsIterator, routerIndexer))
      worker ! ElasticIndexerWorker.Start


    case ElasticIndexerWorker.Finished(indexedNews) =>
      log.info("Indexed {} news", indexedNews)
      log.info("Scheduling the next iteration in {} minutes", schedulingTime)
      //kill the actor and start a news one
      sender ! PoisonPill
      context.system.scheduler.scheduleOnce(schedulingTime minutes, self, Index)

  }

}

object ElasticIndexerWorker {

  def props(dbManager: DBManager, newsIterator: MongoCursorBase, routerIndexer: ActorRef) =
    Props(classOf[ElasticIndexerWorker], dbManager, newsIterator, routerIndexer)

  case object Start

  case class Finished(countIndexed: Int)

}

class ElasticIndexerWorker(dbManager: DBManager, newsIterator: MongoCursorBase, routerIndexer: ActorRef) extends Actor with ActorLogging {

  import it.dtk.elasticSearch.ElasticIndexerWorker._
  import context.dispatcher

  var countRunning = 0
  var countIndexed = 0

  override def receive: Receive = {

    case Start =>
      var time = 4
      1 until 50 foreach { i =>
        if (newsIterator.hasNext) {
          context.system.scheduler.scheduleOnce(time.second, routerIndexer, Index(dBOToNews(newsIterator.next())))
          time += 4
          countRunning += 1
          countIndexed += 1
        }
      }

      if (countRunning == 0) {
        log.info("Successfully indexed {} news", countIndexed)
        context.parent ! Finished(countIndexed)
      }

    case Indexed(id) =>
      log.info("indexed news with id {}", id)
      dbManager.setNlpNewsIndexed(id)
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