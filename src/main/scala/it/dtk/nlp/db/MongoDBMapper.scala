package it.dtk.nlp.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime
import scala.language.implicitConversions

object MongoDBMapper {

  implicit def wordToDBO(word: Word): DBObject = {
    DBObject(
      "token" -> word.token,
      "tokenId" -> word.tokenId,
      "tokenStart" -> word.tokenStart,
      "tokenEnd" -> word.tokenStart,
      "sentence" -> word.sentence,
      "posTag" -> word.posTag,
      "lemma" -> word.lemma,
      "com    Morpho" -> word.compMorpho,
      "stem" -> word.stem,
      "iobEntity" -> word.iobEntity,
      "chunk" -> word.chunk
    )
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
      dbo.getAs[Seq[String]]("iobEntity").get,
      dbo.getAs[String]("chunk"))
  }

  implicit def sentenceToDBO(sentence: Seq[Word]): DBObject = {
    DBObject(
      "words" -> sentence.map(wordToDBO)
    )
  }

  implicit def dBOToSentence(dbo: DBObject): Seq[Word] = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("words")
    //convert to words
    val seqWords = seqDBO.map(s => s.map(dBOToWord))
    seqWords.getOrElse(Vector.empty)
  }

  implicit def dBOToNews(dbo: DBObject): News = {
    RegisterJodaTimeConversionHelpers()
    News(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("urlWebSite").get,
      dbo.getAs[String]("urlNews").get,
      dbo.getAs[String]("title"),
      dbo.getAs[String]("summary"),
      dbo.getAs[DateTime]("newsDate"),
      dbo.getAs[String]("text"),
      dbo.getAs[Set[String]]("tags"),
      dbo.getAs[String]("metaDescription"),
      dbo.getAs[String]("metaKeyword"),
      dbo.getAs[String]("canonicalUrl"),
      dbo.getAs[String]("topImage")
    )
  }

  implicit def newsToDBO(news: News): DBObject = {
    DBObject(
      "_id" -> news.id,
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
      "nlpTitle" -> news.nlpTitle,
      "nlpSummary" -> news.nlpSummary,
      "nlpCorpus" -> news.nlpCorpus,
      "crimes" -> news.crimes,
      "addresses" -> news.addresses,
      "persons" -> news.persons,
      "locations" -> news.locations,
      "dates" -> news.dates,
      "organizations" -> news.organizations,
      "nlpTags" -> news.nlpTags
    )
  }

  implicit def dBOtoLemma(dbo: DBObject): Lemma = {
    Lemma(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("features")
    )
  }

  implicit  def lemmaToDBO(lemma: Lemma): DBObject = {
    DBObject(
      "word" -> lemma.word,
      "lemma" -> lemma.lemma,
      "features" -> lemma.features
    )
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
      dbo.getAs[String]("state")
    )
  }

  implicit def cityToDBO(city: City): DBObject = {
    DBObject(
      "city_name" -> city.city_name,
      "cap" -> city.cap,
      "province" -> city.province,
      "province_code" -> city.province_code,
      "region" -> city.region,
      "region_code" -> city.region_code,
      "state" -> city.state
    )
  }

  implicit def dBOtoCrime(dbo: DBObject): Crime = {
    Crime(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("stem"),
      dbo.getAs[String]("tipo")
    )
  }

  implicit def crimeToDBO(crime: Crime): DBObject = {
    DBObject(
      "word" -> crime.word,
      "lemma" -> crime.lemma,
      "stem" -> crime.stem,
      "type" -> crime.tipo
    )
  }

  implicit def dBOtoAddress(dbo: DBObject): Address = {
    Address(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("street").get,
      dbo.getAs[String]("cap"),
      dbo.getAs[String]("city"),
      dbo.getAs[String]("province"),
      dbo.getAs[String]("state"),
      dbo.getAs[String]("region")
    )
  }

}