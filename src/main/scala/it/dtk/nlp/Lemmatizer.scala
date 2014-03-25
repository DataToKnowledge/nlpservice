package it.dtk.nlp

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONObjectID, BSONDocument}
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object Lemmatizer {

  val driver = new MongoDriver

}

case class Lemma(id: Option[BSONObjectID] = None, word: Option[String], lemma: Option[String], features: Option[String])

object Lemma {

  implicit object LemmaBSONReader extends BSONDocumentReader[Lemma] {
    def read(doc: BSONDocument): Lemma = {
      Lemma(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("word"),
        doc.getAs[String]("lemma"),
        doc.getAs[String]("features")
      )
    }
  }

  implicit object LemmaBSONWriter extends BSONDocumentWriter[Lemma] {
    def write(lemma: Lemma): BSONDocument = BSONDocument(
      "word" -> lemma.word,
      "lemma" -> lemma.lemma,
      "features" -> lemma.features
    )
  }

}

class Lemmatizer(host: String, database: String) {

  import Lemmatizer._

  val connection = driver.connection(List(host))
  val db = connection.db(database)
  val lemmas:BSONCollection = db.collection("lemmas")

  /**
   * Lemmatiza a given token using morph-it
   *
   * @param word input token
   * @return its lemma
   */
  def getLemma(word: String): Future[Option[String]] = {
    val query = BSONDocument("word" -> word)
    val filter = BSONDocument("_id" -> 0, "word" -> 0, "lemma" -> 1, "features" -> 0)

    lemmas.find(query, filter).cursor[Lemma].headOption.map(_.get.lemma)
  }

  /**
   * Convenience method to lemmatize a Word
   *
   * @param word
   * @return
   */
  def getLemma(word: Word): Future[Word] = {
    val f = getLemma(word.token)
    val p = Promise[Word]()

    f onComplete {
      case Success(l) =>
        p success word.copy(lemma = l)
      case Failure(ex) =>
        p failure ex
    }

    p.future
  }

}
