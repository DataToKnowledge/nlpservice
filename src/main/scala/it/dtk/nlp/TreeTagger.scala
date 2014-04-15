package it.dtk.nlp

import org.annolab.tt4j.{ TokenHandler, TreeTaggerWrapper }
import it.dtk.nlp.db.Word
import scala.concurrent.{ Promise, ExecutionContext, Future }
import java.util.concurrent.Executors
import scala.util.{ Success, Failure }

/**
 * @author Andrea Scarpino <andrea@datatoknowledge.it>
 */
object TreeTagger {

  private val executorService = Executors.newCachedThreadPool()
  private implicit val executionContext = ExecutionContext.fromExecutorService(executorService)

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

  private val treeTagger: TreeTaggerWrapper[String] = {
    val tagger = new TreeTaggerWrapper[String]()
    tagger.setModel("italian-par-linux-3.2-utf8.bin:utf-8")
    tagger
  }
  

  /**
   * Returns a list of tokens with their relative pos-tag
   *
   * @param tokens a list of tokens
   * @return tokens with pos-tag and lemma
   */
  def tag(tokens: Seq[Word]): Future[Seq[Word]] = {
    val p = Promise[Seq[Word]]()

    var tags = Vector.empty[Word]

    treeTagger.setHandler(new TokenHandler[String] {
      override def token(tok: String, pos: String, lemm: String): Unit = {
        tags :+= new Word(token = tok, posTag = Option(pos), lemma = Option(lemm))
      }
    })

    try {
      treeTagger.process(tokens.map(_.token).toArray)
      p success tags
    } catch {
      case ex: Throwable => p failure ex
    }

    p.future
  }

  /**
   * Convenience method to tag a string
   *
   * @param token a word
   * @return the pos-tag
   */
  def tag(token: String): Future[Option[String]] = {
    val p = Promise[Option[String]]()

    tag(Array(new Word(token))) onComplete {
      case Success(s) =>
        p success s.head.posTag
      case Failure(ex) =>
        p failure ex
    }

    p.future
  }

}