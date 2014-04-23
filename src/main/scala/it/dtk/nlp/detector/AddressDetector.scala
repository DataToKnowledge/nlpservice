package it.dtk.nlp.detector

import it.dtk.nlp.db.{Address, DBManager, Word}
import org.slf4j.LoggerFactory
import scala.util.Try

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
object AddressDetector extends Detector {

  /**
   * Maximum number of tokens for an address
   */
  private val RANGE = 4

  /**
   * Max range in witch civic number can be matched
   */
  private val CIVIC_RANGE = 3

  /**
   * Addresses prefix sanification regular expressions
   */
  private val SANIFICATION_R = Vector(
    ("v\\.le|vle|v le", "viale"),
    ("p\\.zzale|p\\.le|p\\.ze", "piazzale"),
    ("p\\.za|p\\. za|p\\.zza|p zza|pzza|pza|p/zza", "piazza"),
    ("p\\.tta", "piazzetta"),
    ("v.co", "vico"),
    ("cso|c so|c\\.so|c/so", "corso"),
    ("c/ da|c/da|cda", "contrada"),
    ("v\\.lo", "vicolo"),
    ("l\\.go|l go|lgo", "largo"),
    ("n&deg;|n.ro|civico numero|civico", "numero"),
    ("circonv\\.", "circonvallazione"),
    ("str|str\\.|sda", "strada"),
    ("ss|s\\.s\\.|", "ss"),
    ("v\\.", "via")
  )

  private val PREFIX_R = "(via|viale|piazza|piazzale|piazzetta|vico|" +
    "corso|contrada|vicolo|largo|numero|circonvallazione|strada|ss){1,1}"

  private val log = LoggerFactory.getLogger("AddressDetector")

  override def detect(sentence: Seq[Word]): Try[Seq[Word]] = _detect(sentence, cityName = None)

  def detect(sentence: Seq[Word], cityName: String): Try[Seq[Word]] = _detect(sentence, Option(cityName))

  private def _detect(sentence: Seq[Word], cityName: Option[String]): Try[Seq[Word]] = Try {
    var result = Vector.empty[Word]

    def bumpEndIndex(offset: Int) =  {
      if (offset + RANGE >= sentence.length) sentence.length - 1
      else offset + RANGE
    }

    def sanitize(token: String): String = {
      SANIFICATION_R.foreach(r => if (token.matches(r._1)) return r._2)
      token
    }

    var startIndex: Int = 0
    var endIndex = bumpEndIndex(startIndex)

    while (startIndex < sentence.length) {
      // Sanitize address prefix
      val sanitizedPrefix = sanitize(sentence.apply(startIndex).token)

      // If prefix is Via, Corso, Piazza, ...
      if (sanitizedPrefix.matches("(?i)" + PREFIX_R + "$")) {
        val address = sentence.slice(startIndex, endIndex + 1).map(word => word.token).mkString(sep = " ")

        DBManager.findAddress(address, cityName) match {
          // Address found on DB
          case Some(res: Address) =>
            log.info(s"Found address: ${res.street} (City: ${res.city.getOrElse("None")})")

            val currentWord = sentence.apply(startIndex)
            result :+= currentWord.copy(iobEntity = currentWord.iobEntity + "B-ADDRESS")

            while (startIndex < endIndex) {
              startIndex += 1
              val nextWord = sentence.apply(startIndex)
              result :+= nextWord.copy(iobEntity = nextWord.iobEntity + "I-ADDRESS")
            }

            // TODO: Civic number discovery

            startIndex += 1
            endIndex = bumpEndIndex(startIndex)

          // No address found on DB
          case None =>
            log.debug(s"Cannot find an address named: $address")
            // Recalibrating window
            if (startIndex < endIndex) {
              endIndex -= 1
            } else {
              result :+= sentence.apply(startIndex)

              startIndex += 1
              endIndex = bumpEndIndex(startIndex)
            }
        }
      } else {
        // Slide window
        result :+= sentence.apply(startIndex)

        startIndex += 1
        endIndex += 1
      }

    }

    result.toSeq
  }

}
