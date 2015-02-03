package it.wheretolive.nlp.db
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{ MongoClient, MongoClientOptions }
import it.wheretolive.nlp.Model._

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait MongoDBConnection extends MongoDbMappings {

  def host: String
  def port: Int
  def dbName: String

  def username: String
  def password: String

  def collectionName: String

  val server = new ServerAddress(host, port)
  val credential = MongoCredential.createMongoCRCredential(username, dbName, password.toCharArray)

  val options = MongoClientOptions(autoConnectRetry = true, connectTimeout = 240000, socketKeepAlive = true)

  val mongoClient = MongoClient(server, List(credential))
  val mongoDB = mongoClient(dbName)
  val collection = mongoDB(collectionName)

}

trait CrimeMongoCollection extends MongoDBConnection {
  import com.mongodb.casbah.Imports._

  def findCrimeText(word: String): List[Crime] = {
    val str = "\"" + word + "\""
    val result = collection.find($text(str) $language "italian")
    result.map(d => CrimeMapper.fromBSon(d)).toList
  }
}

trait CrawledNewsMongoCollection extends MongoDBConnection {

  def collectionName: String

  def fetchBatch(nlpAnalyzed: Boolean = false, batchSize: Int): List[CrawledNews] = {

    val result = collection.find("nlpAnalyzed" $eq nlpAnalyzed).
      sort(MongoDBObject("_id" -> -1)).
      limit(batchSize).map(FetchedNewsMapper.fromBSon).toList

    result
  }
}

trait AnalyzedNewsMongoCollection extends MongoDBConnection {

  def collectionName: String

  def fetchBatch(indexed: Boolean = false, batchSize: Int): List[AnalyzedNews] = {

    val result = collection.find("indexed" $eq indexed).
      sort(MongoDBObject("id" -> -1)).
      limit(batchSize).map(AnalyzedNewsMapper.fromBSon).toList
    result
  }

  def save(aNews: AnalyzedNews): Int = {
    val query = MongoDBObject("urlNews" -> aNews.news.urlNews)
    val result = collection.update[MongoDBObject, DBObject](query, AnalyzedNewsMapper.toBSon(aNews), true)
    result.getN
  }

}

