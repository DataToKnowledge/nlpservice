name := "NLPService"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Kamon Repository" at "http://repo.kamon.io"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.5",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.5",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.5",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.5",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-remote" % "2.3.5",
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.annolab.tt4j" % "org.annolab.tt4j" % "1.1.2",
  "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "com.sksamuel.elastic4s" % "elastic4s_2.11" % "1.2.1.3",
  "com.propensive" %% "rapture-io" % "0.9.1",
  "com.propensive" %% "rapture-net" % "0.9.0",
  "com.propensive" %% "rapture-fs" % "0.9.1",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.4.1"
)

//"io.argonaut" %% "argonaut" % "6.0.3"
//atmosSettings
//"io.kamon" % "kamon-core" % "0.3.0"

javaOptions += "-Xms512m -Xmx2G"
