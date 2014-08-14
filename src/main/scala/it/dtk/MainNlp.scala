package it.dtk

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import it.dtk.actornlp.Receptionist
import akka.actor.Inbox

object MainNlp {

  def main(args: Array[String]) {

    println("don't forget to add dbhost and batch news size to the nlp.conf file under resources forlder")
    startup();
  }

  def startup(): Unit = {
    // Override the configuration of the port
    //    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
    //      withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)).
    //      withFallback(ConfigFactory.load("textpro.conf"))

    //val config = ConfigFactory.load("application.conf")
    //val s = config.getConfig("akka.actor.deployment")
    //println(config)

    val config = ConfigFactory.load("nlpservice")

    val system = ActorSystem("NlpService", config)

    val receptionist = system.actorOf(Receptionist.props, "receptionist")
    receptionist ! Receptionist.Start
  }

}