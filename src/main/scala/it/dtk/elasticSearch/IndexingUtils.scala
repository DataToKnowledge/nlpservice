package it.dtk.elasticSearch

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import scala.util.Try
import rapture.net._
import rapture.core._
import rapture.uri._
import rapture.io._
import rapture.fs._
import com.typesafe.config.ConfigFactory
import org.json4s._
import org.json4s.jackson.JsonMethods._
import it.dtk.nlp.db._
import org.joda.time.format.ISODateTimeFormat

object IndexingUtils {
  val dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withLocale(Locale.ITALY).withZoneUTC()
}

case class Address(latitude: Double, longitude: Double, country: String, city: Option[String], state: String, zipcode: Option[String],
  streetName: Option[String], streetNumber: Option[String], countryCode: String)

class IndexingUtils(val geocodingCacheAddress: String) {

  import IndexingUtils._
  val printDateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withZoneUTC() //ISODateTimeFormat.basicDateTime().withZoneUTC()

  /**
   * @param n
   * @return convertire il tipo news memorizzato mongo nel tipo newsEs che e' ottimizzato per ElasticSearch
   */
  def newsToNewsEs(n: News): Option[NewsES] =
    if (n.title.isEmpty || n.nlp.isEmpty)
      None
    else n.nlp.map { nlp =>
      NewsES(n.urlWebSite, n.canonicalUrl.getOrElse(n.urlNews), n.title, n.summary, n.corpus, dateExtractorAsString(nlp.dates, n.newsDate),
        nlp.crimes, nlp.persons, nlp.locations, nlp.organizations, nlp.geopoliticals, positionsExtractor(nlp.addresses, nlp.locations),
        nlp.nlpTags.map(mapToTag))
    }

  private def mapToTag(map: Map[String, Double]): Seq[Tag] =
    map.toSeq.map(kv => Tag(kv._1, kv._2))

  /**
   * @param date
   * @param strDates
   * @return the most recent date from strDates or date
   *
   * prende come data o la data di estrazione o una data rilevata nel testo se precendente a quella di estrazione
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
          if (addr.isSuccess)
          ad <- addr.get
        } yield ad

        val result = for {
          value <- addresses
          if (value.city.isDefined)
          if (locationsSet.contains(value.city.get.toLowerCase()))
        } yield new GeoPoint(value.latitude, value.longitude)

        if (result.isEmpty)
          locationExtractor(locationsSet.toList)
        else result

      case (None, Some(locs)) =>

        for {
          loc <- locs.map(findCity)
          if (loc.isSuccess)
          l <- loc.get
        } yield new GeoPoint(l.latitude, l.longitude)

      case (_, _) =>
        Seq.empty[GeoPoint]
    }

  }

  /**
   * @param strLocations
   * @return
   */
  private def locationExtractor(strLocations: Seq[String]): Seq[GeoPoint] = {

    val locations = for {
      tryLocations <- strLocations.map(findCity)
      if (tryLocations.isSuccess)
      loc <- tryLocations.get
      if (loc.countryCode == "IT")
    } yield loc

    locations.map(l => new GeoPoint(l.latitude, l.longitude)).toSeq

  }

  import strategy.throwExceptions
  implicit val enc = Encodings.`UTF-8`
  implicit val formats = DefaultFormats // Brings in default date formats etc.

  def findCity(city: String): Try[List[Address]] = webServiceCall("city", city)

  def findAddress(address: String): Try[List[Address]] = webServiceCall("address", address)

  def webServiceCall(service: String, param: String): Try[List[Address]] = Try {
    //get the json
    val url: HttpUrl = (Http / geocodingCacheAddress / "google" / s"?$service=${param.replace(" ", "+")}")
    val body = url.slurp[Char]

    //convert the json to address
    val json = parse(body)

    try {
      json.extract[List[Address]].distinct
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }
  }

}