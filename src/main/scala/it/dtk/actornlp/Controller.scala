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
  val collectionFilterActor = context.actorOf(CollectionFillerActor.routerProps(5),"collectionFilterPool")

  def receive = {
    case Process(news) =>
      log.info("processing news with title {}", news.title.getOrElse(news.id))
      mapNewsIdSender += (news.id -> sender)
      textProRouter ! TextProActor.Parse(news)

    case TextProActor.Result(news) =>
      log.debug("calling nlpActors for the news with title {}", news.title.getOrElse(news.id))
      val worker = context.actorOf(NamedEntitiesExtractor.props(news, counter), s"NamedEntityExtractor$counter")
      counter += 1

    case TextProActor.Fail(newsId, ex) =>
      val send = mapNewsIdSender.get(newsId)
      if (send.isDefined)
        send.get ! FailProcess(newsId, ex)
      else
        log.error("sender not defined for news {}", newsId)

    case TextProActor.FailProcessingLine(ex) =>
      log.error("error in TextPro parsing the result with exception {}", ex.getStackTrace().mkString("\t"))

    case NamedEntitiesExtractor.Processed(news) =>
      sender ! PoisonPill
      
      val send = mapNewsIdSender.get(news.id)
      if (send.isDefined)
        collectionFilterActor ! CollectionFillerActor.ProcessSingle(news, send.get)
      else
        log.error("sender not defined for news {}", news.id)

    case CollectionFillerActor.ProcessedSingle(news, send) =>
      log.info("end processing news with title {}", news.title.getOrElse(news.id))
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
  var processedNews = news

  val addressActor = context.actorOf(AddressDetectorActor.props, s"addressActor$id")
  val cityActor = context.actorOf(CityDetectorActor.props, s"cityActor$id")
  val crimeActor = context.actorOf(CrimeDetectorActor.props, s"crimeActor$id")
  val dateActor = context.actorOf(DateDetectorActor.props, s"dateActor$id")

  if (news.nlpTitle.isDefined) {
    addressActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
    cityActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
    crimeActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
    dateActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Title)
    processing += 4
  }

  if (news.nlpSummary.isDefined) {
    addressActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Summary)
    cityActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Summary)
    crimeActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Summary)
    dateActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Summary)
    processing += 4
  }

  if (news.nlpCorpus.isDefined) {
    addressActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Corpus)
    cityActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Corpus)
    crimeActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Corpus)
    dateActor ! Detector.Process(news.id, news.nlpTitle.get, NewsPart.Corpus)
    processing += 4
  }

  def receive = {
    case Detector.Result(news.id, words, NewsPart.Title) =>
      val merge = mergeIOBEntity(news.nlpTitle.get, words)
      processedNews = processedNews.copy(nlpTitle = Option(merge))
      processing -= 1
      if (processing == 0)
        context.parent ! Processed(processedNews)

    case Detector.Result(news.id, words, NewsPart.Summary) =>
      val merge = mergeIOBEntity(news.nlpSummary.get, words)
      processedNews = processedNews.copy(nlpSummary = Option(merge))
      processing -= 1
      if (processing == 0)
        context.parent ! Processed(processedNews)

    case Detector.Result(news.id, words, NewsPart.Corpus) =>
      val merge = mergeIOBEntity(news.nlpCorpus.get, words)
      processedNews = processedNews.copy(nlpCorpus = Option(merge))
      processing -= 1
      if (processing == 0)
        context.parent ! Processed(processedNews)

    case Detector.Failure(newsId, part, ex) =>
      log.error("Detector Failed for news {}, part {}, exception {}", newsId, part, ex.getStackTrace().mkString("  "))
      processing -= 1
      if (processing == 0)
        context.parent ! Processed(processedNews)
  }

  private def mergeIOBEntity(sentences: Seq[Word], annotated: Seq[Word]): Seq[Word] = {
    sentences.zip(annotated).map(w => w._1.copy(iobEntity = w._1.iobEntity ++ w._2.iobEntity))
  }
}