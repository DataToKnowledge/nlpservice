package it.wheretolive.nlp.pipeline

import it.wheretolive.nlp.Model._

/**
 * Created by fabiofumarola on 08/01/15.
 */
object MessageProtocol {

  case object Process

  case class FetchData(indexed: Boolean = false)

  case class Data(newsList: List[CrawledNews])

  case class ProcessItem(
    news: CrawledNews,
    nlp: Option[Nlp] = Option.empty,
    tags: Option[Seq[Tag]] = Option.empty,
    namedEntities: Option[NamedEntities] = Option.empty,
    focusLocation: Option[Location] = Option.empty,
    focusDate: Option[String] = Option.empty,
    analyzedNewsSaved: Option[Int] = Option.empty,
    indexId: Option[String] = Option.empty)

  case object GetLoad

  case class ItemProcessingError(itemId: String,
                                 message: String, error: Option[Throwable])
}
