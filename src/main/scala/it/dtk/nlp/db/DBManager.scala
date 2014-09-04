package it.dtk.nlp.db

import com.mongodb.DBObject
import com.mongodb.casbah.{MongoClient, MongoCursorBase}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import it.dtk.nlp.db.MongoDBMapper._

import scala.tools.nsc.doc.model.Object

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DBManager(val dbHost: String) {

  val options = MongoClientOptions(autoConnectRetry = true, connectTimeout = 240000, socketKeepAlive = true)
  private val mongoClient = MongoClient(dbHost, options)

  private val db = mongoClient("wheretolive")

  private val lemma = db("morphit")
  private val city = db("city")
  private val address = db("address")
  private val crime = db("crime")

  private val geoNews = mongoClient("dbNews")("geoNews")
  private val nlpNews = mongoClient("dbNews")("nlpNews")

  /**
   * Search for a lemma in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findLemma(word: String): Option[Lemma] = {
    val regex = "(?i)^" + word.replace("(", "").replace(")", "") + "$"
    lemma.findOne("word" $regex regex).map(r => r)
    //used regex as explained in  http://mongodb.github.io/casbah/guide/query_dsl.html
    //lemma.findOne(MongoDBObject("word" -> regex.r)).map(r => r)
  }

  /**
   * Search for a crime in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findCrime(word: String): Option[Crime] = {
    val regex = "(?i)^" + word.replace("(", "").replace(")", "") + "$"
    crime.findOne("word" $regex regex).map(r => r)
    //crime.findOne(MongoDBObject("word" -> regex.r)).map(r => r)
  }

  def findCrimeText(word: String): List[Crime] = {
    val str = "\"" + word + "\""
    val result = crime.find($text(str) $language "italian")
    result.toList.map(MongoDBMapper.dBOtoCrime(_))
  }

  def findAddressText(street: String, city: Option[String] = None): List[Address] = {
    val str = "\"" + street + "\""
    val query = if (city.isDefined)
      ($text(str) $language ("italian")) ++ MongoDBObject("city" -> city.get)
    else
      $text(str) $language "italian"
    address.find(query).toList.map(dBOtoAddress)
  }

  /**
   * Search for an address in the DB. Case-insensitive search.
   *
   * @param street
   * @return
   */
  def findAddress(street: String, city: Option[String] = None): Option[Address] = {
    val regexAddr = "(?i)^" + street.replace("(", "").replace(")", "") + "$"
    val regexCity = if (city.isDefined) Some("(?i)^" + city.get.replace("(", "").replace(")", "") + "$") else None
    val query = if (city.isDefined)
      MongoDBObject("street" -> regexAddr.r, "city" -> regexCity.get.r)
    else
      MongoDBObject("street" -> regexAddr.r)

    address.findOne(query).map(r => r)
  }

  /**
   * Search for a city in the DB. Case-insensitive search.
   *
   * @param city_name
   * @return
   */
  def findCity(cityName: String): Option[City] = {
    val regex = "(?i)^" + cityName.replace("(", "").replace(")", "") + "$"
    city.findOne("city_name" $regex regex).map(r => r)
    //city.findOne(MongoDBObject("city_name" -> regex.r)).map(r => r)
  }

  def setGeoNewsAnalyzed(news: News): Int = {
    val query = MongoDBObject("_id" -> new ObjectId(news.id))
    val update = $set("nlpAnalyzed" -> true)
    geoNews.update(query, update, upsert = false, multi = true).getN
  }

  def setNlpNewsIndexed(id: String) ={
    val query = MongoDBObject("_id" -> new ObjectId(id))
    nlpNews.update(query, $set("indexed" -> true), multi = true, upsert = false).getN
  }

  def geoNewsIterator(batchSize: Int): MongoCursorBase =
    geoNews.find().batchSize(batchSize)

  def geoNewsNotAnalyzedIterator(batchSize: Int): MongoCursorBase =
    geoNews.find(MongoDBObject("nlpAnalyzed" -> false)).batchSize(batchSize)

  def nlpNewsIterator(batchSize: Int): MongoCursorBase =
    nlpNews.find().batchSize(batchSize)

  def nlpNewsIteratorNotIndexed(batchSize: Int): MongoCursorBase =
    nlpNews.find("indexed" $eq false).batchSize(batchSize).sort($sort("_id" -> -1, "newsDate" -> -1))


  def findNlpNews(id: String): Option[News] =
    nlpNews.findOne("_id" $eq new ObjectId(id)).map(r => r)

  def findGeoNews(id: String): Option[News] =
    nlpNews.findOne("_id" $eq new ObjectId(id)).map(r => r)

  /**
   * @param batchSize
   * @return an collection iterator which allows to iterate of the news collection
   */
  def iterateOverNews(batchSize: Int): CollectionIterator =
    new CollectionIterator(geoNews, batchSize)

  /**
   * @param news
   * this method should be used to save the nlp processed News into a new collection
   */
  def saveNlpNews(news: News): Int = {
    val q = MongoDBObject("urlNews" -> news.urlNews)
    val result = nlpNews.update[MongoDBObject, DBObject](q, news, true) //nlpNews.save[DBObject](news)
    result.getN()
  }

}

class CollectionIterator(val collection: MongoCollection, val batchSize: Int) {

  require(collection.size > 0)
  var vector = Option(collection.find().limit(batchSize).map(MongoDBMapper.dBOToNews).toVector)
  var lastId = vector.map(_.last.id)

  def hasNext = !vector.isEmpty

  def next: IndexedSeq[News] = {
    val result = vector
    vector = lastId.map(id => collection.find("_id" $gt new ObjectId(id)).limit(batchSize).map(MongoDBMapper.dBOToNews).toVector)
    lastId = vector.map(_.last.id)
    result.getOrElse(Vector.empty[News])
  }

}
