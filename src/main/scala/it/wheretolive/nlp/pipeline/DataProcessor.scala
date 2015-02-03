package it.wheretolive.nlp.pipeline

import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import it.wheretolive.akka.pattern._
import scala.concurrent.duration._

object DataProcessor {

  def props = Props(classOf[DataProcessor])
}

/**
 * Created by fabiofumarola on 08/01/15.
 *
 * this class process a news
 */
class DataProcessor extends Actor with ActorLogging with RouteSlipFallible {

  import it.wheretolive.nlp.Model._
  import MessageProtocol._
  import context.dispatcher

  def config = context.system.settings.config.getConfig("nlpservice")

  val dataProducer = context.actorOf(DataProducer.props(), "dataProducer")
  val textProRouter = context.actorOf(TextPro.routerProps(), "textProActorPool")
  val crimesRouter = context.actorOf(CrimesExtractor.routerProps(), "crimesExtractorPool")
  val namedEntitiesFillerRouter = context.actorOf(NamedEntitiesFiller.routerProps(), "namedEntitiesFillerRouter")
  val focusLocationRouter = context.actorOf(FocusLocationExtractor.routerProps(), "focusLocationExtractorRouter")
  val analyzedNewsSaverRouter = context.actorOf(AnalyzedNewsSaver.props,"analyzedNewsSaverRouter")
  val elasticSearchIndexerRouter = context.actorOf(ElasticSearchIndexer.routerProps(),"elasticSearchIndexerRouter")

  var lastIndex = Option.empty[String]

  var totalItemsCount = -1
  var currentItemCount = 0

  var allProcessedItemsCount = 0
  var allProcessingError = 0

  val MAX_LOAD = config.getInt("maxProcessingSize")
  val schedulingTime = config.getInt("scheduling.time")

  override def receive: Receive = {

    case Process =>
      if (totalItemsCount == -1) {
        totalItemsCount = totalItems
        log.info("Starting to process data!")
      }

      if (currentItemCount < MAX_LOAD)
        dataProducer ! FetchData()

    case Data(newsList) =>
      processBatch(newsList)

    case GetLoad =>
      sender ! currentItemCount

    case item: ProcessItem =>
      log.debug("processed news with title {} indexed into es with id {}", item.news.title, item.indexId)
      allProcessedItemsCount += 1
      currentItemCount -= 1
      continueProcessing()

    case RouteSlipMessageFailure(message: ProcessItem, failedTask, failure) =>
      log.error("Error processing news with title \"{}\" from actor {} with error {}", message.news.title,failedTask.path, failure.getMessage)
      allProcessingError += 1
      currentItemCount -=1
      continueProcessing()
  }

  def processBatch(batch: List[CrawledNews]) = {
    if (batch.isEmpty) {
      log.info("Done processing all items with {} successes and {} failures", allProcessedItemsCount, allProcessingError)
      log.info("scheduling next run in {} minutes", schedulingTime)
      context.system.scheduler.scheduleOnce(schedulingTime.minutes,self,Process)
    }
    else {
      val routeSlip = createRouteSlip()

      batch.foreach { item =>
        currentItemCount += 1
        sendMessageToNextTask(routeSlip,ProcessItem(news = item))
      }
    }
  }

  def createRouteSlip(): Seq[ActorRef] =
    List(textProRouter,crimesRouter,namedEntitiesFillerRouter,
      focusLocationRouter,analyzedNewsSaverRouter,elasticSearchIndexerRouter,self)

  def continueProcessing() = {
    val itemProcessed = allProcessedItemsCount + allProcessingError

    //print progressing
    if (itemProcessed > 0 && itemProcessed % 100 == 0)
      log.info("Processed {} items out of with {} total errors", itemProcessed, allProcessingError)

    self ! Process
  }

  def totalItems: Int = 0
}
