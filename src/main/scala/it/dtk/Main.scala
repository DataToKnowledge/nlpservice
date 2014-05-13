package it.dtk

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import it.dtk.actor.NlpReceptionist

object Main {

  def main(args: Array[String]) {

    if (args.length == 1) {
      startup(args(0))
    } else {
      println("specify db host 10.0.0.11")
      System.exit(1)
    }
  }

  def startup(dbHost: String): Unit = {
    // Override the configuration of the port
    //    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    //      withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)).
    //      withFallback(ConfigFactory.load("textpro.conf"))

    //val config = ConfigFactory.load("application.conf")
    //val s = config.getConfig("akka.actor.deployment")
    //println(config)

    val config = ConfigFactory.load("application")

    val system = ActorSystem("NlpService", config)

    val receptionist = system.actorOf(NlpReceptionist.props(dbHost),"receptionist")

    receptionist ! NlpReceptionist.Start
  }

}