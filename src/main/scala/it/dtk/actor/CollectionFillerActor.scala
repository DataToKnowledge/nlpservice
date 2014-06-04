package it.dtk.actor

import it.dtk.nlp.db.News
import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.nlp.db.Word
import akka.actor.ActorRef
import scala.annotation.tailrec
import akka.actor.Props
import akka.routing.RoundRobinPool
import it.dtk.nlp.detector.EntityType

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

    case ProcessSingle(news, send) =>
      val procNews = process(news)
      sender ! ProcessedSingle(procNews, send)

    case Process(news, send) =>
      val processedNews = news.map(n => process(n))
      sender ! Processed(processedNews, send)
  }

  def process(news: News): News = {

    val minLenght = (x: String) => x.length() > 3
    val acceptAll = (x: String) => true
    val startWithUpperCase = (x: String) => x(0).isUpper

    val modNlp = news.nlp.map { nlp =>

      val crimeCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_CRIME), EntityType.stringValue(EntityType.I_CRIME), minLenght, acceptAll) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_CRIME), EntityType.stringValue(EntityType.I_CRIME), minLenght, acceptAll) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_CRIME), EntityType.stringValue(EntityType.I_CRIME), minLenght, acceptAll) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_CRIME), EntityType.stringValue(EntityType.I_CRIME), minLenght, acceptAll)

      val addressCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_ADDRESS), EntityType.stringValue(EntityType.I_ADDRESS), minLenght, acceptAll) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_ADDRESS), EntityType.stringValue(EntityType.I_ADDRESS), minLenght, acceptAll) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_ADDRESS), EntityType.stringValue(EntityType.I_ADDRESS), minLenght, acceptAll) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_ADDRESS), EntityType.stringValue(EntityType.I_ADDRESS), minLenght, acceptAll)

      val personCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_PER), EntityType.stringValue(EntityType.I_PER), minLenght, startWithUpperCase) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_PER), EntityType.stringValue(EntityType.I_PER), minLenght, startWithUpperCase) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_PER), EntityType.stringValue(EntityType.I_PER), minLenght, startWithUpperCase) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_PER), EntityType.stringValue(EntityType.I_PER), minLenght, startWithUpperCase)

      val locationCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_CITY), EntityType.stringValue(EntityType.I_CITY), minLenght, startWithUpperCase) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_CITY), EntityType.stringValue(EntityType.I_CITY), minLenght, startWithUpperCase) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_CITY), EntityType.stringValue(EntityType.I_CITY), minLenght, startWithUpperCase) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_CITY), EntityType.stringValue(EntityType.I_CITY), minLenght, startWithUpperCase)

      val geopoliticalCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_GPE), EntityType.stringValue(EntityType.I_GPE), minLenght, startWithUpperCase) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_GPE), EntityType.stringValue(EntityType.I_GPE), minLenght, startWithUpperCase) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_GPE), EntityType.stringValue(EntityType.I_GPE), minLenght, startWithUpperCase) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_GPE), EntityType.stringValue(EntityType.I_GPE), minLenght, startWithUpperCase)

      val dateCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_DATE), EntityType.stringValue(EntityType.I_DATE), minLenght, acceptAll) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_DATE), EntityType.stringValue(EntityType.I_DATE), minLenght, acceptAll) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_DATE), EntityType.stringValue(EntityType.I_DATE), minLenght, acceptAll) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_DATE), EntityType.stringValue(EntityType.I_DATE), minLenght, acceptAll) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_DATE), EntityType.stringValue(EntityType.I_DATE), minLenght, acceptAll)

      val organizationCollection = fillCollection(nlp.title, EntityType.stringValue(EntityType.B_ORG), EntityType.stringValue(EntityType.I_ORG), minLenght, startWithUpperCase) ++
        fillCollection(nlp.summary, EntityType.stringValue(EntityType.B_ORG), EntityType.stringValue(EntityType.I_ORG), minLenght, startWithUpperCase) ++
        fillCollection(nlp.corpus, EntityType.stringValue(EntityType.B_ORG), EntityType.stringValue(EntityType.I_ORG), minLenght, startWithUpperCase) ++
        fillCollection(nlp.description, EntityType.stringValue(EntityType.B_ORG), EntityType.stringValue(EntityType.I_ORG), minLenght, startWithUpperCase)

      //crimes cannot contain locations and person
      //val cleanedCrimes = crimeCollection.filter(c => !locationCollection.exists(l => l.equals(c)))
      val cleanedCrimes = crimeCollection.diff(locationCollection.union(personCollection))

      //organizations cannot contain cities
      val cleanedOrganizations = organizationCollection.diff(locationCollection.union(personCollection))

      //persons cannot be organizations or cities
      val cleanedPersons = personCollection.diff(locationCollection)
      
      val cleanedGeopolicals = geopoliticalCollection.diff(personCollection.union(locationCollection))

      nlp.copy(crimes = Option(cleanedCrimes), locations = Option(locationCollection),
        addresses = Option(addressCollection), dates = Option(dateCollection),
        organizations = Option(cleanedOrganizations), persons = Option(cleanedPersons), geopoliticals = Option(cleanedGeopolicals))
    }

    news.copy(nlp = modNlp)
  }

  def fillCollection(words: Option[Seq[Word]], bEncoding: String, iEncoding: String,
    lenghtFilter: String => Boolean, valueFilter: String => Boolean): Seq[String] = {

    @tailrec
    def findCollection0(acc: Seq[String], current: Option[String], words: Seq[Word]): Seq[String] = {

      if (words.isEmpty)
        if (current.isDefined) acc.+:(current.get) else acc
      else {
        val h = words.head
        val (list, newElem) = h match {
          //case is a bEncoding
          case w if (w.iobEntity.contains(bEncoding)) =>
            val list = if (current.isDefined) acc.+:(current.get) else acc
            val newElem = Option(h.token)
            (list, newElem) //return the element to add to the collection and the new element

          //case is a iEncoding
          case w if (w.iobEntity.contains(iEncoding)) =>
            val updateElem = current.map(t => s"$t ${h.token}")
            (acc, updateElem) //return the element to add to the collection and the new element

          case _ =>
            val list = if (current.isDefined) acc.+:(current.get) else acc
            (list, Option.empty[String])
        }
        findCollection0(list, newElem, words.tail)
      }
    }

    val result = if (words.isEmpty)
      Seq.empty[String]
    else {
      val filteredWords = words.get.filter(_.iobEntity.size > 0)
      findCollection0(Seq.empty[String], Option.empty[String], filteredWords).reverse
    }

    result.filter(lenghtFilter).filter(valueFilter)
  }
}