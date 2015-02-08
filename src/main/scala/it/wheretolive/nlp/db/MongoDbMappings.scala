package it.wheretolive.nlp.db

import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import it.wheretolive.nlp.Model
import it.wheretolive.nlp.pipeline.FocusLocationExtractor
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
        "processing" -> news.processing,
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
        processing = dbo.getAs[Boolean]("processing").getOrElse(false),
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

    def fromBSon(dbo: DBObject) =
      AnalyzedNews(
        id = dbo._id.map(_.toString).get,
        news = dbo.getAs[DBObject]("news").map(FetchedNewsMapper.fromBSon).get,
        nlp = dbo.getAs[DBObject]("nlp").map(NlpMapper.fromBSon),
        namedEntities = dbo.getAs[DBObject]("namedEntities").map(NamedEntitiesMapper.fromBSon),
        tags = dbo.getAs[Seq[DBObject]]("tags").map(seq => seq.map(TagMapper.fromBSon)),
        focusLocation = dbo.getAs[DBObject]("focusLocation").map(LocationMapper.fromBSon),
        focusDate = dbo.getAs[String]("focusDate")
      )
  }

  implicit object LocationMapper {
    def toBSon(location: Location) =
      MongoDBObject(
        "city_name" -> location.city_name,
        "province_name" -> location.province_name,
        "region_name" -> location.region_name,
        "population" -> location.population,
        "wikipedia_url" -> location.wikipedia_url,
        "geoname_url" -> location.geoname_url,
        "geo_location" -> location.geo_location
      )

    def fromBSon(dbo: DBObject) =
      Location(
        city_name = dbo.getAs[String]("city_name").get,
        province_name = dbo.getAs[String]("province_name").get,
        region_name = dbo.getAs[String]("region_name").get,
        population = dbo.getAs[String]("population").getOrElse("0"),
        wikipedia_url = dbo.getAs[String]("wikipedia_url").get,
        geoname_url = dbo.getAs[String]("geoname_url").getOrElse(""),
        geo_location = dbo.getAs[String]("geo_location").get
      )
  }

  implicit object TagMapper {

    def toBSon(tag: Tag) =
      MongoDBObject(
        "name" -> tag.name,
        "score" -> tag.score
      )

    def fromBSon(dbo: DBObject) =
      Tag(
        name = dbo.getAs[String]("name").get,
        score = dbo.getAs[Double]("score").get
      )
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

    def fromBSon(dbo: DBObject) =
      NamedEntities(
        crimes = dbo.getAs[List[String]]("crimes").getOrElse(List()),
        relateds = dbo.getAs[List[String]]("relateds").getOrElse(List()),
        addresses = dbo.getAs[List[String]]("addresses").getOrElse(List()),
        persons = dbo.getAs[List[String]]("persons").getOrElse(List()),
        locations = dbo.getAs[List[String]]("locations").getOrElse(List()),
        geopoliticals = dbo.getAs[List[String]]("geopoliticals").getOrElse(List()),
        dates = dbo.getAs[List[String]]("dates").getOrElse(List()),
        organizations = dbo.getAs[List[String]]("organizations").getOrElse(List())
      )
  }

  implicit object NlpMapper {
    def toBSon(nlp: Nlp) =
      MongoDBObject(
        "_id" -> new ObjectId(),
        "title" -> nlp.title.map(WordMapper.toBSon),
        "summary" -> nlp.summary.map(WordMapper.toBSon),
        "corpus" -> nlp.corpus.map(WordMapper.toBSon),
        "description" -> nlp.description.map(WordMapper.toBSon))

    def fromBSon(dbo: DBObject) =
      Nlp(
        title = dbo.getAs[List[DBObject]]("title").map(seq => seq.map(WordMapper.fromBSon)).getOrElse(List[Word]()),
        summary = dbo.getAs[List[DBObject]]("summary").map(seq => seq.map(WordMapper.fromBSon)).getOrElse(List[Word]()),
        corpus = dbo.getAs[List[DBObject]]("corpus").map(seq => seq.map(WordMapper.fromBSon)).getOrElse(List[Word]()),
        description = dbo.getAs[List[DBObject]]("description").map(seq => seq.map(WordMapper.fromBSon)).getOrElse(List[Word]())
      )
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

    def fromBSon(dbo: DBObject) =
      Word(
        token = dbo.getAs[String]("token").get,
        tokenId = dbo.getAs[Int]("tokenId").get,
        tokenStart = dbo.getAs[Int]("tokenStart").get,
        tokenEnd = dbo.getAs[Int]("tokenEnd").get,
        sentence = dbo.getAs[String]("sentence").get,
        posTag = dbo.getAs[String]("posTag").get,
        wordNetPos = dbo.getAs[String]("wordNetPos").get,
        lemma = dbo.getAs[String]("lemma").get,
        iobEntity = dbo.getAs[String]("iobEntity").get,
        chunk = dbo.getAs[String]("chunk").get
      )
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
