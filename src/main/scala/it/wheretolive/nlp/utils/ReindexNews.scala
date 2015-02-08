package it.wheretolive.nlp.utils

import akka.actor.{ActorSystem, Props, Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import it.wheretolive.nlp.db.{ WheretoliveNewsIndex, AnalyzedNewsMongoCollection }
import it.wheretolive.nlp.utils.AnalyzedNewsUtils
import scala.util._
import it.wheretolive.nlp.Model._

object MessageProtocol {

  object Process
  case class Fetch(batchSize: Int)
  case class Data(list: List[AnalyzedNews])
  case class Index(element: AnalyzedNews)
  case class SetIndexed(id: String)
  case class ESIndexed(id: String)
  case class ESNotIndexed(id: String, ex: Throwable)
}

object ReindexNews {

  def props = Props(classOf[ReindexNews])
}

/**
 * Created by fabiofumarola on 08/02/15.
 */
class ReindexNews extends Actor with ActorLogging {

  import MessageProtocol._

  val mongoDbWorker = context.actorOf(MongoDbWorker.props, name = "mongoDbWorker")
  val esWorker = context.actorOf(ElasticSearchWorker.props, name = "elasticsearchWorker")
  val batchSize = 50

  var counterIndexed = 0
  var counterError = 0
  var toProcess = 0

  override def receive: Actor.Receive = {

    case Process =>
      mongoDbWorker ! Fetch(batchSize)

    case Data(list) =>

      toProcess = list.size

      if (list.isEmpty) {
        log.info("Indexed {} news with {} errors", counterIndexed, counterError)
        context.stop(self)
      }
      else {
        list.foreach { n =>
          esWorker ! Index(n)
        }
      }

    case ESIndexed(id) =>
      counterIndexed += 1
      toProcess -= 1
      mongoDbWorker ! SetIndexed(id)
      continue

    case ESNotIndexed(id, ex) =>
      counterError += 1
      toProcess -= 1
      log.error("error processing new with id {} for error {}", id, ex)
      mongoDbWorker ! SetIndexed(id)
      continue

  }

  def continue: Unit = {
    if (toProcess == 0)
      self ! Process
  }
}

object ElasticSearchWorker {

  def props = Props(classOf[ElasticSearchWorker])
}

class ElasticSearchWorker extends Actor with ActorLogging with WheretoliveNewsIndex with AnalyzedNewsUtils {

  import context.dispatcher
  import MessageProtocol._

  def conf = context.system.settings.config.getConfig("nlpservice.elasticsearch")
  override def host: String = conf.getString("host")
  override def port: Int = conf.getInt("port")
  override def documentPath: String = conf.getString("wheretolive.news")
  override def clusterName: Option[String] = Option(conf.getString("clusterName"))

  override def receive: Actor.Receive = {

    case Index(aNews) =>

      val send = sender

      val toIndexNews = extractNewsToIndex(aNews)

      indexNews(toIndexNews).onComplete {

        case Success(resp) =>
          send ! ESIndexed(aNews.id)

        case Failure(ex) =>
          send ! ESNotIndexed(aNews.id, ex)
      }
  }
}

object MongoDbWorker {

  def props = Props(classOf[MongoDbWorker])
}

class MongoDbWorker extends Actor with ActorLogging with AnalyzedNewsMongoCollection {

  import MessageProtocol._

  def conf = context.system.settings.config.getConfig("nlpservice.mongo")
  override def host = conf.getString("host")
  override def port = conf.getInt("port")
  override def dbName = conf.getString("dbName")
  override def username = conf.getString("username")
  override def password = conf.getString("password")
  override def collectionName = conf.getString("analyzedNews")

  val batchSize = 50

  override def receive: Receive = {
    case Fetch(size) =>
      sender ! Data(fetchBatch(false, size))

    case SetIndexed(id) =>
      val send = sender

      setIndexed(id) match {
        case Success(value) =>
          log.info("correctly set news {} as indexed",id)

        case Failure(ex) =>
          log.error("error processing news with id {} for error", id, ex)
      }
  }
}

class ReindexRunner extends App {
  import MessageProtocol._

  val config = ConfigFactory.load("nlpservice")
  val actorSystem = ActorSystem("ReindexRunner",config)

  val reindexActor = actorSystem.actorOf(ReindexNews.props,"reindexNews")
  reindexActor ! Process
}