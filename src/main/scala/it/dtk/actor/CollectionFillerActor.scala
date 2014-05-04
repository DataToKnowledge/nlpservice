package it.dtk.actor

import it.dtk.nlp.db.News
import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.nlp.db.News
import it.dtk.nlp.db.Word
import akka.actor.ActorRef

object CollectionFillerActor {

  case class Process(news: Seq[News], sender: ActorRef)
  case class Processed(newsProcessed: Seq[News], send: ActorRef)
}

class CollectionFillerActor extends Actor with ActorLogging {

  import CollectionFillerActor._

  def receive = {

    case Process(news, send) =>
      val processedNews = news.map(n => process(n))
      sender ! Processed(processedNews, send)
  }

  def process(news: News): News = {
    var crimeCollection = Seq.empty[String]
    var adressCollection = Seq.empty[String]
    var personCollection = Seq.empty[String]
    var locationCollection = Seq.empty[String]
    var dateCollection = Seq.empty[String]
    var organizzationCollection = Seq.empty[String]

    //crimeCollection :+ getCrimeCollection(news.nlpCorpus)
    if (news.nlpCorpus.isDefined) {
      crimeCollection :+ fillCollection(news.nlpCorpus.get,"B-CRIME","I-CRIME")
      
    }
//
//    if (news.nlpSummary.isDefined)
//      crimeCollection :+ fillCollection(news.nlpSummary.get)
//    if (news.nlpTitle.isDefined)
//      crimeCollection :+ fillCollection(news.nlpTitle.get)

    ???
  }

  def fillCollection(sentence: Seq[Word], bEncoding: String, eEncoding: String): Seq[String] = {

    var collection = Seq.empty[String]
    //val nlpCorpus = news.nlpCorpus.get
    var word: Option[String] = None
    sentence.foreach { f =>
      if (f.iobEntity.contains(bEncoding)) {
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
            collection = collection :+ w
            word = None
          }
          case None =>
        }

      }

    }
    collection
  }

}