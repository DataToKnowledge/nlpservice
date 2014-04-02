package it.dtk.nlp.db

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import MongoDBMapper._

/**
 *
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object DBManager {

  private val mongoClient = MongoClient("10.1.0.62", 27017)

  private val db = mongoClient("wheretolive")

  private val lemma = db("morphit")
  private val city = db("city")
  private val address = db("address")
  private val crime = db("crime")
  private val news = mongoClient("dbNews")("geoNews")

  /**
   * Search for a lemma in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findLemma(word: String): Option[Lemma] = {
    val regex = "(?i)^" + word + "$"
    lemma.findOne(MongoDBObject("word" -> regex.r)) match {
      case Some(res: DBObject) =>
        Option(res)
      case None =>
        None
    }
  }

  /**
   * Search for a crime in the DB. Case-insensitive search.
   *
   * @param word
   * @return
   */
  def findCrime(word: String): Option[Crime] = {
    val regex = "(?i)^" + word + "$"
    crime.findOne(MongoDBObject("word" -> regex.r)) match {
      case Some(res: DBObject) =>
        Option(res)
      case None =>
        None
    }
  }

  /**
   * Search for an address in the DB. Case-insensitive search.
   *
   * @param street
   * @return
   */
  def findAddress(street: String, city: Option[String] = None): Option[Address] = {
    val regexAddr = "(?i)^" + street + "$"
    val regexCity = if (city.isDefined) Some("(?i)^" + city.get + "$") else None
    val query = {
      if (city.isDefined) MongoDBObject("street" -> regexAddr.r, "city" -> regexCity.get.r)
      else MongoDBObject("street" -> regexAddr.r)
    }

    address.findOne(query) match {
      case Some(res: DBObject) =>
        Option(res)
      case None =>
        None
    }
  }

  /**
   * Search for a city in the DB. Case-insensitive search.
   *
   * @param city_name
   * @return
   */
  def findCity(city_name: String): Option[City] = {
    val regex = "(?i)^" + city_name + "$"
    city.findOne(MongoDBObject("city_name" -> regex.r)) match {
      case Some(res: DBObject) =>
        Option(res)
      case None =>
        None
    }
  }

  def getNews(limit: Int = 0): List[News] = {
    if (limit == 0) {
      news.find().map(n => MongoDBMapper.dBOToNews(n)).toList
    } else {
      news.find().limit(limit).map(n => MongoDBMapper.dBOToNews(n)).toList
    }
  }

}
