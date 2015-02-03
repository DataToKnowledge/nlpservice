package it.wheretolive.nlp.pipeline.detector

import it.wheretolive.nlp.Model
import Model._

/**
 * Created by fabiofumarola on 11/01/15.
 */
trait NERDetector {
  def detect(words: Seq[Word]): Seq[Word]
}
