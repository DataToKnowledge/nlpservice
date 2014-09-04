package it.dtk.nlp.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime
import scala.language.implicitConversions

object MongoDBMapper {
  RegisterJodaTimeConversionHelpers()

  implicit def wordToDBO(word: Word): DBObject = {
    DBObject(
      "_id" -> new ObjectId(),
      "token" -> word.token,
      "tokenId" -> word.tokenId,
      "tokenStart" -> word.tokenStart,
      "tokenEnd" -> word.tokenStart,
      "sentence" -> word.sentence,
      "posTag" -> word.posTag,
      "lemma" -> word.lemma,
      "comMorpho" -> word.compMorpho,
      "stem" -> word.stem,
      "iobEntity" -> word.iobEntity,
      "chunk" -> word.chunk)
  }

  implicit def dBOToWord(dbo: DBObject): Word = {
    Word(
      dbo.getAs[String]("token").get,
      dbo.getAs[Int]("tokenId"),
      dbo.getAs[Int]("tokenStart"),
      dbo.getAs[Int]("tokenEnd"),
      dbo.getAs[String]("sentence"),
      dbo.getAs[String]("posTag"),
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("comMorpho"),
      dbo.getAs[String]("stem"),
      dbo.getAs[Vector[String]]("iobEntity").get,
      dbo.getAs[String]("chunk"),
      dbo._id.map(_.toString()).get)
  }

  implicit def dBOToSentence(dbo: DBObject): Seq[Word] = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("words")
    //convert to words
    val seqWords = seqDBO.map(s => s.map(dBOToWord))
    seqWords.getOrElse(Vector.empty)
  }

  implicit def dBOToNews(dbo: DBObject): News = {    
    News(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("urlWebSite").getOrElse(""),
      dbo.getAs[String]("urlNews").getOrElse(""),
      dbo.getAs[String]("title"),
      dbo.getAs[String]("summary"),
      dbo.getAs[DateTime]("newsDate"),
      dbo.getAs[String]("text"),
      dbo.getAs[Set[String]]("tags"),
      dbo.getAs[String]("metaDescription"),
      dbo.getAs[String]("metaKeyword"),
      dbo.getAs[String]("canonicalUrl"),
      dbo.getAs[String]("topImage"),
      dbo.getAs[Boolean]("nlpAnalyzed"),
      dbo.getAs[Boolean]("indexed").getOrElse(false),
      dbo.getAs[DBObject]("nlp").map(dBOToNlp))
  }

  implicit def dBOToNlp(dbo: DBObject): Nlp =
    Nlp(
      None, //title
      None, //summary
      None, //corpus
      None, //description
      dbo.getAs[Seq[String]]("crimes"),
      dbo.getAs[Seq[String]]("addresses"),
      dbo.getAs[Seq[String]]("persons"),
      dbo.getAs[Seq[String]]("locations"),
      dbo.getAs[Seq[String]]("geopoliticals"),
      dbo.getAs[Seq[String]]("dates"),
      dbo.getAs[Seq[String]]("organizations"),
      dbo.getAs[Map[String, Double]]("nlpTags"))

  implicit def newsToDBO(news: News): DBObject = {
    DBObject(
      "_id" -> new ObjectId(news.id),
      "urlWebSite" -> news.urlWebSite,
      "urlNews" -> news.urlNews,
      "title" -> news.title,
      "summary" -> news.summary,
      "newsDate" -> news.newsDate,
      "text" -> news.corpus,
      "tags" -> news.tags,
      "metaDescription" -> news.metaDescription,
      "metaKeyword" -> news.metaKeyword,
      "canonicalUrl" -> news.canonicalUrl,
      "topImage" -> news.topImage,
      "nlp" -> news.nlp.map(nlpToDBO),
      "indexed" -> news.indexed)
  }

  implicit def nlpToDBO(nlp: Nlp): DBObject =
    DBObject(
      "_id" -> new ObjectId(),
      "title" -> nlp.title.map(_.map(wordToDBO(_))),
      "summary" -> nlp.summary.map(_.map(wordToDBO(_))),
      "corpus" -> nlp.corpus.map(_.map(wordToDBO(_))),
      "description" -> nlp.description.map(_.map(wordToDBO(_))),
      "crimes" -> nlp.crimes,
      "addresses" -> nlp.addresses,
      "persons" -> nlp.persons,
      "locations" -> nlp.locations,
      "dates" -> nlp.dates,
      "organizations" -> nlp.organizations,
      "geopoliticals" -> nlp.geopoliticals,
      //FIXME remember to restore the dots
      "nlpTags" -> nlp.nlpTags.map(_.map(kw => (kw._1.replace(".", "_dot_") -> kw._2))))

  implicit def dBOtoLemma(dbo: DBObject): Lemma = {
    Lemma(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("features"))
  }

  implicit def lemmaToDBO(lemma: Lemma): DBObject = {
    DBObject(
      "word" -> lemma.word,
      "lemma" -> lemma.lemma,
      "features" -> lemma.features)
  }

  implicit def dBOtoCity(dbo: DBObject): City = {
    City(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("city_name").get,
      dbo.getAs[String]("cap"),
      dbo.getAs[String]("province"),
      dbo.getAs[String]("province_code"),
      dbo.getAs[String]("region"),
      dbo.getAs[String]("region_code"),
      dbo.getAs[String]("state"))
  }

  implicit def cityToDBO(city: City): DBObject = {
    DBObject(
      "city_name" -> city.city_name,
      "cap" -> city.cap,
      "province" -> city.province,
      "province_code" -> city.province_code,
      "region" -> city.region,
      "region_code" -> city.region_code,
      "state" -> city.state)
  }

  implicit def dBOtoCrime(dbo: DBObject): Crime = {
    Crime(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("stem"),
      dbo.getAs[String]("type"))
  }

  implicit def crimeToDBO(crime: Crime): DBObject = {
    DBObject(
      "word" -> crime.word,
      "lemma" -> crime.lemma,
      "stem" -> crime.stem,
      "type" -> crime.tipo)
  }

  implicit def dBOtoAddress(dbo: DBObject): Address = {
    Address(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("street").get,
      dbo.getAs[String]("cap"),
      dbo.getAs[String]("city"),
      dbo.getAs[String]("province"),
      dbo.getAs[String]("state"),
      dbo.getAs[String]("region"))
  }

}