package it.dtk.actornlp

import akka.actor.Actor
import akka.actor.ActorLogging
import it.dtk.nlp.db.News
import akka.actor.Props
import akka.routing.FromConfig
import it.dtk.actor.textpro.TextProActor
import it.dtk.actor._
import it.dtk.nlp.detector._
import it.dtk.nlp.db.Word
import akka.actor.ActorRef
import akka.actor.PoisonPill
import scala.util.Success
import it.dtk.nlp.detector.NewsPart._
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import org.joda.time.format.DateTimeFormat

object Controller {
  case class Process(news: News)
  case class Processed(news: News)
  case class FailProcess(newsId: String, ex: Throwable)

  def props() = Props[Controller]
}

class Controller extends Actor with ActorLogging {

  import Controller._

  var counter = 1L
  var mapNewsIdSender = Map.empty[String, ActorRef]

  //  val lemmatizerRouter = context.actorOf(LemmatizerActor.routerProps(), "lemmatizerRouter")
  //  val postagRouter = context.actorOf(PosTaggerActor.routerProps(), "postagRouter")
  //  val sentenceRouter = context.actorOf(SentenceDetectorActor.routerProps(), "sentenceDetectorRouter")
  //  val stemmerRouter = context.actorOf(StemmerActor.routerProps(), "stemmerRouter")
  //  val tokenizerActor = context.actorOf(TokenizerActor.routerProps(), "tokenizerRouter")

  val textProRouter = context.actorOf(FromConfig.props(Props[TextProActor]), "textProActorPool")
  val collectionFilterActor = context.actorOf(CollectionFillerActor.routerProps(5), "collectionFilterPool")

  def receive = {

    case Process(news) =>
      log.info("processing news with title {}", news.title.getOrElse(news.id))
      mapNewsIdSender += (news.id -> sender)
      textProRouter ! TextProActor.Parse(news)

    case TextProActor.Result(news) =>
      log.info("calling nlpActors for the news with title {}", news.title.getOrElse(news.id))
      val worker = context.actorOf(NamedEntitiesExtractor.props(news, counter), s"NamedEntityExtractor$counter")
      counter += 1

    case TextProActor.Fail(newsId, ex) =>
      val send = mapNewsIdSender.get(newsId)
      if (send.isDefined)
        send.get ! FailProcess(newsId, ex)
      else
        log.error("sender not defined for news {}", newsId)

    case TextProActor.FailProcessingLine(ex) =>
      log.info("error in TextPro parsing the result with exception {}", ex.getStackTrace().mkString("\t"))

    case NamedEntitiesExtractor.Processed(news) =>

      sender ! PoisonPill

      val send = mapNewsIdSender.get(news.id)
      if (send.isDefined)
        collectionFilterActor ! CollectionFillerActor.ProcessSingle(news, send.get)
      else
        log.error("sender not defined for news {}", news.id)

    case CollectionFillerActor.ProcessedSingle(news, send) =>
      //log.info("end processing news with title {}", news.title.getOrElse(news.id))
      send ! Processed(news)

  }
}

object NamedEntitiesExtractor {

  import it.dtk.nlp.detector.NewsPart._

  case class Processed(news: News)

  def props(news: News, id: Long) = Props(classOf[NamedEntitiesExtractor], news, id)
}

class NamedEntitiesExtractor(news: News, id: Long) extends Actor with ActorLogging {

  import NamedEntitiesExtractor._

  var processing = 0
  val processedNews = news
  var processedNlp = news.nlp.get
  val fmt = DateTimeFormat.forPattern("dd/MM/yyyy")

  context.setReceiveTimeout(60.seconds)

  val addressActor = context.actorOf(AddressDetectorActor.props, s"addressActor$id")
  val cityActor = context.actorOf(CityDetectorActor.props, s"cityActor$id")
  val crimeActor = context.actorOf(CrimeDetectorActor.props, s"crimeActor$id")
  val dateActor = context.actorOf(DateDetectorActor.props, s"dateActor$id")

  news.nlp.foreach { nlp =>

    nlp.title.foreach { title =>
      addressActor ! AddressDetectorActor.Process(news.id, title, NewsPart.Title)
      cityActor ! CityDetectorActor.Process(news.id, title, NewsPart.Title)
      crimeActor ! CrimeDetectorActor.Process(news.id, title, NewsPart.Title)
      dateActor ! DateDetectorActor.Process(news.id, title, NewsPart.Title)
      processing += 4
    }

    nlp.summary.foreach { summary =>
      addressActor ! AddressDetectorActor.Process(news.id, summary, NewsPart.Summary)
      cityActor ! CityDetectorActor.Process(news.id, summary, NewsPart.Summary)
      crimeActor ! CrimeDetectorActor.Process(news.id, summary, NewsPart.Summary)
      dateActor ! DateDetectorActor.Process(news.id, summary, NewsPart.Summary)
      processing += 4
    }

    nlp.corpus.foreach { corpus =>
      addressActor ! AddressDetectorActor.Process(news.id, corpus, NewsPart.Corpus)
      cityActor ! CityDetectorActor.Process(news.id, corpus, NewsPart.Corpus)
      crimeActor ! CrimeDetectorActor.Process(news.id, corpus, NewsPart.Corpus)
      dateActor ! DateDetectorActor.Process(news.id, corpus, NewsPart.Corpus)
      processing += 4
    }

    //process date from url
    dateActor ! DateDetectorActor.ExtractDate(news.urlNews)
    processing += 1

    //extract named entities from meta description
    nlp.description.foreach { description =>

      addressActor ! AddressDetectorActor.Process(news.id, description, NewsPart.Description)
      cityActor ! CityDetectorActor.Process(news.id, description, NewsPart.Description)
      crimeActor ! CrimeDetectorActor.Process(news.id, description, NewsPart.Description)
      dateActor ! DateDetectorActor.Process(news.id, description, NewsPart.Description)
      processing += 4
    }

  }

  def receive = {

    case AddressDetectorActor.Result(news.id, words, part) =>
      updateData(words, part)

    case CityDetectorActor.Result(news.id, words, part) =>
      updateData(words, part)

    case CrimeDetectorActor.Result(news.id, words, part) =>
      updateData(words, part)

    case DateDetectorActor.Result(news.id, words, part) =>
      updateData(words, part)

    case AddressDetectorActor.Failed(newsId, part, ex) =>
      log.info("AddressDetectorActor Failed for news {}, part {}, exception {}", newsId, part, ex.getStackTrace().mkString("  "))
      decreaseAndCheck()

    case CityDetectorActor.Failed(newsId, part, ex) =>
      log.info("CityDetectorActor Failed for news {}, part {}, exception {}", newsId, part, ex.getStackTrace().mkString("  "))
      decreaseAndCheck()

    case CrimeDetectorActor.Failed(newsId, part, ex) =>
      log.info("CrimeDetectorActor Failed for news {}, part {}, exception {}", newsId, part, ex.getStackTrace().mkString("  "))
      decreaseAndCheck()

    case DateDetectorActor.Failed(newsId, part, ex) =>
      log.info("DateDetectorActor Failed for news {}, part {}, exception {}", newsId, part, ex.getStackTrace().mkString("  "))
      decreaseAndCheck()

    case DateDetectorActor.ExtractedDate(date) =>

      date match {
        case Some(d) =>
          val updateDates = processedNlp.dates.getOrElse(Seq.empty[String]) :+ fmt.print(d)
          processedNlp = processedNlp.copy(dates = Option(updateDates))
        case None =>
      }
      decreaseAndCheck()

    case ReceiveTimeout =>
      log.info("Receive Timeout")
      decreaseAndCheck()

  }

  def updateData(words: Seq[Word], part: NewsPart): Unit = {

    part match {
      case Title =>
        val merge = mergeIOBEntity(processedNlp.title.get, words)
        processedNlp = processedNlp.copy(title = Option(merge))
      case Summary =>
        val merge = mergeIOBEntity(processedNlp.summary.get, words)
        processedNlp = processedNlp.copy(summary = Option(merge))

      case Corpus =>
        val merge = mergeIOBEntity(processedNlp.corpus.get, words)
        processedNlp = processedNlp.copy(corpus = Option(merge))

      case Description =>
        val merge = mergeIOBEntity(processedNlp.description.get, words)
        processedNlp = processedNlp.copy(description = Option(merge))
    }
    decreaseAndCheck()
  }

  def decreaseAndCheck(): Unit = {
    processing -= 1
    if (processing == 0)
      context.parent ! Processed(processedNews.copy(nlp = Option(processedNlp)))
  }

  private def mergeIOBEntity(sentences: Seq[Word], annotated: Seq[Word]): Seq[Word] = {
    sentences.zip(annotated).map { pair =>
      val entities = pair._1.iobEntity ++ pair._2.iobEntity
      val filtered = entities.groupBy(w => w).keys.toVector
      pair._1.copy(iobEntity = filtered)
    }
  }
}