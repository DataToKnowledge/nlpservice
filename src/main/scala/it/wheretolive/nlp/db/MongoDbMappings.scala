package it.wheretolive.nlp.db

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import it.wheretolive.nlp.Model
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.mongodb.casbah.commons.conversions.scala._

/**
 * Created by fabiofumarola on 09/01/15.
 */
trait MongoDbMappings {
  RegisterJodaTimeConversionHelpers()

  import Model._

  implicit object FetchedNewsMapper {

    def toBSon(news: CrawledNews) =
      MongoDBObject(
        "_id" -> new ObjectId(news.id),
        "urlWebSite" -> news.urlWebSite,
        "urlNews" -> news.urlNews,
        "title" -> news.title,
        "summary" -> news.summary,
        "newsDate" -> news.newsDate,
        "corpus" -> news.corpus,
        "tags" -> news.tags,
        "metaDescription" -> news.metaDescription,
        "metaKeyword" -> news.metaKeyword,
        "canonicalUrl" -> news.canonicalUrl,
        "topImage" -> news.topImage,
        "nlpAnalyzed" -> news.nlpAnalyzed)

    def fromBSon(dbo: DBObject) =
      CrawledNews(
        id = dbo._id.map(_.toString).get,
        urlWebSite = dbo.getAs[String]("urlWebSite").getOrElse(""),
        urlNews = dbo.getAs[String]("urlNews").getOrElse(""),
        title = dbo.getAs[String]("title").getOrElse(""),
        summary = dbo.getAs[String]("summary").getOrElse(""),
        newsDate = dbo.getAs[DateTime]("newsDate"),
        corpus = dbo.getAs[String]("corpus").getOrElse(""),
        tags = dbo.getAs[Set[String]]("tags").getOrElse(Set[String]()),
        metaDescription = dbo.getAs[String]("metaDescription").getOrElse(""),
        metaKeyword = dbo.getAs[String]("metaKeyword").getOrElse(""),
        canonicalUrl = dbo.getAs[String]("canonicalUrl").getOrElse(""),
        topImage = dbo.getAs[String]("topImage"),
        nlpAnalyzed = dbo.getAs[Boolean]("nlpAnalyzed").getOrElse(false))
  }

  implicit object AnalyzedNewsMapper {
    def toBSon(analyzedNews: AnalyzedNews) =
      MongoDBObject(
        "news" -> FetchedNewsMapper.toBSon(analyzedNews.news),
        "nlp" -> NlpMapper.toBSon(analyzedNews.nlp.get),
        "namedEntities" -> analyzedNews.namedEntities.map(NamedEntitiesMapper.toBSon),
        "tags" -> analyzedNews.tags.map(_.map(TagMapper.toBSon)),
        "focusLocation" -> analyzedNews.focusLocation.map(LocationMapper.toBSon),
        "focusDate" -> analyzedNews.focusDate)

    def fromBSon = ???
  }

  implicit object LocationMapper {
    def toBSon(location: Location) =
      MongoDBObject(
        "city_name" -> location.city_name,
        "province_name" -> location.province_name,
        "region_name" -> location.region_name,
        "wikipedia_url" -> location.wikipedia_url,
        "geo_location" -> location.geo_location
      )

    def fromBSon = ???
  }

  implicit object TagMapper {

    def toBSon(tag: Tag) =
      MongoDBObject(
        "name" -> tag.name,
        "score" -> tag.score
      )

    def fromBSon = ???
  }

  implicit object NamedEntitiesMapper {

    def toBSon(namedEntites: NamedEntities) =
      MongoDBObject(
        "crimes" -> namedEntites.crimes,
        "relateds" -> namedEntites.relateds,
        "addresses" -> namedEntites.addresses,
        "persons" -> namedEntites.persons,
        "locations" -> namedEntites.locations,
        "geopoliticals" -> namedEntites.geopoliticals,
        "dates" -> namedEntites.dates,
        "organizations" -> namedEntites.organizations)

    def fromBSon = ???
  }

  implicit object NlpMapper {
    def toBSon(nlp: Nlp) =
      MongoDBObject(
        "_id" -> new ObjectId(),
        "title" -> nlp.title.map(WordMapper.toBSon),
        "summary" -> nlp.summary.map(WordMapper.toBSon),
        "corpus" -> nlp.corpus.map(WordMapper.toBSon),
        "description" -> nlp.description.map(WordMapper.toBSon))

    //FIXME implement
    def fromBSon = ???
  }

  implicit object WordMapper {
    def toBSon(word: Word) =
      MongoDBObject(
        "_id" -> new ObjectId(),
        "token" -> word.token,
        "tokenId" -> word.tokenId,
        "tokenStart" -> word.tokenStart,
        "tokenEnd" -> word.tokenStart,
        "sentence" -> word.sentence,
        "posTag" -> word.posTag,
        "wordNetPos" -> word.wordNetPos,
        "lemma" -> word.lemma,
        "iobEntity" -> word.iobEntity,
        "chunk" -> word.chunk)

    //FIXME implement
    def fromBSon = ???
  }

  implicit object CityMapper {

    def fromBSon(dbo: DBObject) =
      City(
        id = dbo._id.map(_.toString).get,
        city_name = dbo.getAs[String]("city_name").get,
        cap = dbo.getAs[String]("cap"),
        province = dbo.getAs[String]("province"),
        province_code = dbo.getAs[String]("province_code"),
        region = dbo.getAs[String]("region"),
        region_code = dbo.getAs[String]("region_code"),
        state = dbo.getAs[String]("state"))

    def toBSon(city: City) =
      MongoDBObject(
        "city_name" -> city.city_name,
        "cap" -> city.cap,
        "province" -> city.province,
        "province_code" -> city.province_code,
        "region" -> city.region,
        "region_code" -> city.region_code,
        "state" -> city.state)

  }

  implicit object CrimeMapper {

    def fromBSon(dbo: DBObject) =
      Crime(
        id = dbo._id.map(_.toString).get,
        name = dbo.getAs[String]("name").get,
        _type = dbo.getAs[String]("type").get)

    def toBSon(crime: Crime) =
      MongoDBObject(
        "name" -> crime.name,
        "type" -> crime._type)
  }
}
