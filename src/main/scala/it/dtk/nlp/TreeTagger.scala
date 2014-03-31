package it.dtk.nlp

import org.annolab.tt4j.{TokenHandler, TreeTaggerWrapper}
import it.dtk.nlp.db.{Sentence, Word}

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object TreeTagger {

  private val treeTaggerPath = {
    val os = System.getProperty("os.name").toLowerCase

    if (os.contains("mac")) {
      getClass.getResource("/treetagger/mac").getPath
    } else if (os.contains("linux")) {
      getClass.getResource("/treetagger/linux").getPath
    } else {
      throw new Throwable("Operating system is not supported")
    }
  }
  Runtime.getRuntime.exec("chmod 755 " + treeTaggerPath + "/bin/tree-tagger")

  System.setProperty("treetagger.home", treeTaggerPath)

  private val treeTagger: TreeTaggerWrapper[String] = new TreeTaggerWrapper()
  treeTagger.setModel("italian-par-linux-3.2-utf8.bin:utf-8")

  /**
   * Returns a list of tokens with their relative pos-tag
   *
   * @param tokens a list of tokens
   * @return tokens with pos-tag and lemma
   */
  def tag(tokens: Seq[Word]): Seq[Word] = {
    var tags = Vector.empty[Word]

    treeTagger.setHandler(new TokenHandler[String] {
      override def token(tok: String, pos: String, lemm: String): Unit = {
        tags :+= new Word(token = tok, posTag = Option(pos), lemma = Option(lemm))
      }
    })

    try {
      treeTagger.process(tokens.map(_.token).toArray)
      tags
    } catch {
      case _: Throwable => tokens
    }
  }

  /**
   * Convenience method to tag a string
   *
   * @param token a word
   *@return the pos-tag
   */
  def tag(token: String): Option[String] = {
    tag(Array(new Word(token))).head.posTag
  }

  /**
   * Convenience method to tag every word in a Sentence
   *
   * @param sentence
   * @return
   */
  def apply(sentence: Sentence): Sentence = {
    Sentence(tag(sentence.words))
  }

}