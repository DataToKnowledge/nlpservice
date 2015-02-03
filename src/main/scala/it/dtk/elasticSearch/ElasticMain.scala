package it.dtk.elasticSearch

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

//object ElasticMain extends App{
//
//  println("start with indexAll or indexNotAnalyzed. The second is the default option")
//
//  val indexType = if (args.length > 0) args(0).toString else "indexNotAnalyzed"
//
//  val config = ConfigFactory.load("elastic")
//  val system = ActorSystem("NlpService", config)
//
//  val elasticReceptionist = system.actorOf(ElasticReceptionist.props,"ElasticIndexer")
//  indexType match {
//    case "indexAll" =>
//      elasticReceptionist ! ElasticReceptionist.Reindex
//    case "indexNotAnalyzed" =>
//      elasticReceptionist ! ElasticReceptionist.Index
//  }
//
//}