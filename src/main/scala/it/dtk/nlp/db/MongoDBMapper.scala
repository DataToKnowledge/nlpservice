package it.dtk.nlp.db

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime

object MongoDBMapper {

  def wordToDBO(word: Word): DBObject = {
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

  def dBOToWord(dbo: DBObject): Word = {
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

  def sentenceToDBO(sentence: Sentence): DBObject = {
    DBObject(
      "words" -> sentence.words.map(wordToDBO)
    )
  }

  def dBOToSentence(dbo: DBObject): Sentence = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("words")
    //convert to words
    val seqWords = seqDBO.map(s => s.map(dBOToWord))
    Sentence(seqWords.getOrElse(Vector.empty))
  }

  def nlpTitleToDBO(title: NLPTitle): DBObject = {
    DBObject(
      "sentences" -> title.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpTitle(dbo: DBObject): NLPTitle = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPTitle(seqSentences.getOrElse(Vector.empty))
  }

  def nlpSummaryToDBO(summary: NLPSummary): DBObject = {
    DBObject(
      "sentences" -> summary.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpSummary(dbo: DBObject): NLPSummary = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPSummary(seqSentences.getOrElse(Vector.empty))
  }

  def nlpTextToDBO(text: NLPText): DBObject = {
    DBObject(
      "sentences" -> text.sentences.map(s => sentenceToDBO(s))
    )
  }

  def dBOtoNlpText(dbo: DBObject): NLPText = {
    val seqDBO = dbo.getAs[Seq[DBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence))
    NLPText(seqSentences.getOrElse(Vector.empty))
  }

  def dBOToNews(dbo: DBObject): News = {
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

  def NewsToDBO(news: News): DBObject = {
    DBObject(
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

  def dBOtoLemma(dbo: DBObject): Lemma = {
    Lemma(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("features")
    )
  }

  def lemmaToDBO(lemma: Lemma): DBObject = {
    DBObject(
      "word" -> lemma.word,
      "lemma" -> lemma.lemma,
      "features" -> lemma.features
    )
  }

  def dBOtoCity(dbo: DBObject): City = {
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

  def cityToDBO(city: City): DBObject = {
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

  def dBOtoCrime(dbo: DBObject): Crime = {
    Crime(
      dbo._id.map(_.toString).get,
      dbo.getAs[String]("word").get,
      dbo.getAs[String]("lemma"),
      dbo.getAs[String]("stem"),
      dbo.getAs[String]("tipo")
    )
  }

  def crimeToDBO(crime: Crime): DBObject = {
    DBObject(
      "word" -> crime.word,
      "lemma" -> crime.lemma,
      "stem" -> crime.stem,
      "type" -> crime.tipo
    )
  }

  def dBOtoAddress(dbo: DBObject): Address = {
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