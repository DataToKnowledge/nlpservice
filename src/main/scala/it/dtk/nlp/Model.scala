package it.dtk.nlp

import org.joda.time.DateTime
import java.util.Date

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */

case class Word(token: String, tokenId: Option[Int] = None, tokenStart: Option[Int] = None, tokenEnd: Option[Int] = None,
    sentence: Option[String] = None, posTag: Option[String] = None, lemma: Option[String] = None, compMorpho: Option[String] = None,
    stem: Option[String] = None, iobEntity: Option[Seq[String]] = None , chunk: Option[String] = None)
    

case class Sentence(words: Seq[Word])

case class NLPTitle(sentences: Seq[Sentence])

case class NLPSummary(sentences: Seq[Sentence])

case class NLPText(sentences: Seq[Sentence])

case class News(id: Option[String] = None, urlWebSite: Option[String], urlNews: Option[String],
                title: Option[String], summary: Option[String], newsDate: Option[DateTime],
                text: Option[String] = None, tags: Option[Set[String]] = None,
                metaDescription: Option[String] = None, metaKeyword: Option[String] = None,
                canonicalUrl: Option[String] = None, topImage: Option[String] = None, 
                nlpTitle: Option[NLPTitle] = None, nlpSummary: Option[NLPSummary] = None, nlpText: Option[NLPText] = None,
                crime: Option[Seq[String]] = None, address: Option[Seq[String]] = None,  person: Option[Seq[String]] = None,  
                location: Option[Seq[String]] = None, date: Option[Seq[String]] = None, organization: Option[Seq[String]] = None,
                nlpTags: Option[Map[String,Double]] = None)