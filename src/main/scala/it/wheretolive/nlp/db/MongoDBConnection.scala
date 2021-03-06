package it.wheretolive.nlp.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientOptions}
import it.wheretolive.nlp.Model._
import org.bson.types.ObjectId

import scala.util.Try

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

  def fetchBatch(nlpAnalyzed: Boolean = false, processing: Boolean = false, batchSize: Int): List[CrawledNews] = {

    val query = $and("nlpAnalyzed" $eq nlpAnalyzed , "corpus" $ne "", "processing" $eq processing)
    val result = collection.find(query).
      sort(MongoDBObject("_id" -> -1)).
      limit(batchSize).map(FetchedNewsMapper.fromBSon).toList

    result
  }

  /**
   *
   * @param id
   * @param value set processing true o false
   * @return
   */
  def setProcessing(id: String, value: Boolean): Try[Int] = Try {
    val query = MongoDBObject("_id" -> new ObjectId(id))
    val update = $set("processing" -> value)
    collection.update(query, update, upsert = false, multi = true).getN
  }

  def setAnalyzed(id: String): Try[Int] = Try {
    val query = MongoDBObject("_id" -> new ObjectId(id))
    val update = $set("nlpAnalyzed" -> true)
    collection.update(query, update, upsert = false, multi = true).getN
  }
}

trait AnalyzedNewsMongoCollection extends MongoDBConnection {

  def collectionName: String

  def fetchBatch(indexed: Boolean = false, batchSize: Int): List[AnalyzedNews] = {
    val result = collection.find("indexed" $eq indexed).
      limit(batchSize).map(AnalyzedNewsMapper.fromBSon).toList
    result
  }

  def setIndexed(id: String): Try[Int] = Try {
    val query = MongoDBObject("_id" -> new ObjectId(id))
    val update = $set("indexed" -> true)
    collection.update(query, update, upsert = false, multi = true).getN
  }

  def inCollection(urlNews: String) = {
    val query = MongoDBObject("news.urlNews" -> urlNews)
    val result = collection.find(query)
    result.size > 0
  }

  def save(aNews: AnalyzedNews): Try[Int] = Try {
    val bson = AnalyzedNewsMapper.toBSon(aNews) + ("indexd" -> true)
    val result = collection.insert(bson)
    result.getN
  }

}

