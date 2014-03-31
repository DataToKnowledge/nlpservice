package it.dtk.nlp.db

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject

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

  def findLemma(word: String): Option[Lemma] = {
    lemma.findOne(MongoDBObject("word" -> word)) match {
      case Some(res: DBObject) =>
        Option(MongoDBMapper.dBOtoLemma(res))
      case None =>
        None
    }
  }

  def findCrime(word: String): Option[Crime] = {
    crime.findOne(MongoDBObject("word" -> word)) match {
      case Some(res: DBObject) =>
        Option(MongoDBMapper.dBOtoCrime(res))
      case None =>
        None
    }
  }

  def findAddress(street: String): Option[Address] = {
    address.findOne(MongoDBObject("street" -> street)) match {
      case Some(res: DBObject) =>
        Option(MongoDBMapper.dBOtoAddress(res))
      case None =>
        None
    }
  }

  def findCity(city_name: String): Option[City] = {
    city.findOne(MongoDBObject("city_name" -> city_name)) match {
      case Some(res: DBObject) =>
        Option(MongoDBMapper.dBOtoCity(res))
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
