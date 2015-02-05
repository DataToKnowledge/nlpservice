package it.wheretolive.nlp.pipeline

import akka.actor.{Props, Actor, ActorLogging}
import it.wheretolive.akka.pattern._
import it.wheretolive.nlp.db.CrawledNewsMongoCollection
import it.wheretolive.nlp.pipeline.MessageProtocol.ProcessItem

import scala.util.{Failure, Success}

object CrawledNewsSaver {

  def props = Props(classOf[CrawledNewsSaver])
}

/**
 * Created by fabiofumarola on 05/02/15.
 */
class CrawledNewsSaver extends Actor with ActorLogging with RouteSlipFallible with CrawledNewsMongoCollection {

  def conf = context.system.settings.config.getConfig("nlpservice.mongo")

  override def host = conf.getString("host")
  override def port = conf.getInt("port")
  override def dbName = conf.getString("dbName")
  override def username = conf.getString("username")
  override def password = conf.getString("password")
  override def collectionName = conf.getString("crawledNews")

  override def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val result = setAnalyzed(procNews.news.id)

      result match {
        case Success(res) =>
          sendMessageToNextTask(routeSlip,procNews)


        case Failure(ex) =>
          sendToEndTask(routeSlip,procNews,self,ex)
      }

  }


}
