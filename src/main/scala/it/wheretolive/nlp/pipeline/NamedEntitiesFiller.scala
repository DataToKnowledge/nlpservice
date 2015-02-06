package it.wheretolive.nlp.pipeline

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.FromConfig
import it.wheretolive.akka.pattern.{ RouteSlip, RouteSlipFallible, RouteSlipMessage }
import it.wheretolive.nlp.Model._
import it.wheretolive.nlp.pipeline.MessageProtocol._
import it.wheretolive.nlp.pipeline.detector.CollectionFiller

object NamedEntitiesFiller {
  def props = Props[NamedEntitiesFiller]

  def routerProps() =
    FromConfig.props(props)
}

/**
 * Created by fabiofumarola on 12/01/15.
 */
class NamedEntitiesFiller extends Actor with ActorLogging with CollectionFiller with RouteSlipFallible {

  override def receive: Receive = {

    case RouteSlipMessage(routeSlip, procNews: ProcessItem) =>

      val myself = self

      try {
        val nlp = procNews.nlp.get
        val persons = fillPersons(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)
        val organizations = fillOrganizations(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)
        val locations = fillLocations(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)
        val gpe = fillGPEs(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)
        val crimes = fillCrimes(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)
        val relateds = fillRelated(nlp.title ++ nlp.summary ++ nlp.description ++ nlp.corpus)

        val namedEntities = NamedEntities(
          crimes = crimes,
          related = relateds,
          persons = persons,
          locations = locations,
          geopoliticals = gpe,
          organizations = organizations
        )

        sendMessageToNextTask(routeSlip, procNews.copy(namedEntities = Option(namedEntities)))
      }
      catch {
        case ex: Throwable =>
          sendToEndTask(routeSlip, procNews, myself, ex)
      }
  }
}
