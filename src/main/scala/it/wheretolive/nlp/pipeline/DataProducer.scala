package it.wheretolive.nlp.pipeline

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import it.wheretolive.nlp.db._

import scala.concurrent.Await
import scala.concurrent.duration._

object DataProducer {

  def props() = Props(classOf[DataProducer])
}

/**
 * Created by fabiofumarola on 08/01/15.
 */
class DataProducer extends Actor with ActorLogging with CrawledNewsMongoCollection {

  import MessageProtocol._

  def conf = context.system.settings.config.getConfig("nlpservice.mongo")
  override def host = conf.getString("host")
  override def port = conf.getInt("port")
  override def dbName = conf.getString("dbName")
  override def username = conf.getString("username")
  override def password = conf.getString("password")
  override def collectionName = conf.getString("crawledNews")

  val batchSize = conf.getInt("batchSize")
  val waitTime = 10.seconds

  override def receive: Receive = {
    case FetchData(indexed, processing) =>
      throttleDown()
      val data = fetchBatch(indexed,processing, batchSize)
      //set as processing
      data.foreach(d => setProcessing(d.id,true))

      sender ! Data(data)
  }

  def throttleDown(): Unit = {
    implicit val timeout = Timeout(waitTime)
    val eventuallyLoad = context.parent ? GetLoad
    try {
      val load = Await.result(eventuallyLoad, waitTime).asInstanceOf[Int]

      if (load > batchSize) {
        Thread.sleep(waitTime.toMillis)
        throttleDown()
      }
    }
    catch {
      case ex: Exception =>
        throttleDown()
    }

  }
}

