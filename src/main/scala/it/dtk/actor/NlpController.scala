package it.dtk.actor

import akka.actor.{ Actor, ActorLogging }
import it.dtk.nlp.db.News
import akka.actor.ActorRef

object NlpController {

  case class Process(news: Seq[News])
  case class Processed(news: Seq[News])
  case class FailedProcess(news: News)
}

object NewsPart extends Enumeration {
  type NewsPart = Value
  val Title, Summary, Corpus = Value
}

class NlpController extends Actor with ActorLogging {

  import NlpController._
  import NewsPart._
  
  //private var mapNews = Map.empty[String,News]
  
  def receive = waiting

  val waiting: Receive = {
    case Process(newsSeq) =>
      log.debug("start processing {} news", newsSeq.length)
      context.become(runNext(newsSeq, sender))
  }
  
  def runNext(newsSeq: Seq[News], send: ActorRef): Receive = {
      val setIds = newsSeq.map(_.id).toSet
      val mapNews = newsSeq.foldLeft(Map.empty[String,News])((map, news) => map + (news.id -> news)) 
      val send = sender
      //start pipeline
      newsSeq.foreach { n =>
        
      }
      running(setIds,mapNews,send)
  }
  
  def running(setIds: Set[String], mapNews: Map[String,News], send: ActorRef): Receive = {
    ???
  }

}