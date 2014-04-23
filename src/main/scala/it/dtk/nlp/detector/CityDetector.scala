package it.dtk.nlp.detector

import it.dtk.nlp.db.{City, DBManager, Word}
import org.slf4j.LoggerFactory
import scala.util.Try

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object CityDetector extends Detector {

  /**
   * Maximum number of tokens for a city
   */
  private val RANGE = 3

  /**
   * City name regular expression
   */
  private val CITIES_R = "^[A-Z](\\w+|\\')[\\w\\s\\']*"

  private val log = LoggerFactory.getLogger("CityDetector")

  override def detect(sentence: Seq[Word]): Try[Seq[Word]] = Try {
    var result = Vector.empty[Word]

    def bumpEndIndex(offset: Int) = {
      if (offset + RANGE >= sentence.length) sentence.length - 1
      else offset + RANGE
    }

    var startIndex: Int = 0
    var endIndex = bumpEndIndex(startIndex)

    while (startIndex < sentence.length) {
      val city = sentence.slice(startIndex, endIndex + 1).map(word => word.token).mkString(sep = " ")

      // verifico se questa sequenze di word matcha l'espressione regolare
      if (city.matches(CITIES_R)) {
        DBManager.findCity(city) match {

          // ho trovato una corrispondenza nel DB, flaggo le word come City e sposto la finestra
          // di N posizioni, dove N e` il numero di word che ho flaggato
          case Some(res: City) =>
            log.info(s"Found city: ${res.city_name}")

            val currentWord = sentence.apply(startIndex)
            result :+= currentWord.copy(iobEntity = currentWord.iobEntity + "B-CITY")

            while (startIndex < endIndex) {
              startIndex += 1
              val nextWord = sentence.apply(startIndex)
              result :+= nextWord.copy(iobEntity = nextWord.iobEntity + "I-CITY")
            }

            startIndex += 1
            endIndex = bumpEndIndex(startIndex)

          // nessuna corrispondenza nel DB
          case None =>
            log.debug(s"Cannot find a city named: $city")
            // stringo la finestra di 1 posizione oppure la ri-calibro
            if (startIndex < endIndex) {
              endIndex -= 1
            } else {
              result :+= sentence.apply(startIndex)

              startIndex += 1
              endIndex = bumpEndIndex(startIndex)
            }
        }
      } else {
        // non matcha l'espressione regolare, muovo la finestra di 1 posizione
        result :+= sentence.apply(startIndex)

        startIndex += 1
        endIndex += 1
      }

    }

    result.toSeq
  }

}
