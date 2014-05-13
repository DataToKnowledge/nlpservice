package it.dtk.actor

import it.dtk.nlp.db.DBManager
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import scala.Array.canBuildFrom

object NlpReceptionist {
  case object Start
  case object Pause
  case object Stop
  case class Finished(processedNews: Int)

  def props(dbHost: String) = Props(classOf[NlpReceptionist], dbHost)
}

/**
 *
 * @param dbHost
 */
class NlpReceptionist(dbHost: String) extends Actor with ActorLogging {

  import NlpReceptionist._

  //db configurations
  def db = "dbNews"

  var processed = 0

  val nlpControllerActor = context.actorOf(NlpController.props(), "nlpController")
  
  DBManager.dbHost = dbHost

  val newsIterator = DBManager.iterateOverNews(1)

  def receive = {
    case Start =>
      log.info("Using MongoDB instance on {}", dbHost)
      //process the news 10 by 10
      val newsSeq = newsIterator.next

      if (newsSeq.isEmpty)
        context.parent ! Finished(processed)
      else
        nlpControllerActor ! NlpController.Process(newsSeq)

    case NlpController.Processed(newsSeq) =>
      //save the news in the db collection nlpNews
      newsSeq.foreach { n =>
        try {
          DBManager.saveNlpNews(n)
        } catch {
          case ex: Throwable =>
            ex.printStackTrace()
        }

        log.info("save news with id {} and title {}", n.id, n.title)
      }

      processed += newsSeq.size
      //process the next ten news
      self ! Start

    case NlpController.FailedProcess(news, ex) =>
      //save a reference to the news and the error in a log file
      val stacktraceString = ex.getStackTrace.map(_.toString).mkString(" ")
      log.error("failed process news with id {} title {} and stacktrace {}", news.id,
        news.title.getOrElse("no title"), stacktraceString)

    case NlpController.FailedProcessPart(newsId, part, ex) =>
      val stacktraceString = ex.getStackTrace.map(_.toString).mkString(" ")
      log.error("failed process news part {} with id {} and stacktrace {}", part, newsId,
        stacktraceString)

    case NlpController.FailParsingTextProResult(ex) =>
      log.error("error processing textpro " + ex.getMessage())

    case Pause =>

    case Stop =>
  }
}
