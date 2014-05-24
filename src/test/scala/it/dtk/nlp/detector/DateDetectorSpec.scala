package it.dtk.nlp.detector

import it.dtk.nlp.{ TreeTagger, TextPreprocessor }
import org.scalatest.{ Matchers, FlatSpec }
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.net.URL
import org.scalatest.concurrent.{ Futures, ScalaFutures }
import scala.util.Success
import scala.util.Failure

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
class DateDetectorSpec extends FlatSpec with Matchers with Futures with ScalaFutures {

  "A DateDetector" should "tag all detected dates in a sentence" in {
    val sentence = "a b 28 Gennaio 2013 A B " +
      "c 28 Febbraio 2014 b 12/10/2015 " +
      "a Febbraio a b Lunedì 34 Gennaio 2016 " +
      "Lunedì 12 Marzo 2018 d E 3 Aprile '12"

    whenReady(TreeTagger.tag(TextPreprocessor.apply(sentence))) {
      words =>
        val results = DateDetector.detect(words)

        results match {
          case Success(res) =>
            res.size should be(words.size)
            res.count(w => w.iobEntity.contains(EntityType.stringValue(EntityType.B_DATE))) should be(4)
            res.count(w => w.iobEntity.contains(EntityType.stringValue(EntityType.I_DATE))) should be(11)
          case Failure(ex) =>
            ex.printStackTrace()
        }

    }
  }

  it should "return the same vector if no dates are detected" in {
    val sentence = "a b A B c b a Febbraio a b Lunedì 34 Gennaio 2016 d E"
    whenReady(TreeTagger.tag(TextPreprocessor.apply(sentence))) {
      words =>
        val results = DateDetector.detect(words)

        results match {
          case Success(res) =>
            res.size should be(words.size)
            res.count(w => w.iobEntity.nonEmpty) should be(0)
          case Failure(ex) =>
            ex.printStackTrace()
        }
    }
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
      "http://bari.repubblica.it/cronaca/2014/03/24/news/anno_accademico-81750197/")

    urls foreach {
      urlStr =>
        val url = new URL(urlStr)
        val actualDate = DateDetector getDateFromURL url

        actualDate shouldBe a[Some[_]]
        actualDate.get shouldBe a[DateTime]
        actualDate.get should equal(expectedDate)
    }

    val url = new URL("http://www.baritoday.it/cronaca/spaccio-scuola-locali-triggiano-arresti-24-marzo-201.html")
    DateDetector getDateFromURL url should be(None)
  }

}
