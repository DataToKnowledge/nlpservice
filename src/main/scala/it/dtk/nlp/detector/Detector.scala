package it.dtk.nlp.detector

import it.dtk.nlp.db.Word
import scala.util.Try

object NewsPart extends Enumeration {
  type NewsPart = Value
  val Title, Summary, Corpus, Description = Value
}


object EntityType extends Enumeration {
  type EntityType = Value
  val B_CITY, I_CITY, B_ADDRESS, I_ADDRESS, B_CRIME, I_CRIME, B_DATE, I_DATE, B_PER, I_PER, B_ORG, I_ORG, B_LOC, I_LOC, B_GPE, I_GPE = Value

  def stringValue(value: EntityType) =
    value.toString().replace("_", "-")

  def enumValue(str: String): EntityType =
    EntityType.Value(str.replace("-", "_"))
}
