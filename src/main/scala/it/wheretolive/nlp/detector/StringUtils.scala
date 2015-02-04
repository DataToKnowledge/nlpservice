package it.wheretolive.nlp.detector

/**
 * Created by fabiofumarola on 04/02/15.
 */
trait StringUtils {

  /**
   *
   * @param list
   * @return string to lowercase except the first letter
   */
  def standardiseNames(list: List[String]): List[String] =
    list.map(standardiseName)

  def standardiseName(name: String) = {
    val lower = name.toLowerCase
    val head = lower.head.toUpper
    head.toString + lower.tail
  }
}
