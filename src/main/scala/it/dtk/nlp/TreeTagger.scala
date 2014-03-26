package it.dtk.nlp

import org.annolab.tt4j.{TokenHandler, TreeTaggerWrapper}
import scala.collection.mutable

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object TreeTagger {

  val treeTaggerPath = {
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

  val treeTagger: TreeTaggerWrapper[String] = new TreeTaggerWrapper()
  treeTagger.setModel("italian-par-linux-3.2-utf8.bin:utf-8")
}

class TreeTagger {

  import TreeTagger._

  /**
   * Returns a list of tokens with their relative pos-tag
   *
   * @param token a token
   * @return token with pos-tag and lemma
   */
  def tag(token: String): Word = {
    val tags: mutable.Buffer[Word] = mutable.ArrayBuffer()

    treeTagger.setHandler(new TokenHandler[String] {
      override def token(tok: String, pos: String, lemm: String): Unit = {
        tags += new Word(token = tok, posTag = Option(pos), lemma = Option(lemm))
      }
    })

    try {
      treeTagger.process(Array(token))
      tags.head
    } catch {
      case _: Throwable => new Word(token, None, None)
    }
  }

}