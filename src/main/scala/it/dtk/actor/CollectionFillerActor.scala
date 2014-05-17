package it.dtk.actor

import it.dtk.nlp.db.News
import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.nlp.db.News
import it.dtk.nlp.db.Word
import akka.actor.ActorRef
import scala.annotation.tailrec
import akka.actor.Props
import akka.routing.RoundRobinPool

object CollectionFillerActor {

  case class ProcessSingle(news: News, sender: ActorRef)
  case class Process(news: Seq[News], sender: ActorRef)
  case class Processed(newsProcessed: Seq[News], send: ActorRef)
  case class ProcessedSingle(news: News, send: ActorRef)
  
  def props = Props[CollectionFillerActor]
  
  def routerProps(nrOfInstances: Int = 5) =
    RoundRobinPool(nrOfInstances).props(props)
}

class CollectionFillerActor extends Actor with ActorLogging {

  import CollectionFillerActor._

  def receive = {
    
    case ProcessSingle(news,send) =>
      val procNews = process(news)
      sender ! ProcessedSingle(procNews,send)

    case Process(news, send) =>
      val processedNews = news.map(n => process(n))
      sender ! Processed(processedNews, send)
  }

  def process(news: News): News = {
    val crimeCollection = fillCollection(news.nlpTitle, "B-CRIME", "I-CRIME") ++ fillCollection(news.nlpSummary, "B-CRIME", "I-CRIME") ++
      fillCollection(news.nlpCorpus, "B-CRIME", "I-CRIME")
    val addressCollection = fillCollection(news.nlpTitle, "B-ADDRESS", "I-ADDRESS") ++ fillCollection(news.nlpSummary, "B-ADDRESS", "I-ADDRESS") ++
      fillCollection(news.nlpCorpus, "B-ADDRESS", "I-ADDRESS")
    val personCollection = fillCollection(news.nlpTitle, "B-PER", "I-PER") ++ fillCollection(news.nlpSummary, "B-PER", "I-PER") ++
      fillCollection(news.nlpCorpus, "B-PER", "I-PER")
    val locationCollection = fillCollection(news.nlpTitle, "B-CITY", "I-CITY") ++ fillCollection(news.nlpSummary, "B-CITY", "I-CITY") ++
      fillCollection(news.nlpCorpus, "B-CITY", "I-CITY")
    val dateCollection = fillCollection(news.nlpTitle, "B-DATE", "I-DATE") ++ fillCollection(news.nlpSummary, "B-DATE", "I-DATE") ++
      fillCollection(news.nlpCorpus, "B-DATE", "I-DATE")
    val organizationCollection = fillCollection(news.nlpTitle, "B-ORG", "I-ORG") ++ fillCollection(news.nlpSummary, "B-ORG", "I-ORG") ++
      fillCollection(news.nlpCorpus, "B-ORG", "I-ORG")

    news.copy(crimes = Option(crimeCollection), locations = Option(locationCollection),
      addresses = Option(addressCollection), dates = Option(dateCollection),
      organizations = Option(organizationCollection), persons = Option(personCollection))
  }

  def fillCollection(words: Option[Seq[Word]], bEncoding: String, iEncoding: String): Seq[String] = {

    @tailrec
    def findCollection0(acc: Seq[String], lastElem: Option[String], words: Seq[Word]): Seq[String] = {

      if (words.isEmpty)
        if (lastElem.isDefined) acc.+:(lastElem.get) else acc
      else {
        val h = words.head
        val (list, newElem) = h match {
          //case is a bEncoding
          case w if (w.iobEntity.contains(bEncoding)) =>
            val list = if (lastElem.isDefined) acc.+:(lastElem.get) else acc
            val newElem = Option(h.lemma.getOrElse(h.token))
            (list, newElem) //return the element to add to the collection and the new element

          //case is a iEncoding
          case w if (w.iobEntity.contains(iEncoding)) =>
            val updateElem = lastElem.map(_ + " " + h.lemma.getOrElse(h.token))
            (acc, updateElem) //return the element to add to the collection and the new element

          case _ =>
             val list = if (lastElem.isDefined) acc.+:(lastElem.get) else acc
            (list, Option.empty[String])
        }
        findCollection0(list, newElem, words.tail)
      }
    }

    if (words.isEmpty)
      Seq.empty[String]
    else {
      val filteredWords = words.get.filter(_.iobEntity.size > 0)
      findCollection0(Seq.empty[String], Option.empty[String], filteredWords).reverse
    }
  }

  def fillCollection2(sentence: Seq[Word], bEncoding: String, eEncoding: String): Seq[String] = {

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