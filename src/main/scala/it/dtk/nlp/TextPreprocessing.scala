package it.dtk.nlp

import java.text.BreakIterator
import java.util.Locale
import java.text.BreakIterator

object TextPreprocessing {

  /**
   * @param text
   * @return the text without any text which is included in < >
   */
  def removeHtmlTags(text: String): String =
    text.replaceAll("\\<.*?>", "")
    

  def getSentences(text: String): Seq[String] = {
    val boundary = BreakIterator.getSentenceInstance(Locale.ITALIAN)
    boundary.setText(text)

    var sentences = Vector[String]()

    var start = boundary.first()
    var end = boundary.next()
    while (end != BreakIterator.DONE) {
      sentences = sentences :+ text.substring(start, end).trim()
      start = end
      end = boundary.next()
    }

    sentences
  }

  def getTokens(sentence: String): Seq[Word] = {

    val boundary = BreakIterator.getWordInstance(Locale.ITALIAN)
    boundary.setText(sentence)

    var tokens = Vector[Word]()

    var start = boundary.first()
    var end = boundary.next()
    while (end != BreakIterator.DONE) {
      val token = sentence.substring(start, end)
      if (!token.matches("\\s|\\t"))
    	  tokens = tokens :+ Word(token)
      start = end
      end = boundary.next()
    }
    tokens
  }

  /**
   * @param document
   * @return given a document as text return its representation as sequence of sentences of words
   */
  def apply(document: String): Document = 
    Document(getSentences(document).map(removeHtmlTags(_)).map(s => Sentence(getTokens(s))))
}