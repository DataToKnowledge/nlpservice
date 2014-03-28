package it.dtk.nlp

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime

object MongoDBMapper {

  def wordToDBO(word: Word): MongoDBObject = {
    MongoDBObject(
      "token" -> word.token,
      "tokenId" -> word.tokenId,
      "tokenStart" -> word.tokenStart,
      "tokenEnd" -> word.tokenStart,
      "sentence" -> word.sentence,
      "posTag" -> word.posTag,
      "lemma" -> word.lemma,
      "com    Morpho" -> word.compMorpho,
      "stem" -> word.stem,
      "iobEntity" -> word.iobEntity.getOrElse(Vector.empty),
      "chunk" -> word.chunk
    )
  }

  def dBOToWord(dbo: MongoDBObject): Word = {
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
      dbo.getAs[Seq[String]]("iobEntity"),
      dbo.getAs[String]("chunk"))
  }

  def sentenceToDBO(sentence: Sentence): MongoDBObject = {
    MongoDBObject(
      "words" -> sentence.words.map(wordToDBO)
    )
  }

  def dBOToSentence(dbo: MongoDBObject): Sentence = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("words")
    //convert to words
    val seqWords = seqDBO.map(s => s.map(dBOToWord))
    Sentence(seqWords.getOrElse(Vector.empty))
  }

  def nlpTitleToDBO(title: NLPTitle): MongoDBObject = {
    MongoDBObject(
      "sentences" -> title.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpTitle(dbo: MongoDBObject): NLPTitle = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPTitle(seqSentences.getOrElse(Vector.empty))
  }

  def nlpSummaryToDBO(summary: NLPSummary): MongoDBObject = {
    MongoDBObject(
      "sentences" -> summary.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpSummary(dbo: MongoDBObject): NLPSummary = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPSummary(seqSentences.getOrElse(Vector.empty))
  }

  def nlpTextToDBO(text: NLPText): MongoDBObject = {
    MongoDBObject(
      "sentences" -> text.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpText(dbo: MongoDBObject): NLPText = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPText(seqSentences.getOrElse(Vector.empty))
  }

  def dBOToNews(dbo: MongoDBObject): News = {
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

  def NewsToDBO(news: News): MongoDBObject = {
    MongoDBObject(
      "urlWebSite" -> news.urlWebSite,
      "urlNews" -> news.urlNews,
      "title" -> news.title,
      "summary" -> news.summary,
      "newsDate" -> news.newsDate,
      "text" -> news.text,
      "tags" -> news.tags,
      "metaDescription" -> news.metaDescription,
      "metaKeyword" -> news.metaKeyword,
      "canonicalUrl" -> news.canonicalUrl,
      "topImage" -> news.topImage
    )
  }

  def dBOtoLemma(dbo: MongoDBObject): Lemma = {
    Lemma(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("features")
    )
  }

  def lemmaToDBO(lemma: Lemma): MongoDBObject = {
    MongoDBObject(
      "word" -> lemma.word,
      "lemma" -> lemma.lemma,
      "features" -> lemma.features
    )
  }

  def dBOtoCity(dbo: MongoDBObject): City = {
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

  def cityToDBO(city: City): MongoDBObject = {
    MongoDBObject(
      "city_name" -> city.city_name,
      "cap" -> city.cap,
      "province" -> city.province,
      "province_code" -> city.province_code,
      "region" -> city.region,
      "region_code" -> city.region_code,
      "state" -> city.state
    )
  }

  def dBOtoCrime(dbo: MongoDBObject): Crime = {
    Crime(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("stem"),
      dbo.getAs[String]("tipo")
    )
  }

  def crimeToDBO(crime: Crime): MongoDBObject = {
    MongoDBObject(
      "word" -> crime.word,
      "lemma" -> crime.lemma,
      "stem" -> crime.stem,
      "type" -> crime.tipo
    )
  }

  def dBOtoAddress(dbo: MongoDBObject): Address = {
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