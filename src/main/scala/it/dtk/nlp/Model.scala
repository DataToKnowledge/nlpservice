package it.dtk.nlp

import org.joda.time.DateTime

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */

case class Word(token: String, tokenId: Option[Int] = None, tokenStart: Option[Int] = None, tokenEnd: Option[Int] = None,
                sentence: Option[String] = None, posTag: Option[String] = None, lemma: Option[String] = None, compMorpho: Option[String] = None,
                stem: Option[String] = None, iobEntity: Option[Seq[String]] = None, chunk: Option[String] = None)

case class Sentence(words: Seq[Word] = Vector.empty)

case class NLPTitle(sentences: Seq[Sentence] = Vector.empty)

case class NLPSummary(sentences: Seq[Sentence] = Vector.empty)

case class NLPText(sentences: Seq[Sentence] = Vector.empty)

case class News(id: String, urlWebSite: String, urlNews: String, title: Option[String], summary: Option[String],
                newsDate: Option[DateTime], text: Option[String] = None, tags: Option[Set[String]] = None,
                metaDescription: Option[String] = None, metaKeyword: Option[String] = None, canonicalUrl: Option[String] = None,
                topImage: Option[String] = None, nlpTitle: Option[NLPTitle] = None, nlpSummary: Option[NLPSummary] = None,
                nlpText: Option[NLPText] = None, crime: Option[Seq[String]] = None, address: Option[Seq[String]] = None,
                person: Option[Seq[String]] = None, location: Option[Seq[String]] = None, date: Option[Seq[String]] = None,
                organization: Option[Seq[String]] = None, nlpTags: Option[Map[String, Double]] = None)

case class Lemma(id: String, word: String, lemma: Option[String] = None, features: Option[String] = None)

case class City(id: String, city_name: String, cap: Option[String], province: Option[String], province_code: Option[String],
                region: Option[String], region_code: Option[String], state: Option[String])

case class Crime(id: String, word: String, lemma: Option[String] = None, stem: Option[String] = None, tipo: Option[String] = None)

case class Address(id: String, street: String, cap: Option[String] = None, city: Option[String] = None, province: Option[String] = None,
                   state: Option[String] = None, region: Option[String] = None)
