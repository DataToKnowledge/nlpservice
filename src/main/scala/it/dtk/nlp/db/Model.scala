package it.dtk.nlp.db

import org.joda.time.DateTime
import com.sksamuel.elastic4s.source.DocumentMap
import scala.util.Try
import org.joda.time.format.DateTimeFormatter
import scala.language.implicitConversions
import org.joda.time.LocalDate

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */

case class News(id: String, urlWebSite: String, urlNews: String, title: Option[String], summary: Option[String],
  newsDate: Option[DateTime], corpus: Option[String] = None, tags: Option[Set[String]] = None,
  metaDescription: Option[String] = None, metaKeyword: Option[String] = None, canonicalUrl: Option[String] = None,
  topImage: Option[String] = None, nlpAnalyzed: Option[Boolean], nlp: Option[Nlp] = None)

case class Nlp(title: Option[Seq[Word]], summary: Option[Seq[Word]], corpus: Option[Seq[Word]],
  description: Option[Seq[Word]], crimes: Option[Seq[String]] = None, addresses: Option[Seq[String]] = None,
  persons: Option[Seq[String]] = None, locations: Option[Seq[String]] = None, geopoliticals: Option[Seq[String]] = None,
  dates: Option[Seq[String]] = None, organizations: Option[Seq[String]] = None, nlpTags: Option[Map[String, Double]] = None)

case class Word(token: String, tokenId: Option[Int] = None, tokenStart: Option[Int] = None, tokenEnd: Option[Int] = None,
  sentence: Option[String] = None, posTag: Option[String] = None, lemma: Option[String] = None, compMorpho: Option[String] = None,
  stem: Option[String] = None, iobEntity: Vector[String] = Vector.empty[String], chunk: Option[String] = None, id: String = "")

case class Lemma(id: String, word: String, lemma: Option[String] = None, features: Option[String] = None)

case class City(id: String, city_name: String, cap: Option[String], province: Option[String], province_code: Option[String],
  region: Option[String], region_code: Option[String], state: Option[String])

case class Crime(id: String, word: String, lemma: Option[String] = None, stem: Option[String] = None, tipo: Option[String] = None)

case class Address(id: String, street: String, cap: Option[String] = None, city: Option[String] = None, province: Option[String] = None,
  state: Option[String] = None, region: Option[String] = None)

case class NewsES(urlWebsite: String, urlNews: String, title: Option[String],
  summary: Option[String], corpus: Option[String], date: Option[String], crimes: Option[Seq[String]],
  persons: Option[Seq[String]], locations: Option[Seq[String]], organizations: Option[Seq[String]],
  geopoliticals: Option[Seq[String]], positions: Seq[GeoPoint], nlp_tags: Option[Seq[Tag]])

case class GeoPoint(lat: Double, lon: Double)

case class Tag(name: String, rank: Double)




