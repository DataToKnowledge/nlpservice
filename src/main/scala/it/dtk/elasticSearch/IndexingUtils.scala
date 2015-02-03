package it.dtk.elasticSearch

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import scala.util.Try
import it.dtk.nlp.db._
import dispatch._ , Defaults._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.DefaultFormats

object IndexingUtils {
  val dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withLocale(Locale.ITALY).withZoneUTC()
}

case class Address(latitude: String, longitude: String, country: String, city: Option[String], state: String, zipcode: Option[String],
                   streetName: Option[String], streetNumber: Option[String], countryCode: String)

class IndexingUtils(val geocodingcacheAddress: String) {

  import IndexingUtils._

  val printDateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withZoneUTC() //ISODateTimeFormat.basicDateTime().withZoneUTC()

  /**
   * @param n
   * @return convertire il tipo news memorizzato mongo nel tipo newsEs che e' ottimizzato per ElasticSearch
   */
  def newsToNewsEs(n: News): Option[NewsES] =
    if (n.title.isEmpty || n.nlp.isEmpty)
      Option.empty[NewsES]
    else n.nlp.map { nlp =>
      NewsES(n.urlWebSite, n.canonicalUrl.getOrElse(n.urlNews), n.title, n.summary, n.corpus, dateExtractorAsString(nlp.dates, n.newsDate),
        nlp.crimes, nlp.persons, nlp.locations, nlp.organizations, nlp.geopoliticals, positionsExtractor(nlp.addresses, nlp.locations).distinct,
        nlp.nlpTags.map(mapToTag))
    }

  private def mapToTag(map: Map[String, Double]): Seq[Tag] =
    map.toSeq.map(kv => Tag(kv._1, kv._2))

  /**
   * @param date
   * @param strDates
   * @return the most recent date from strDates or date
   *
   *         prende come data o la data di estrazione o una data rilevata nel testo se precendente a quella di estrazione
   */
  def dateExtractor(strDates: Option[Seq[String]], date: Option[DateTime]): Option[DateTime] = {

    val formattedDates = strDates.map(_.map(dateFormatter.parseDateTime))
    formattedDates match {
      case Some(dates) =>
        val sorted = dates.sortWith(_ isAfter _)
        if (dates.length > 0)
          date.filter(d => d isBefore sorted.head).orElse(Option(sorted.head))
        else date

      case None =>
        date

    }
  }

  /**
   * @param strDates
   * @param date
   * @return estrae le date e le converte nel formato scelto in stringa
   */
  def dateExtractorAsString(strDates: Option[Seq[String]], date: Option[DateTime]): Option[String] =
    dateExtractor(strDates, date).map(d => printDateFormatter.print(d))

  /**
   * @param optAddresses
   * @param optLocations
   * @return
   */
  def positionsExtractor(optAddresses: Option[Seq[String]], optLocations: Option[Seq[String]]): Seq[GeoPoint] = {

    val distinctAddrs = optAddresses.map(_.distinct.toSeq)
    val distinctLocs = optLocations.map(_.map(_.toLowerCase()).distinct)

    (distinctAddrs, distinctLocs) match {

      case (Some(addrs), Some(locationsSet)) =>

        val addresses = for {
          addr <- addrs.map(findAddress)
          ad <- addr
        } yield ad

        val result = for {
          value <- addresses
          if (value.city.isDefined)
          if (locationsSet.contains(value.city.get.toLowerCase()))
        } yield new GeoPoint(value.latitude.toDouble, value.longitude.toDouble)

        if (result.isEmpty)
          cityExtractor(locationsSet.toList)
        else result.distinct

      case (None, Some(locs)) =>

        for {
          loc <- locs.map(findCity)
          l <- loc
        } yield new GeoPoint(l.latitude.toDouble, l.longitude.toDouble)


      case (_, _) =>
        Seq.empty[GeoPoint]
    }

  }

  /**
   * @param strLocations
   * @return
   */
  private def cityExtractor(strLocations: Seq[String]): Seq[GeoPoint] = {
    val italianLocations = strLocations.flatMap(str => findCity(str)).filter(_.countryCode == "IT")
    italianLocations.map(l => new GeoPoint(l.latitude.toDouble, l.longitude.toDouble)).distinct
  }


  def findCity(city: String): List[Address] = webServiceCall("city", city)

  def findAddress(address: String): List[Address] = webServiceCall("address", address)

  def webServiceCall(service: String, queryString: String): List[Address] = {

    implicit val formats = DefaultFormats // Brings in default date formats etc.

    val path = host(geocodingcacheAddress) / service / queryString
    val response = Http(path OK as.String).completeOption

    response.map (r => {
       parse(r).extract[List[Address]].distinct
    }).getOrElse(List[Address]())

  }

}