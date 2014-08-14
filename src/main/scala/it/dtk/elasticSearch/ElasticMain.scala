package it.dtk.elasticSearch

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object ElasticMain extends App{

  val config = ConfigFactory.load("elastic")
  val system = ActorSystem("NlpService", config)
  
  val elasticReceptionist = system.actorOf(ElasticReceptionist.props,"ElasticIndexer")
  elasticReceptionist ! ElasticReceptionist.Start
  
}