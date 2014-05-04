package it.dtk.actor

import it.dtk.nlp.db.News
import scala.actors.ActorRef
import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.nlp.db.News
import it.dtk.nlp.db.Word

object CollectionFillerActor {

  case class Process(news: Seq[News], sender: ActorRef)
  case class Processed(newsProcessed: Seq[News], send: ActorRef)
}

class CollectionFillerActor extends Actor with ActorLogging {

  import CollectionFillerActor._

  def receive = {

    case Process(news, send) =>
      val processedNews = Seq.empty[News]
      news.foreach { n => processedNews :+ process(n) }
      sender ! Processed(processedNews, send)
  }

  def process(news: News): News = {
    val crimeCollection = Seq.empty[String]
    val adressCollection = Seq.empty[String]
    val personCollection = Seq.empty[String]
    val locationCollection = Seq.empty[String]
    val dateCollection = Seq.empty[String]
    val organizzationCollection = Seq.empty[String]

    //crimeCollection :+ getCrimeCollection(news.nlpCorpus)
    if (news.nlpCorpus.isDefined)
      crimeCollection :+ getCrimeCollection(news.nlpCorpus.get)
    if (news.nlpSummary.isDefined)
      crimeCollection :+ getCrimeCollection(news.nlpSummary.get)
    if (news.nlpTitle.isDefined)
      crimeCollection :+ getCrimeCollection(news.nlpTitle.get)

    ???
  }

  def getCrimeCollection(sentence: Seq[Word]): Seq[String] = {
    val collection = Seq.empty[String]
    //val nlpCorpus = news.nlpCorpus.get
    var word: Option[String] = None
    sentence.foreach { f =>
      if (f.iobEntity.contains("B-CRIME")) {
        //se il lemma non esiste viene messo il token
        f.lemma match {
          case Some(lemma) => word = Some(lemma)
          case None => word = Some(f.token)
        }

      } else if (f.iobEntity.contains("I-CRIME")) {
        f.lemma match {
          case Some(lemma) => word.get.concat(" " + lemma)
          case None => word.get.concat(" " + f.token)
        }
      } else {
        word match {
          case Some(w) => {
            collection :+ w
            word = None
          }
          case None =>
        }

      }

    }
    collection
  }

}