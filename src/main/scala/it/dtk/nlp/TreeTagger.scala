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

  System.setProperty("treetagger.home", treeTaggerPath)

  val treeTagger: TreeTaggerWrapper[String] = new TreeTaggerWrapper()
  treeTagger.setModel("italian-par-linux-3.2-utf8.bin:utf-8")
}

class TreeTagger {

  import TreeTagger._

  def tag(tokens: Array[String]) = {
    val tags: mutable.Buffer[Word] = mutable.ArrayBuffer()

    treeTagger.setHandler(new TokenHandler[String] {
      override def token(token: String, pos: String, lemma: String): Unit = {
        tags += new Word(token, Some(pos), Some(lemma))
      }
    })

    treeTagger.process(tokens)

    tags
  }

}