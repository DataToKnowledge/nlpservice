package it.dtk.nlp

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */

case class Word(token: String, postTag: Option[String] = None, lemma: Option[String] = None, stem: Option[String] = None)

case class Sentence(words: Seq[Word])

case class Document(sentences: Seq[Sentence])
