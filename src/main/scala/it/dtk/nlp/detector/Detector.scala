package it.dtk.nlp.detector

import it.dtk.nlp.Word

/**
 * Entry point for detector classes
 *
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 *
 */
trait Detector {

  def detect(words: Seq[Word]): Seq[Word]

}
