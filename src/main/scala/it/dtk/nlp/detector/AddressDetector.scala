package it.dtk.nlp.detector

import it.dtk.nlp.db.{ Address, DBManager, Word }
import org.slf4j.LoggerFactory
import scala.util.Try
import EntityType._
import scala.collection.immutable.TreeMap

/**
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 */
object AddressDetector {

  /**
   * Maximum number of tokens for an address
   */
  private val range = 4

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
    ("v\\.", "via"))

  private val PREFIX_R = "(via|viale|piazza|piazzale|piazzetta|vico|" +
    "corso|contrada|vicolo|largo|numero|circonvallazione|strada|ss){1,1}"

  private val log = LoggerFactory.getLogger("AddressDetector")

  def detect(words: IndexedSeq[Word], cityName: Option[String] = None): Try[Seq[Word]] = Try {

    def normalizeToken(word: Word): Word = {
      val result = SANIFICATION_R.foldLeft(Option.empty[String]) { (opt, r) =>
        if (opt.isDefined)
          opt
        else if (word.token.matches(r._1))
          Option(r._2)
        else
          opt
      }
      if (result.isDefined)
        word.copy(token = result.get)
      else word
    }

    val normalizedWords = words.map(w => normalizeToken(w))
    
    //create a map of words ordered by tokenId
    var mapWords = normalizedWords.map(w => w.tokenId.get -> w).toMap
    var taggedTokenId = Set.empty[Int]
    //var entities = Vector.empty[String]

    def tag(slide: IndexedSeq[Word], pos: Int, value: EntityType): Option[Word] = {
      val tokenId = slide(pos).tokenId.get
      mapWords.get(tokenId).map(w => w.copy(iobEntity = w.iobEntity :+ EntityType.stringValue(value)))
    }

    for (sizeNGram <- range to 1 by -1) {
      val sliding = words.sliding(sizeNGram)

      for (slide <- sliding) {

        if (slide(0).token.matches("(?i)" + PREFIX_R + "$")) {
          val candidate = slide.map(_.token).mkString(" ")

          DBManager.findAddress(candidate).foreach { address =>
            
            var entity = ""

            for (j <- 0 until slide.size) {
              val word = if (j == 0)
                tag(slide, j, EntityType.B_ADDRESS)
              else
                tag(slide, j, EntityType.I_ADDRESS)

              if (word.isDefined && !taggedTokenId.contains(word.get.tokenId.get)) {
                mapWords += (word.get.tokenId.get -> word.get)
                taggedTokenId += word.get.tokenId.get
                //entity += " " + word.get.token
              }
              //entities = entities :+ entity
            }

            // TODO: Civic number discovery
          }
        }
      }
    }

    //return the sequence of the words where some words are annotated with entity
    TreeMap(mapWords.toArray: _*).values.toIndexedSeq
  }

}
