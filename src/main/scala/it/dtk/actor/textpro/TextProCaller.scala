package it.dtk.actor.textpro

import java.io.File
import scala.util.Try
import scala.io.Source
import java.nio.file.Files
import java.nio.file.Paths
import java.io.BufferedWriter
import java.io.FileWriter
import scala.sys.process.Process

case class TextProFailure(msg: String) extends Throwable

private object TextProCaller {

  private var count = 0L

  private val variableName = "TEXTPRO"
  private val envPath = "/usr/local/share/TextPro1.5.0/"

  private val inputFolder = System.getProperty("java.io.tmpdir") + "/input/"
  new File(inputFolder).mkdir()
  private val outputFolder = System.getProperty("java.io.tmpdir") + "/output/"
  new File(outputFolder).mkdir()

  private val baseCommand = envPath + "textpro.pl -l ita -c token+tokenid+tokenstart+tokenend+pos+sentence+lemma+comp_morpho+keywords+entity+chunk -y -verbose -o "

  def tagText(text: String): Try[String] = {
    
    //create input file
    val inputfile = s"file$count"
    count += 1

    val nameFileCreated = createInputFile(inputfile, text)
    val nameOutputFile = nameFileCreated.flatMap(fileName => processFile(fileName))
    //delete the input file
    nameFileCreated.map(name => deleteFile(inputFolder + name))
    val content = nameOutputFile.flatMap(name => getContent(outputFolder + name))
    nameOutputFile.map(name => deleteFile(outputFolder + name))

    content
  }

  private def getContent(path: String): Try[String] = Try {
    Source.fromFile(path).getLines().mkString("\n")
  }

  private def deleteFile(path: String): String = {
    Files.delete(Paths.get(path))
    "deleted"
  }

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
  private def processFile(inputFile: String): Try[String] = Try {
    val command = baseCommand + outputFolder + " " + inputFolder + inputFile
    println(command)
    val result = Process(command, None, (variableName, envPath)).!

    if (result != 0)
      throw new TextProFailure("could not run TextPro")

    inputFile + ".txp"
  }
}