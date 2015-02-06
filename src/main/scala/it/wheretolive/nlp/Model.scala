package it.wheretolive.nlp

import org.joda.time.DateTime

/**
 * Created by fabiofumarola on 08/01/15.
 */
object Model {

  case class CrawledNews(
    id: String,
    urlWebSite: String,
    urlNews: String,
    title: String,
    summary: String,
    newsDate: Option[DateTime],
    corpus: String,
    tags: Set[String],
    metaDescription: String,
    metaKeyword: String,
    canonicalUrl: String,
    topImage: Option[String],
    nlpAnalyzed: Boolean = false)

  case class AnalyzedNews(
    news: CrawledNews,
    nlp: Option[Nlp],
    namedEntities: Option[NamedEntities],
    tags: Option[Seq[Tag]],
    focusLocation: Option[Location],
    focusDate: Option[String])

  case class IndexedNews(
    newspaper: Option[String],
    urlWebSite: String,
    urlNews: String,
    imageLink: Option[String],
    title: String,
    summary: String,
    corpus: String,
    focusDate: Option[String],
    focusLocation: Option[Location],
    namedEntities: Option[NamedEntities],
    tags: Option[Seq[Tag]])

  case class Word(
    tokenId: Int,
    sentence: String,
    tokenStart: Int,
    tokenEnd: Int,
    token: String,
    posTag: String,
    wordNetPos: String,
    lemma: String,
    iobEntity: String,
    chunk: String)

  case class Nlp(
    title: Seq[Word],
    summary: Seq[Word],
    corpus: Seq[Word],
    description: Seq[Word])

  case class NamedEntities(
    crimes: Seq[String] = Seq(),
    related: Seq[String] = Seq(),
    addresses: Seq[String] = Seq(),
    persons: Seq[String] = Seq(),
    locations: Seq[String] = Seq(),
    geopoliticals: Seq[String] = Seq(),
    organizations: Seq[String] = Seq(),
    dates: Seq[String] = Seq())

  case class Tag(
    name: String,
    score: Double)

  case class City(
    id: String,
    city_name: String,
    cap: Option[String],
    province: Option[String],
    province_code: Option[String],
    region: Option[String],
    region_code: Option[String],
    state: Option[String])

  case class Crime(
    id: String,
    name: String,
    _type: String)

  //  case class NewsToIndex(
  //    urlWebsite: String,
  //    urlNews: String,
  //    title: String,
  //    summary: String,
  //    corpus: String,
  //    date: String,
  //    crimes: Seq[String],
  //    persons: Seq[String],
  //    locations: Seq[String],
  //    organizations: Seq[String],
  //    geopoliticals: Seq[String],
  //    mainLocation: String,
  //    position: GeoPoint,
  //    nlp_tags: Seq[WordCount])

  //  case class GeoPoint(
  //    lat: Double,
  //    lon: Double)

  //  case class WordCount(
  //    name: String,
  //    rank: Double)

  //from GFoss Index
  case class Location(
    city_name: String,
    province_name: String,
    region_name: String,
    population: String,
    wikipedia_url: String,
    geoname_url: String,
    geo_location: String)

  object NewsPart extends Enumeration {
    type NewsPart = Value
    val Title, Summary, Corpus, Description = Value
  }

  val EmptyEntity = "O"

  object EntityType extends Enumeration {
    type EntityType = Value
    val B_CITY, I_CITY, B_ADDRESS, I_ADDRESS, B_CRIME, I_RELATED,B_RELATED, I_CRIME, B_DATE, I_DATE, B_PER, I_PER, B_ORG, I_ORG, B_LOC, I_LOC, B_GPE, I_GPE, O = Value

    def stringValue(value: EntityType) =
      value.toString().replace("_", "-")

    def enumValue(str: String): EntityType =
      EntityType.Value(str.replace("-", "_"))
  }
}

