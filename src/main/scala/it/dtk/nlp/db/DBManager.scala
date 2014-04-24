package it.dtk.nlp.db

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import MongoDBMapper._
import com.mongodb.casbah.MongoCursor

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object DBManager {

  private val mongoClient = MongoClient("10.0.0.1", 27017)

  private val db = mongoClient("wheretolive")

  private val lemma = db("morphit")
  private val city = db("city")
  private val address = db("address")
  private val crime = db("crime")

  private val news = mongoClient("dbNews")("geoNews")

  private val nlpNews = mongoClient("dbNews")("nlpNews")

  /**
   * Search for a lemma in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findLemma(word: String): Option[Lemma] = {
    val regex = "(?i)^" + word.replace("(", "").replace(")", "") + "$"
    lemma.findOne(MongoDBObject("word" -> regex.r)).map(r => r)
  }

  /**
   * Search for a crime in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findCrime(word: String): Option[Crime] = {
    val regex = "(?i)^" + word.replace("(", "").replace(")", "") + "$"
    crime.findOne(MongoDBObject("word" -> regex.r)).map(r => r)
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
    val query = {
      if (city.isDefined) MongoDBObject("street" -> regexAddr.r, "city" -> regexCity.get.r)
      else MongoDBObject("street" -> regexAddr.r)
    }

    address.findOne(query).map(r => r)
  }

  /**
   * Search for a city in the DB. Case-insensitive search.
   *
   * @param city_name
   * @return
   */
  def findCity(city_name: String): Option[City] = {
    val regex = "(?i)^" + city_name.replace("(", "").replace(")", "") + "$"
    city.findOne(MongoDBObject("city_name" -> regex.r)).map(r => r)
  }

  def getNews(limit: Int = 0): List[News] = {
    if (limit == 0) {
      news.find().map(n => MongoDBMapper.dBOToNews(n)).toList
    } else {
      news.find().limit(limit).map(n => MongoDBMapper.dBOToNews(n)).toList
    }
  }

  /**
   * @param batchSize
   * @return an collection iterator which allows to iterate of the news collection
   */
  def iterateOverNews(batchSize: Int): CollectionIterator =
    new CollectionIterator(news.find(), batchSize)

  /**
   * @param news
   * this method should be used to save the nlp processed News into a new collection
   */
  def saveNlpNews(news: News): Int = {
    val q = MongoDBObject("urlNews" -> news.urlNews)
    val result = nlpNews.update[MongoDBObject,DBObject](q, news, true) //nlpNews.save[DBObject](news)
    result.getN()
  }

}

class CollectionIterator(val cursor: MongoCursor, val batchSize: Int) {
  
  def hasNext = cursor.hasNext

  def next: IndexedSeq[News] = {

    var i = 0
    var result = Vector.empty[News]

    while (hasNext && i < batchSize) {
      result = result :+ MongoDBMapper.dBOToNews(cursor.next)
      i += 1
    }
    result
  }

}
