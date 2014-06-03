package it.dtk.actor.textpro

import it.dtk.actor.MySpec
import it.dtk.nlp.db.News
import org.joda.time.DateTime
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object TextProActorSpec {

  val news = News("5328866f1dbb33b9993dc1d3", "http://bari.repubblica.it", "http://bari.repubblica.it/cronaca/2014/03/18/news/insulti_e_botte_a_tre_ragazze_per_rubare_telefonino_fermata_baby_gang-81285622/",
    Option("Insulti e botte a tre ragazze per rubare telefonino, fermata baby gang"), Option("Hanno tra gli 11 e i 13 anni i cinque ragazzini di Foggia autori di alcuni episodi di bullismo ai danni di una comitiva di coetanee"),
    Option.empty[DateTime],
    Option("Hanno tra gli 11 e i 13 anni i cinque ragazzini di Foggia autori di alcuni episodi di bullismo ai danni di tre ragazze in via Lucera. Le vittime - di 13, 15 e 20 anni - in stato di shock hanno raccontato ad una pattuglia della polizia che intorno alle 19 e 30, mentre erano sedute su una panchina alla periferia della città sono state circondate dai cinque ragazzini. I minorenni le hanno aggredite, insultate e picchiate: uno dei bulli ha tentato anche di rubare ad una ragazza il telefono cellulare. Solo le grida delle vittime hanno messo in fuga i cinque che, poco dopo, sono stati rintracciati dagli investigatori. Accompagnati in questura i ragazzini, non imputabili, sono stati riaffidati ai genitori. Nel corso delle indagini è emerso che uno degli aggressori era compagno di scuola di una delle vittime."))
}

class TextProActorSpec(_system: ActorSystem) extends MySpec(_system) {
  
  def this() = this(ActorSystem("TextProActorSpec", ConfigFactory.load()))
  

  import TextProActor._
  import TextProActorSpec._

  "a Text Pro Actor" must {

    val textPro = system.actorOf(TextProActor.props,"textProActor")

    "return a result when Parsing a news" in {
      textPro ! Parse(news)
      val result = expectMsgClass(classOf[Result])
      
      result.news should have(
        'nlptitle ('defined),
        'nlpSummary ('defined),
        'nlpCorpus ('defined),
        'nlpTags ('defined))

      import org.scalatest.OptionValues._
      if (result.news.nlp.isDefined)
    	  result.news.nlp.get.nlpTags.value.size should be > 1
    }

  }

}