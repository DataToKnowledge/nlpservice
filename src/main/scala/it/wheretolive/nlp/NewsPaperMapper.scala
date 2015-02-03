package it.wheretolive.nlp

/**
 * Created by fabiofumarola on 02/02/15.
 */
trait NewsPaperMapper {

  /**
   *
   * @param urlWebSite
   * @return the name of the newspaper
   */
  def map(urlWebSite: String): String =
    urlWebSite match {
      case value: String if value.contains("baritoday") =>
        "BariToday"

      case value: String if value.contains("brindisilibera") =>
        "BrindisiLibera"

      case value: String if value.contains("brindisireport") =>
        "BrindisiReport"

      case value: String if value.contains("corrieredelmezzogiorno") =>
        "Corriere del Mezzogiorno"

      case value: String if value.contains("lecceprima") =>
        "LeccePrima"

      case value: String if value.contains("puglia24") =>
        "Puglia24"

      case value: String if value.contains("bari.repubblica") =>
        "Bari Repubblica"

      case value: String if value.contains("senzacolonne") =>
        "SenzaColonne"

      case value: String if value.contains("corrieresalentino") =>
        "Corriere Salentino"

      case value: String if value.contains("quotidianodipuglia") =>
        "Quotidiano di Puglia"

      case _ =>
        ""

    }
}
