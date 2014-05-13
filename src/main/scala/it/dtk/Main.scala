package it.dtk

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import it.dtk.actor.NlpReceptionist

object Main {

  def main(args: Array[String]) {

    if (args.length == 2) {
      startup(args(0), args(1))
    } else {
      println("specify db host and new per iteration 10.0.0.11 5")
      System.exit(1)
    }
  }

  def startup(dbHost: String, newsIteration: String): Unit = {
    // Override the configuration of the port
    //    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    //      withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)).
    //      withFallback(ConfigFactory.load("textpro.conf"))

    //val config = ConfigFactory.load("application.conf")
    //val s = config.getConfig("akka.actor.deployment")
    //println(config)

    val config = ConfigFactory.load("application")

    val system = ActorSystem("NlpService", config)

    val receptionist = system.actorOf(NlpReceptionist.props(dbHost, newsIteration.toInt),"receptionist")

    receptionist ! NlpReceptionist.Start
  }

}