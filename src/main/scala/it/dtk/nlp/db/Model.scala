package it.dtk.nlp.db

import org.joda.time.DateTime

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */

case class Word(token: String, tokenId: Option[Int] = None, tokenStart: Option[Int] = None, tokenEnd: Option[Int] = None,
  sentence: Option[String] = None, posTag: Option[String] = None, lemma: Option[String] = None, compMorpho: Option[String] = None,
  stem: Option[String] = None, iobEntity: Vector[String] = Vector.empty[String], chunk: Option[String] = None, id: String = "")

case class News(id: String, urlWebSite: String, urlNews: String, title: Option[String], summary: Option[String],
  newsDate: Option[DateTime], corpus: Option[String] = None, tags: Option[Set[String]] = None,
  metaDescription: Option[String] = None, metaKeyword: Option[String] = None, canonicalUrl: Option[String] = None,
  topImage: Option[String] = None, nlp: Option[Nlp] = None)

case class Nlp(title: Option[IndexedSeq[Word]], summary: Option[IndexedSeq[Word]], corpus: Option[IndexedSeq[Word]], 
  description: Option[IndexedSeq[Word]], crimes: Option[IndexedSeq[String]] = None, addresses: Option[IndexedSeq[String]] = None,
  persons: Option[IndexedSeq[String]] = None, locations: Option[IndexedSeq[String]] = None, dates: Option[IndexedSeq[String]] = None,
  organizations: Option[IndexedSeq[String]] = None, nlpTags: Option[Map[String, Double]] = None)

case class Lemma(id: String, word: String, lemma: Option[String] = None, features: Option[String] = None)

case class City(id: String, city_name: String, cap: Option[String], province: Option[String], province_code: Option[String],
  region: Option[String], region_code: Option[String], state: Option[String])

case class Crime(id: String, word: String, lemma: Option[String] = None, stem: Option[String] = None, tipo: Option[String] = None)

case class Address(id: String, street: String, cap: Option[String] = None, city: Option[String] = None, province: Option[String] = None,
  state: Option[String] = None, region: Option[String] = None)
