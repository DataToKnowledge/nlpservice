package it.dtk

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import it.dtk.actor.NlpReceptionist

object Main {

  def main(args: Array[String]) {

    if (args.length == 3) {
      startup(args(0), args(1), args(2))
    } else {
      println("specify db host, hostname and port number to run NlpService such as 10.0.0.1 10.0.0.10 2552")
      System.exit(1)
    }
  }

  def startup(dbHost: String, hostname: String, port: String): Unit = {
    // Override the configuration of the port
//    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
//      withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)).
//      withFallback(ConfigFactory.load("textpro.conf"))
    
    val config = ConfigFactory.load()

    val system = ActorSystem("NlpService", config)
    
    val receptionist = system.actorOf(NlpReceptionist.props(dbHost))
    
    receptionist ! NlpReceptionist.Start
  }

}