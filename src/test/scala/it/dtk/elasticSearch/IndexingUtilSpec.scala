package it.dtk.elasticSearch

import it.dtk.nlp.BaseTestClass
import org.joda.time.DateTime

object IndexingUtilSpec {

  val date1 = "22/1/2014"
  val date2 = "22/12/2013"
  val date3 = "12/1/2012"
  val date4 = "22/12/2010"
  val date5 = "10/07/2014"

  val dates = Option(Seq(date1, date2, date3, date4))
  val emptyDates = Option(Seq[String]())

  val todayNewsDate = Option(IndexingUtils.dateFormatter.parseDateTime(date5))
  val oldNewsDate = Option(IndexingUtils.dateFormatter.parseDateTime("1/1/2010"))
  val emptyNewsDate = Option.empty[DateTime]

  val addresses = Option(Seq("via Michelangelo", "via Martano"))
  val emptyAddresses = Option(Seq[String]())

  val locations = Option(Seq("GRECI",
    "Lecce",
    "Martano",
    "Maglie",
    "Greci",
    "GRECI",
    "Lecce",
    "Martano",
    "Maglie",
    "Greci",
    "Martano",
    "Andrano"))

  val emptyLocations = Option(Seq[String]())

  val addresses2 = Option(Seq("via Santo Stefano", "via Santo Stefano"))
  val locations2 = Option(Seq("NOVOLI",
    "Santa Croce",
    "Novoli",
    "Santo Stefano",
    "NOVOLI",
    "Santa Croce",
    "Novoli",
    "Santo Stefano",
    "Lecce",
    "Vito",
    "Lecce",
    "Lecce"))

  val addresses3 = Option(Seq("via viterbo"))
  val locations3 = Option(Seq("OSTUNI"))
  val addressWebServiceUrl = "10.0.0.11:8080"

}

class IndexingUtilSpec extends BaseTestClass {

  import IndexingUtilSpec._

  val indexer = new IndexingUtils(addressWebServiceUrl)

  "An IndexingUtil" should {

    val rightDate = Option(IndexingUtils.dateFormatter.parseDateTime(date1))

    s"return $date1 if the input for date is $dates and for newsDate is ${todayNewsDate.get.toLocalDate()}" in {
      indexer.dateExtractor(dates, todayNewsDate) should equal(rightDate)
    }

    s"return ${oldNewsDate.get.toLocalDate()} if the inputs are $dates and ${oldNewsDate.get.toLocalDate()}" in {
      indexer.dateExtractor(dates, oldNewsDate) should equal(oldNewsDate)
    }

    s"return ${oldNewsDate.get.toLocalDate()} if dates is $emptyDates" in {
      indexer.dateExtractor(emptyDates, todayNewsDate) should equal(todayNewsDate)
    }

    s"return ${rightDate.get.toLocalDate()} if newsDate for input $dates and $emptyNewsDate" in {
      indexer.dateExtractor(dates, emptyNewsDate) should equal(rightDate)
    }

    s"return ${Option.empty} for $emptyNewsDate and $emptyDates" in {
      indexer.dateExtractor(emptyDates, emptyNewsDate) should equal(None)
    }
  }

  it should {
    s"[Case got position from address with a matching location] return a non empty position list for $addresses and $locations" in {
      val result = indexer.positionsExtractor(addresses, locations)
      result.length should be (1)
      //result.foreach(println)
    }

    s"[Case got position from locations] return a non empty position list for $addresses2 and $locations2" in {
      val result = indexer.positionsExtractor(addresses2, locations2)
      result.length should be > 0
      //result.foreach(println)
    }

    s"return a non empty position list for $addresses3 and $locations3" in {
      val result = indexer.positionsExtractor(addresses3, locations3)
      result.length should be(1)
      //result.foreach(println)
    }

    s"return a non empty position list for $addresses and $emptyLocations" in {
      val result = indexer.positionsExtractor(addresses, locations)
      result.length should be > 0
      //result.foreach(println)
    }

    s"return a position list with size ${locations.get.map(_.toLowerCase()).distinct.size} for locations= $locations and addresses= $emptyAddresses" in {
      val result = indexer.positionsExtractor(emptyAddresses, locations)
      result.length should be > (7)
      //result.foreach(println)
    }

    "for empty locations and addresses should return an empty sequence" in {
      indexer.positionsExtractor(emptyAddresses, emptyLocations) should have length 0
    }
  }

}