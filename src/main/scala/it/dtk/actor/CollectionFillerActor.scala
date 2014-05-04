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

    //CRIME
    if (news.nlpCorpus.isDefined)
      crimeCollection = crimeCollection ++ fillCollection(news.nlpCorpus.get, "B-CRIME", "I-CRIME")
    if (news.nlpSummary.isDefined)
      crimeCollection = crimeCollection ++ fillCollection(news.nlpSummary.get, "B-CRIME", "I-CRIME")
    if (news.nlpTitle.isDefined)
      crimeCollection = crimeCollection ++ fillCollection(news.nlpTitle.get, "B-CRIME", "I-CRIME")
    //DATE
    if (news.nlpCorpus.isDefined)
      dateCollection = dateCollection ++ fillCollection(news.nlpCorpus.get, "B-DATE", "I-DATE")
    if (news.nlpSummary.isDefined)
      dateCollection = dateCollection ++ fillCollection(news.nlpSummary.get, "B-DATE", "I-DATE")
    if (news.nlpTitle.isDefined)
      dateCollection = dateCollection ++ fillCollection(news.nlpTitle.get, "B-DATE", "I-DATE")
    //ADDRESS
    if (news.nlpCorpus.isDefined)
      adressCollection = adressCollection ++ fillCollection(news.nlpCorpus.get, "B-ADDRESS", "I-ADDRESS")
    if (news.nlpSummary.isDefined)
      adressCollection = adressCollection ++ fillCollection(news.nlpSummary.get, "B-ADDRESS", "I-ADDRESS")
    if (news.nlpTitle.isDefined)
      adressCollection = adressCollection ++ fillCollection(news.nlpTitle.get, "B-ADDRESS", "I-ADDRESS")
    //CITY
    if (news.nlpCorpus.isDefined)
      locationCollection = locationCollection ++ fillCollection(news.nlpCorpus.get, "B-CITY", "I-CITY")
    if (news.nlpSummary.isDefined)
      locationCollection = locationCollection ++ fillCollection(news.nlpSummary.get, "B-CITY", "I-CITY")
    if (news.nlpTitle.isDefined)
      locationCollection = locationCollection ++ fillCollection(news.nlpTitle.get, "B-CITY", "I-CITY")

    //PERSON
    if (news.nlpCorpus.isDefined)
      personCollection = personCollection ++ fillCollection(news.nlpCorpus.get, "B-PER", "I-PER")
    if (news.nlpSummary.isDefined)
      personCollection = personCollection ++ fillCollection(news.nlpSummary.get, "B-PER", "I-PER")
    if (news.nlpTitle.isDefined)
      personCollection = personCollection ++ fillCollection(news.nlpTitle.get, "B-PER", "I-PER")
    //ORGANIZATION
    if (news.nlpCorpus.isDefined)
      organizzationCollection = organizzationCollection ++ fillCollection(news.nlpCorpus.get, "B-ORG", "I-ORG")
    if (news.nlpSummary.isDefined)
      organizzationCollection = organizzationCollection ++ fillCollection(news.nlpSummary.get, "B-ORG", "I-ORG")
    if (news.nlpTitle.isDefined)
      organizzationCollection = organizzationCollection ++ fillCollection(news.nlpTitle.get, "B-ORG", "I-ORG")

    news.copy(crimes = Option(crimeCollection), locations = Option(locationCollection),
      addresses = Option(adressCollection), dates = Option(dateCollection), 
      organizations = Option(organizzationCollection), persons=Option(personCollection))

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