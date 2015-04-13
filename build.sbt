
name := "NLPService"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

enablePlugins(JavaAppPackaging)
bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Kamon Repository" at "http://repo.kamon.io"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.8"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.mongodb" %% "casbah" % "2.7.4",
    "org.annolab.tt4j" % "org.annolab.tt4j" % "1.1.2",
    "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
    "com.github.nscala-time" %% "nscala-time" % "1.0.0",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    //  "org.json4s" %% "json4s-native" % "3.2.10",
    "org.json4s" %% "json4s-jackson" % "3.2.10"
  )
}

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.4.8",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.4.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.2"
)

javaOptions += "-Xms512m -Xmx2G"

mainClass in Compile := Some("it.wheretolive.nlp.pipeline.NlpRunner")
