package it.dtk

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import it.dtk.actornlp.Receptionist
import akka.actor.Inbox

object MainNlp {

  def main(args: Array[String]) {

    println("you can start to IndexAll or IndexNotAnalyzed, by default run IndexNotAnalyzed")
    val indexMethod = if (args.size > 0) args(0) else "IndexNotAnalyzed"

    startup(indexMethod);
  }

  def startup(indexMethod: String): Unit = {
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
    indexMethod match {
      case "IndexAll" =>
        receptionist ! Receptionist.Start
      case "IndexNotAnalyzed" =>
        receptionist ! Receptionist.IndexNotAnalyzed
      case value: String =>
        println(s"value $value not accepted")
        system.shutdown()
    }
  }

}