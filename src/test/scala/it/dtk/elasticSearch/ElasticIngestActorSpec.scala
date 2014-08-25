package it.dtk.elasticSearch

import it.dtk.nlp.BaseTestClass
import akka.testkit.TestActorRef
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import akka.actor.ActorSystem
import it.dtk.nlp.db._
import akka.util.Timeout
import it.dtk.nlp.db.News
import scala.collection.JavaConversions._

object ElasticIngestActorSpec {

  val addressWebServiceUrl = "10.0.0.11:8080"
  val node = ("localhost", 9300)
  val indexDocumentPath = "wheretolive/news"
  val dbManager = new DBManager("10.0.0.11")

  val seqNews = dbManager.nlpNewsIterator(100)

  val singleNews = dbManager.findNlpNews("532886721dbb33b9993dc1db")

}

class ElasticIngestActorSpec extends BaseTestClass {

  import ElasticIngestActorSpec._
  import ElasticIngestActor._
  import scala.util.{ Success, Failure }

  implicit val actorSystem = ActorSystem("test-system")
  implicit val timeout = Timeout(10.seconds)
  import scala.concurrent.ExecutionContext.Implicits.global

  "The actor" should {

    "index news correctly" in {
      val actorRef = TestActorRef(new ElasticIngestActor(node, indexDocumentPath, addressWebServiceUrl))

      while (seqNews.hasNext) {
        val future = actorRef ? Index(MongoDBMapper.dBOToNews(seqNews.next()))

        future.onComplete {
          case Success(v) =>
            println(v)
          case Failure(e) =>
            println(e)
        }
      }

    }

    "index correctly a single news" in {
      val actorRef = TestActorRef(new ElasticIngestActor(node, indexDocumentPath, addressWebServiceUrl))
      val future = actorRef ? Index(singleNews.get)

      future.onComplete {
        case Success(v) =>
          println(v)
        case Failure(e) =>
          println(e)
      }
    }
  }
}