package it.dtk.nlp.detector

import it.dtk.nlp.db.Word

/**
 * Entry point for detector classes
 *
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 *
 */
trait Detector {

  def detect(sentence: Seq[Word]): Seq[Word]

}
