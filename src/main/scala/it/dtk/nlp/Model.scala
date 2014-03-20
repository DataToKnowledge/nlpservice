package it.dtk.nlp

/**
 * @author Andrea Scarpino <me@andreascarpino.it>
 */

case class Word(token: String, postTag: String, lemma: String, stem: String)

case class Sentence(words: Seq[Word])

case class Document(sentences: Seq[Sentence])
