package it.wheretolive.nlp.detector.textpro

import java.io._
import java.nio.charset.Charset

import scala.io.{Codec, Source}
import scala.sys.process._
import scala.util.{Random, Try}

case class TextProFailure(msg: String) extends Throwable

object TextProExecutor {

  val random = new Random(1234L)
}

class TextProExecutor(basePath: String) {

  private val instanceId = TextProExecutor.random.nextInt()

  private var count = 0L

  private val variableName = "TEXTPRO"

  private val inputFolder = "/tmp/input/"
  new File(inputFolder).mkdir()
  private val outputFolder = "/tmp/output/"
  new File(outputFolder).mkdir()

  //-c token[+tokenid][+tokenstart][+tokenend][+pos][+wnpos][+sentence][+lemma][+comp_morpho][+full_morpho][+entity][+chunk][+keywords]

  private val baseCommand = basePath + "/textpro.pl -l ita -c tokenid+sentence+tokenstart+tokenend+token+pos+wnpos+lemma+chunk+entity+keywords -y -o "

  def tagText(text: String): Try[List[String]] = {

    if (text.isEmpty)
      Try {
        List[String]()
      }
    else {
      //create input file
      val fileName = s"file$instanceId$count"
      count += 1

      val inputFile = createInputFile(fileName, text)
      val outputFile = processFile(inputFile)

      //deleteFile(inputFolder, inputFile)
      val content = getContent(outputFile)
      //deleteFile(outputFolder, outputFile)
      content
    }
  }

  private def getContent(path: Try[String]): Try[List[String]] =
    path.map { p =>
      Source.fromFile(outputFolder + p)(Codec.ISO8859).getLines().toList
    }

  private def deleteFile(folder: String, path: Try[String]): Boolean =
    path.map{ p =>
      val file = new File(folder + p)
      file.delete()
    }.isSuccess

  /**
   * @param inputFile
   * @param content
   * @return the name of the input file created
   */
  private def createInputFile(inputFile: String, content: String): Try[String] = Try {
    val bw = new BufferedWriter(new FileWriter(inputFolder + inputFile))
    bw.write(content)
    bw.close()
    inputFile
  }

  /**
   * @param inputFile
   * @return
   */
  private def processFile(inputFile: Try[String]): Try[String] =
    inputFile.map { input =>
      val command = baseCommand + outputFolder + " " + inputFolder + input
      val result = Process(command, None, (variableName, basePath)).!

      if (result != 0)
        throw new TextProFailure("could not run TextPro")

      input + ".txp"
    }
}