package it.dtk.nlp.db

import it.dtk.nlp.BaseTestClass

object DBManagerSpec {

  val lemma = "dormire"

  val address = "Corso Cavour"

  val city = "Bari"

  val crime = "rapina"

  val crimeText = "omicidio"
  
  val dbManager = new DBManager("10.0.0.11")

}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
class DBManagerSpec extends BaseTestClass {

  import DBManagerSpec._

  "A DBManager" when {

    "called" should {

      "return a lemma" in {
        dbManager.findLemma(lemma).get shouldBe a[Lemma]
      }

      "return a None when there is no lemma" in {
        dbManager.findLemma("") should be(None)
      }

      "return an address" in {
        val addr = dbManager.findAddress(address)

        addr.get shouldBe a[Address]
        addr.get.street should be(address)
        addr.get.city shouldBe a[Some[_]]
      }

      "return an address in text search" in {
        val addrs = dbManager.findAddressText(address)
        addrs.size should be > 0
      }

      "return an address in text search with city " in {
        val addrs = dbManager.findAddressText(address, Some(city))
        addrs.size should be > 0
      }

      "do not return an address in text search with city " in {
        val addrs = dbManager.findAddressText(address, Some("Fandomia"))
        addrs.size == 0
      }

      "do not return an address in text search" in {
        val addrs = dbManager.findAddressText("falso")
        addrs.size == 0
      }

      "return an address of a city" in {
        val addr = dbManager.findAddress(address, Some(city))

        addr.get shouldBe a[Address]
        addr.get.street should be(address)
        addr.get.city.get should be(city)
      }

      "return a None when there is no address" in {
        dbManager.findAddress("") should be(None)
      }

      "return a city" in {
        dbManager.findCity(city).get shouldBe a[City]
      }

      "return a None when there is no city" in {
        dbManager.findCity("") should be(None)
      }

      "return a crime" in {
        dbManager.findCrime(crime).get shouldBe a[Crime]
      }

      "return a crime from a text search" in {
        val r = dbManager.findCrimeText(crimeText)
        //r.foreach(println)
        r.length should be > 0
      }

      "should no return a crime from wrong a text search" in {
        val r = dbManager.findCrimeText("wrong")
        r.length == 0
      }

      "return a None when there is no crime" in {
        dbManager.findCrime("") should be(None)
      }

    }

  }

}
