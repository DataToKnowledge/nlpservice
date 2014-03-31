package it.dtk.nlp.detector

import it.dtk.nlp.db.Sentence

/**
 * Entry point for detector classes
 *
 * @author Michele Damiano Torelli <daniele@datatoknowledge.it>
 *
 */
trait Detector {

  def detect(sentence: Sentence): Sentence

}
