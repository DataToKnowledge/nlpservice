package it.wheretolive.nlp.utils

import it.wheretolive.nlp.Model.{AnalyzedNews, IndexedNews}
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

  def extractNewsToIndex(procNews: ProcessItem): IndexedNews = {

    val newspaper = matchUrlWebSite(procNews.news.urlWebSite)

    val filteredEntities = procNews.namedEntities.map { ent =>
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

  def extractNewsToIndex(aNews: AnalyzedNews): IndexedNews = {

    val filteredEntities = aNews.namedEntities.map { ent =>
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
