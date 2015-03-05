package it.wheretolive.nlp.pipeline

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import MessageProtocol._

/**
 * Created by fabiofumarola on 29/01/15.
 */
//object NlpRunner extends App {
//
//  val config = ConfigFactory.load("nlpservice.conf")
//  val system = ActorSystem("NlpService", config)
//
//  val dataProcessor = system.actorOf(DataProcessor.props, name = "dataProcessor")
//
//  dataProcessor ! Process
//
//}
