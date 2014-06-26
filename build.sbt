name := "NLPService"

version := "1.0"

scalaVersion := "2.11.1"

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
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.3",
  "com.typesafe.akka" %% "akka-remote" % "2.3.3",
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.annolab.tt4j" % "org.annolab.tt4j" % "1.1.2",
  "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2" 
)

//"io.argonaut" %% "argonaut" % "6.0.3"
//atmosSettings
//"io.kamon" % "kamon-core" % "0.3.0"

javaOptions += "-Xms512m -Xmx2G"