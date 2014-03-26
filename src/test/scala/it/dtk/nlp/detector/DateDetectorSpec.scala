package it.dtk.nlp.detector

import it.dtk.nlp.TextPreprocessor
import org.scalatest.{Matchers, FlatSpec}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.net.URL

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
class DateDetectorSpec extends FlatSpec with Matchers {

  "A DateDetector" should "detect all dates in a sentence" in {
    val sentence = "a b 28 Gennaio 2013 A B " +
      "c 28 Febbraio 2014 b 12/10/2015 " +
      "a Febbraio a b Lunedì 34 Gennaio 2016 " +
      "Lunedì 12 Marzo 2018 d E 3 Aprile '12"

    val words = TextPreprocessor.getTokens(sentence)
    val results = DateDetector.detect(words)

    results.size should be(4)
  }

  it should "return an empty vector if no dates are detected" in {
    val sentence = "a b A B c b a Febbraio a b Lunedì 34 Gennaio 2016 d E"
    val words = TextPreprocessor.getTokens(sentence)
    val results = DateDetector.detect(words)

    results.size should be(0)
  }

  it should "return datetime object of a given date" in {
    val expectedDate = DateTime.parse("28/01/2013", DateTimeFormat.forPattern("dd/MM/yyyy"))
    val actualDate = DateDetector toDate "28 Gennaio 2013"

    actualDate shouldBe a[Some[_]]
    actualDate.get shouldBe a[DateTime]
    actualDate.get should equal(expectedDate)

    DateDetector toDate "Martedì 35 Gennaio 2014" should be(None)
  }

  it should "detect a date in a URL" in {
    val expectedDate = DateTime.parse("24/03/2014", DateTimeFormat.forPattern("dd/MM/yyyy"))
    val urls = Vector(
      "http://bari.repubblica.it/cronaca/2014/03/24/news/abuso_d_ufficio_sospeso_il_sindaco_di_fasano_lello_di_bari-81768917/",
      "http://bari.repubblica.it/cronaca/2014/03/24/news/falsi_braccianti-81760445/",
      "http://bari.repubblica.it/cronaca/2014/03/24/news/fasano-81758511/",
      "http://bari.repubblica.it/cronaca/2014/03/24/news/anno_accademico-81750197/"
    )

    urls foreach {
      urlStr =>
        val url = new URL(urlStr)
        val actualDate = DateDetector getDateFromURL url

        actualDate shouldBe a[Some[_]]
        actualDate.get shouldBe a[DateTime]
        actualDate.get should equal(expectedDate)
    }

    val url = new URL("http://www.baritoday.it/cronaca/spaccio-scuola-locali-triggiano-arresti-24-marzo-201.html")
    DateDetector getDateFromURL url should be (None)
  }

}
