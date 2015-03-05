package it.wheretolive.nlp.utils

import it.wheretolive.nlp.Model.{ NamedEntities, IndexedNewsFlatten, AnalyzedNews, IndexedNews }
import it.wheretolive.nlp.pipeline.MessageProtocol.ProcessItem
import org.joda.time.format.DateTimeFormat

/**
 * Created by fabiofumarola on 02/02/15.
 */
trait AnalyzedNewsUtils {

  val dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm")

  /**
   *
   * @param urlWebSite
   * @return the name of the newspaper
   */
  private def matchUrlWebSite(urlWebSite: String): String =
    urlWebSite match {
      case value: String if value.contains("baritoday") =>
        "BariToday"

      case value: String if value.contains("brindisilibera") =>
        "BrindisiLibera"

      case value: String if value.contains("brindisireport") =>
        "BrindisiReport"

      case value: String if value.contains("corrieredelmezzogiorno") =>
        "Corriere del Mezzogiorno"

      case value: String if value.contains("lecceprima") =>
        "LeccePrima"

      case value: String if value.contains("puglia24") =>
        "Puglia24"

      case value: String if value.contains("bari.repubblica") =>
        "Bari Repubblica"

      case value: String if value.contains("senzacolonne") =>
        "SenzaColonne"

      case value: String if value.contains("corrieresalentino") =>
        "Corriere Salentino"

      case value: String if value.contains("quotidianodipuglia") =>
        "Quotidiano di Puglia"

      case value: String if value.contains("go-bari") =>
        "Go Bari"

      case _ =>
        ""

    }

  /**
   *
   * @param list
   * @return a non approriate method to filter duplicates
   */
  private def filterDuplicates(list: List[String]): List[String] = {
    list.map(e => e.toLowerCase -> e).toMap.values.toList
  }

  private def extractNewsToIndex(procNews: ProcessItem): IndexedNews = {

    val newspaper = matchUrlWebSite(procNews.news.urlWebSite)

    val filteredEntities = filterEntities(procNews.namedEntities)

    IndexedNews(
      newspaper = Option(newspaper),
      urlWebSite = procNews.news.urlWebSite,
      urlNews = procNews.news.urlNews,
      imageLink = procNews.news.topImage,
      title = procNews.news.title,
      summary = procNews.news.summary,
      corpus = procNews.news.corpus,
      focusDate = procNews.focusDate,
      focusLocation = procNews.focusLocation,
      namedEntities = filteredEntities,
      tags = procNews.tags
    )
  }

  def extractNewsToIndexFlatten(aNews: ProcessItem): IndexedNewsFlatten = {
    val filteredEntities = filterEntities(aNews.namedEntities)

    IndexedNewsFlatten(
      newspaper = Option(matchUrlWebSite(aNews.news.urlWebSite)),
      urlWebSite = aNews.news.urlWebSite,
      urlNews = aNews.news.urlNews,
      imageLink = aNews.news.topImage,
      title = aNews.news.title,
      summary = aNews.news.summary,
      corpus = aNews.news.corpus,
      focusDate = aNews.news.newsDate.map(d => dateFormatter.print(d)),
      cityName = aNews.focusLocation.map(_.city_name),
      provinceName = aNews.focusLocation.map(_.province_name),
      regionName = aNews.focusLocation.map(_.region_name),
      population = aNews.focusLocation.map(_.population),
      geoLocation = aNews.focusLocation.map(_.geo_location),
      crimes = aNews.namedEntities.map(_.crimes).getOrElse(List()),
      relateds = aNews.namedEntities.map(_.relateds).getOrElse(List()),
      crimeStems = aNews.namedEntities.map(_.crimeStems).getOrElse(List()),
      relatedStems = aNews.namedEntities.map(_.relatedStems).getOrElse(List()),
      addresses = aNews.namedEntities.map(_.addresses).getOrElse(List()),
      persons = aNews.namedEntities.map(_.persons).getOrElse(List()),
      locations = aNews.namedEntities.map(_.locations).getOrElse(List()),
      geopoliticals = aNews.namedEntities.map(_.geopoliticals).getOrElse(List()),
      organizations = aNews.namedEntities.map(_.organizations).getOrElse(List()),
      dates = aNews.namedEntities.map(_.dates).getOrElse(List()),
      tags = aNews.tags
    )
  }

  private def filterEntities(namedEntities: Option[NamedEntities]) =
    namedEntities.map { ent =>
      ent.copy(
        crimes = filterDuplicates(ent.crimes),
        relateds = filterDuplicates(ent.relateds),
        crimeStems = filterDuplicates(ent.crimes),
        relatedStems = filterDuplicates(ent.relateds),
        addresses = filterDuplicates(ent.addresses),
        persons = filterDuplicates(ent.persons),
        locations = filterDuplicates(ent.locations),
        geopoliticals = filterDuplicates(ent.geopoliticals),
        organizations = filterDuplicates(ent.organizations)
      )
    }

  def extractNewsToIndexFlatten(aNews: AnalyzedNews): IndexedNewsFlatten = {
    val filteredEntities = filterEntities(aNews.namedEntities)

    IndexedNewsFlatten(
      newspaper = Option(matchUrlWebSite(aNews.news.urlWebSite)),
      urlWebSite = aNews.news.urlWebSite,
      urlNews = aNews.news.urlNews,
      imageLink = aNews.news.topImage,
      title = aNews.news.title,
      summary = aNews.news.summary,
      corpus = aNews.news.corpus,
      focusDate = aNews.news.newsDate.map(d => dateFormatter.print(d)),
      cityName = aNews.focusLocation.map(_.city_name),
      provinceName = aNews.focusLocation.map(_.province_name),
      regionName = aNews.focusLocation.map(_.region_name),
      population = aNews.focusLocation.map(_.population),
      geoLocation = aNews.focusLocation.map(_.geo_location),
      crimes = aNews.namedEntities.map(_.crimes).getOrElse(List()),
      relateds = aNews.namedEntities.map(_.relateds).getOrElse(List()),
      crimeStems = aNews.namedEntities.map(_.crimeStems).getOrElse(List()),
      relatedStems = aNews.namedEntities.map(_.relatedStems).getOrElse(List()),
      addresses = aNews.namedEntities.map(_.addresses).getOrElse(List()),
      persons = aNews.namedEntities.map(_.persons).getOrElse(List()),
      locations = aNews.namedEntities.map(_.locations).getOrElse(List()),
      geopoliticals = aNews.namedEntities.map(_.geopoliticals).getOrElse(List()),
      organizations = aNews.namedEntities.map(_.organizations).getOrElse(List()),
      dates = aNews.namedEntities.map(_.dates).getOrElse(List()),
      tags = aNews.tags
    )
  }

  private def extractNewsToIndex(aNews: AnalyzedNews): IndexedNews = {

    val filteredEntities = filterEntities(aNews.namedEntities)

    IndexedNews(
      newspaper = Option(matchUrlWebSite(aNews.news.urlWebSite)),
      urlWebSite = aNews.news.urlWebSite,
      urlNews = aNews.news.urlNews,
      imageLink = aNews.news.topImage,
      title = aNews.news.title,
      summary = aNews.news.summary,
      corpus = aNews.news.corpus,
      focusDate = aNews.news.newsDate.map(d => dateFormatter.print(d)),
      focusLocation = aNews.focusLocation,
      namedEntities = filteredEntities,
      tags = aNews.tags
    )
  }
}
