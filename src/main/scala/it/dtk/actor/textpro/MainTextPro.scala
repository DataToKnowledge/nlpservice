package it.dtk.actor.textpro
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object MainTextPro {

  def main(args: Array[String]) {

    if (args.length == 2) {
      startup(args(0),args(1))
    } else {
      println("specify hostname and port number to run TextProActor such as 10.0.0.10 2552")
      System.exit(1)
    }
  }

  def startup(hostname: String, port : String): Unit = {
    // Override the configuration of the port
    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
      withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + hostname)).
      withFallback(ConfigFactory.load("textpro.conf"))
      
      val system = ActorSystem("TextProSystem",config)
  }
}