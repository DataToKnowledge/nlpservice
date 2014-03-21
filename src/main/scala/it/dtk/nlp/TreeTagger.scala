package it.dtk.nlp

import org.annolab.tt4j.{TreeTaggerException, TokenHandler, TreeTaggerWrapper}
import scala.collection.mutable
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.IOException
import scala.util.{Failure, Success}

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

  def tag(token: String): Future[Word] = {
    val p = Promise[Word]()

    val tags: mutable.Buffer[Word] = mutable.ArrayBuffer()

    treeTagger.setHandler(new TokenHandler[String] {
      override def token(token: String, pos: String, lemma: String): Unit = {
        tags += new Word(token, Some(pos), Some(lemma))
      }
    })

    try {
      treeTagger.process(Array(token))
      p success tags.head
    } catch {
      case ex: IOException => p failure ex
      case ex: TreeTaggerException => p failure ex
    }

    p.future
  }

  def tag(tokens: Array[String]): Future[mutable.Buffer[Word]] = {
    val p = Promise[mutable.Buffer[Word]]()

    val tags: mutable.Buffer[Word] = mutable.ArrayBuffer()

    tokens.foreach(x =>
      tag(x) onComplete {
        case Success(t) => tags += t
        case Failure(ex) => p failure ex
      }
    )
    p success tags

    p.future
  }

}