package it.dtk.nlp.detector

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.net.URL
import org.slf4j.LoggerFactory
import it.dtk.nlp.db.Word
import scala.util.Try
import EntityType._

/**
 * Implements recognition of date entities
 *
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 *
 */
object DateDetector {

  /* Maximum number of tokens for a date */
  val OFFSET = 6

  val YEAR_R = "((('''')?(\\s+)?)\\d{4}|\\d{2})"
  val COMPACT_MONTH_R = "(\\d{1,2})"
  val MONTH_R = "(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)"
  val DAY_R = "(\\d{1,2})"
  val DAY_OF_WEEK_R = "(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)"

  /* Regular expression to match compact dates (01/02/2012 or 01-02-2012 or 01.02.2012) */
  val COMPACT_DATE_R = DAY_R +
    "((\\s+)?(-|/|\\.)(\\s+)?)" +
    COMPACT_MONTH_R +
    "((\\s+)?(-|/|\\.)(\\s+)?)" +
    YEAR_R

  /* Regular expression to match long dates (21 dicembre 2012 or 21 dicembre '12) */
  val DATE_R = DAY_R + "\\s" + MONTH_R + "\\s" + YEAR_R

  /* We need this to match the single tokens */
  val DATE_TOKEN_R = YEAR_R + "|" + MONTH_R + "|" + DAY_R + "|" + DAY_OF_WEEK_R

  /* Regular expression to match full dates */
  val FULL_DATE_R = COMPACT_DATE_R + "|((" + DAY_OF_WEEK_R + "(,)?\\s+)?" + DATE_R + ")"

  val log = LoggerFactory.getLogger("DateDetector")

  def detectNew(words: IndexedSeq[Word]): Try[Seq[Word]] = Try {

    ???
  }
  
  def detect(sentence: Seq[Word]): Try[IndexedSeq[Word]] = Try {
    var nextIndex = 0
    var result = Vector.empty[Word]

    while (nextIndex < sentence.size) {
      val dateR = getNextDate(sentence, nextIndex)

      if (dateR.isDefined) {
        if (nextIndex < dateR.get.head)
          result ++= sentence.slice(nextIndex, dateR.get.head)

        sentence.slice(dateR.get.head, dateR.get.last + 1).foreach { w =>
          val prevIOB = w.iobEntity
          val newIOB = if (w == sentence.apply(dateR.get.head))
            prevIOB + EntityType.stringValue(EntityType.B_DATE)
          else prevIOB + EntityType.stringValue(EntityType.B_DATE)
          result :+= w.copy(iobEntity = w.iobEntity :+ newIOB)
        }
        val date = sentence.slice(dateR.get.head, dateR.get.last + 1).map(word => word.token).mkString(sep = " ")
        log.info(s"Found date: $date")
        nextIndex = dateR.get.last + 1
      } else {
        result :+= sentence.apply(nextIndex)
        nextIndex += 1
      }
    }
    result
  }

  def toDate(dateToken: String): Option[DateTime] = {
    val compactPattern = "(?i)" + COMPACT_DATE_R
    val longPattern = "(?i)" + DATE_R
    val fullPattern = "(?i)" + FULL_DATE_R

    if (dateToken.matches(compactPattern)) {
      // 12/10/2012 or 12/10/12 or 12-10-2012 or 12-10-12 or 12.10.2012 or 12.10.12
      // Day: group 1; Month: group 6; Year: group 11
      val dateFragments = compactPattern.r.findAllIn(dateToken)
      // FIXME: *** SCALA BUG *** this throws a IllegalStateException (fixed in v. 2.11.0-RC1)
      // https://issues.scala-lang.org/browse/SI-8215
      // WORKAROUND:
      dateFragments.hasNext
      val day = dateFragments.group(1)
      val month = dateFragments.group(6)
      val year = dateFragments.group(11)

      // Validate tokens
      if (!isValidDay(day)) {
        log.debug(s"Unable to validate day: $day (token: $dateToken)")
        return None
      } else if (!isValidMonth(month)) {
        log.debug(s"Unable to validate month: $month (token: $dateToken)")
        return None
      } else if (!isValidYear(year)) {
        log.debug(s"Unable to validate year: $year (token: $dateToken)")
        return None
      }

      // Sanitize year token
      val pYear = if (year.charAt(0) == '\'') year.replaceAll("\\'(\\s+)?", "") else year

      // Select correct date format
      val dateFormat = if (pYear.length == 2) "dd/MM/yy" else "dd/MM/yyyy"

      Some(DateTime.parse(day + "/" + month + "/" + pYear, DateTimeFormat.forPattern(dateFormat)))
    } else if (dateToken.matches(longPattern)) {
      // 12 Settembre 2012 or 12 Settembre '12
      // Day: group 1; Month: group 2; Year: group 3
      val dateFragments = longPattern.r.findAllIn(dateToken)
      // FIXME: *** SCALA BUG *** this throws a IllegalStateException (fixed in v. 2.11.0-RC1)
      // https://issues.scala-lang.org/browse/SI-8215
      // WORKAROUND:
      dateFragments.hasNext
      val day = dateFragments.group(1)
      val month = dateFragments.group(2)
      val year = dateFragments.group(3)

      // Validate tokens
      if (!isValidDay(day)) {
        log.debug(s"Unable to validate day: $day (token: $dateToken)")
        return None
      } else if (!isValidMonth(month)) {
        log.debug(s"Unable to validate month: $month (token: $dateToken)")
        return None
      } else if (!isValidYear(year)) {
        log.debug(s"Unable to validate year: $year (token: $dateToken)")
        return None
      }

      // Sanitize year token
      val pYear = if (year.charAt(0) == '\'') year.replaceAll("\\'(\\s+)?", "") else year

      // Select correct date format
      val dateFormat = if (pYear.length == 2) "dd/MM/yy" else "dd/MM/yyyy"

      Some(DateTime.parse(day + "/" + toCompactMonth(month).get + "/" + pYear, DateTimeFormat.forPattern(dateFormat)))
    } else if (dateToken.matches(fullPattern)) {
      // Mercoledi 12 Settembre 2012 or Mercoledi 12 Settembre '12
      // Mercoledi, 12 Settembre 2012 or Mercoledi, 12 Settembre '12
      // Day of week: 17; Day: group 19; Month: group 20; Year: group 21
      val dateFragments = fullPattern.r.findAllIn(dateToken)
      // FIXME: *** SCALA BUG *** this throws a IllegalStateException (fixed in v. 2.11.0-RC1)
      // https://issues.scala-lang.org/browse/SI-8215
      // WORKAROUND:
      dateFragments.hasNext
      // val dayOfWeek = dateFragments.group(17)
      val day = dateFragments.group(19)
      val month = dateFragments.group(20)
      val year = dateFragments.group(21)

      // Validate tokens
      if (!isValidDay(day)) {
        log.debug(s"Unable to validate day: $day (token: $dateToken)")
        return None
      } else if (!isValidMonth(month)) {
        log.debug(s"Unable to validate month: $month (token: $dateToken)")
        return None
      } else if (!isValidYear(year)) {
        log.debug(s"Unable to validate year: $year (token: $dateToken)")
        return None
      }

      // Sanitize year token
      val pYear = if (year.charAt(0) == '\'') year.replaceAll("\\'(\\s+)?", "") else year

      // Select correct date format
      val dateFormat = if (pYear.length == 2) "dd/MM/yy" else "dd/MM/yyyy"

      Some(DateTime.parse(day + "/" + toCompactMonth(month).get + "/" + pYear, DateTimeFormat.forPattern(dateFormat)))
    } else {
      log.error(s"Unable to match date token: $dateToken")
      None
    }
  }

  def getDateFromURL(url: URL): Option[DateTime] = {
    val YEAR_R = "(\\d{4})"
    val compactDateUrlPattern = "(?i)(.*)(/|-)(" + YEAR_R + "/" + COMPACT_MONTH_R + "/" + DAY_R + ")(.*)"
    val longDateUrlPattern = "(?i)(.*)(/|-)(" + DAY_R + "-" + MONTH_R + "-" + YEAR_R + ")(.*)"

    if (url.toString.matches(compactDateUrlPattern)) {
      val dateFragments = compactDateUrlPattern.r.findAllIn(url.toString)
      // FIXME: *** SCALA BUG *** this throws a IllegalStateException (fixed in v. 2.11.0-RC1)
      // https://issues.scala-lang.org/browse/SI-8215
      // WORKAROUND:
      dateFragments.hasNext
      val day = dateFragments.group(6)
      val month = dateFragments.group(5)
      val year = dateFragments.group(4)

      // Validate tokens
      if (!isValidDay(day)) {
        log.debug(s"Unable to validate day: $day (URL: $url)")
        return None
      } else if (!isValidMonth(month)) {
        log.debug(s"Unable to validate month: $month (URL: $url)")
        return None
      } else if (!isValidYear(year)) {
        log.debug(s"Unable to validate year: $year (URL: $url)")
        return None
      }

      Some(DateTime.parse(day + "/" + month + "/" + year, DateTimeFormat.forPattern("dd/MM/yyyy")))
    } else if (url.toString.matches(longDateUrlPattern)) {
      val dateFragments = longDateUrlPattern.r.findAllIn(url.toString)
      // FIXME: *** SCALA BUG *** this throws a IllegalStateException (fixed in v. 2.11.0-RC1)
      // https://issues.scala-lang.org/browse/SI-8215
      // WORKAROUND:
      dateFragments.hasNext
      val day = dateFragments.group(4)
      val month = dateFragments.group(5)
      val year = dateFragments.group(6)

      // Validate tokens
      if (!isValidDay(day)) {
        log.debug(s"Unable to validate day: $day (URL: $url)")
        return None
      } else if (!isValidMonth(month)) {
        log.debug(s"Unable to validate month: $month (URL: $url)")
        return None
      } else if (!isValidYear(year)) {
        log.debug(s"Unable to validate year: $year (URL: $url)")
        return None
      }

      Some(DateTime.parse(day + "/" + toCompactMonth(month).get + "/" + year, DateTimeFormat.forPattern("dd/MM/yyyy")))
    } else None
  }

  /**
   * Detects first occurrence of a date in a sequence of words
   *
   * @param words sequence of words
   * @return the range of first matching date if exists; none otherwise
   */
  private def getNextDate(words: Seq[Word], startIndex: Int = 0): Option[Range] = {
    val sIndex = words.drop(startIndex).indexWhere(word => isValidDateToken(word.token))
    val eIndex = if ((sIndex + OFFSET) > words.size) words.size else sIndex + OFFSET

    val dates = for (
      i <- eIndex until sIndex by -1;
      dateCandidate = words.slice(startIndex + sIndex, startIndex + i).map(_.token).mkString(sep = " ") if isValidDate(dateCandidate)
    ) yield Range(startIndex + sIndex, startIndex + i)

    if (dates.nonEmpty) Some(dates.head)
    else None
  }

  /**
   * Controls if input day token matches regular expression
   *
   * @param day day to check
   * @return true if day token matches its regular expression; false otherwise
   */
  private def isValidDay(day: String): Boolean = {
    val dayPattern = "(?i)" + DAY_R

    if (day.matches(dayPattern) && day.toInt >= 1 && day.toInt <= 31) true
    else false
  }

  /**
   * Checks if input month token matches regular expression
   *
   * @param month month to check
   * @return true if month token matches its regular expression; false otherwise
   */
  private def isValidMonth(month: String): Boolean = {
    val monthPattern = "(?i)" + MONTH_R
    val compactMonthPattern = "(?i)" + COMPACT_MONTH_R

    if (month.matches(monthPattern)) true
    else if (month.matches(compactMonthPattern) && month.toInt >= 1 && month.toInt <= 12) true
    else false
  }

  /**
   * Checks if input year token matches regular expression
   *
   * @param year year to check
   * @return true if year token matches its regular expression; false otherwise
   */
  private def isValidYear(year: String): Boolean = {
    val yearPattern = "(?i)" + YEAR_R

    if (year.matches(yearPattern)) {
      val pYear = if (year.charAt(0) == '\'') year.replaceAll("\\'(\\s+)?", "") else year

      if (pYear.length == 2) true
      else if (pYear.length == 4 && pYear.toInt >= 1800 && pYear.toInt <= 2200) true
      else false
    } else false
  }

  /**
   * Checks if input date token matches regular expression
   *
   * @param dateToken date token to check
   * @return true if date token matches its regular expression; false otherwise
   */
  private def isValidDateToken(dateToken: String): Boolean = {
    val dateTokenPattern = "(?i)" + DATE_TOKEN_R

    dateToken.matches(dateTokenPattern)
  }

  /**
   * Checks if input date matches regular expression
   *
   * @param date date to check
   * @return true if date matches its regular expression; false otherwise
   */
  private def isValidDate(date: String, semanticCheck: Boolean = true): Boolean = {
    val datePattern = "(?i)" + FULL_DATE_R

    // TODO: Semantic check about day, month and year
    if (semanticCheck) date.matches(datePattern) && toDate(date).isDefined
    else date.matches(datePattern)
  }

  private def toCompactMonth(longMonth: String): Option[String] = {
    longMonth.toLowerCase match {
      case "gennaio" => Some("01")
      case "febbraio" => Some("02")
      case "marzo" => Some("03")
      case "aprile" => Some("04")
      case "maggio" => Some("05")
      case "giugno" => Some("06")
      case "luglio" => Some("07")
      case "agosto" => Some("08")
      case "settembre" => Some("09")
      case "ottobre" => Some("10")
      case "novembre" => Some("11")
      case "dicembre" => Some("12")
      case _ => None
    }
  }

}
