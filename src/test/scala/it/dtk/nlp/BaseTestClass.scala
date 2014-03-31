package it.dtk.nlp

import org.scalatest._
import org.scalatest.concurrent.Futures
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent._
import org.scalatest.time.Span
import org.scalatest.time._

/**
 * @author fabiofumarola
 * we must read the documentation at http://www.scalatest.org/user_guide/defining_base_classes and the consecutive pages
 */
class BaseTestClass extends WordSpec with Matchers with OptionValues with Futures with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(5, Millis))
}