package it.dtk.nlp

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime

object MongoDBMapper {

  def wordToDBO(w: Word): MongoDBObject =
    MongoDBObject(
      "token" -> w.token,
      "tokenId" -> w.tokenId.get,
      "tokenStart" -> w.tokenStart.get,
      "tokenEnd" -> w.tokenStart.get,
      "sentence" -> w.sentence.get,
      "posTag" -> w.posTag.get,
      "lemma" -> w.lemma.get,
      "com    Morpho" -> w.compMorpho.get,
      "stem" -> w.stem.get,
      "iobEntity" -> w.iobEntity.getOrElse(Vector.empty),
      "chunk" -> w.chunk)

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

  def sentenceToDBO(s: Sentence): MongoDBObject = {
    MongoDBObject(
      "words" -> s.words.map(wordToDBO(_)))
  }

  def dBOToSentence(dbo: MongoDBObject): Sentence = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("words")
    //convert to words
    val seqWords = seqDBO.map(s => s.map(dBOToWord(_)))
    Sentence(seqWords.getOrElse(Vector.empty[Word]))
  }

  def nlpTitleToDBO(title: NLPTitle): MongoDBObject = {
    MongoDBObject(
      "sentences" -> title.sentences.map(s => sentenceToDBO(s)))
  }

  def dBOtoNlpTitle(dbo: MongoDBObject): NLPTitle = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence(_)))
    NLPTitle(seqSentences.getOrElse(Vector.empty[Sentence]))
  }

  def nlpSummaryToDBO(title: NLPTitle): MongoDBObject = {
    MongoDBObject(
      "sentences" -> title.sentences.map(s => sentenceToDBO(s)))
  }

  def dBOtoNlpSummary(dbo: MongoDBObject): NLPTitle = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence(_)))
    NLPTitle(seqSentences.getOrElse(Vector.empty[Sentence]))
  }

  def nlpTextToDBO(title: NLPTitle): MongoDBObject = {
    MongoDBObject("sentences" -> title.sentences.map(s => sentenceToDBO(s)))
  }

  def dBOtoNlpText(dbo: MongoDBObject): NLPTitle = {
    val seqDBO = dbo.getAs[Seq[MongoDBObject]]("sentences")
    //convert to sentences
    val seqSentences = seqDBO.map(s => s.map(dBOToSentence(_)))
    NLPTitle(seqSentences.getOrElse(Vector.empty[Sentence]))
  }
  
  def dBOToNews(dbo: MongoDBObject): News = {
    RegisterJodaTimeConversionHelpers()
    News(
     dbo._id.map(_.toString),
     dbo.getAs[String]("urlWebSite"),
     dbo.getAs[String]("urlNews"),
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
}