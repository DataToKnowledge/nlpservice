name := "NLPService"

version := "1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.1",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.1",
  "org.mongodb" %% "casbah" % "2.7.0-RC2",
  "org.annolab.tt4j" % "org.annolab.tt4j" % "1.1.2",
  "com.github.rholder" % "snowball-stemmer" % "1.3.0.581.1",
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "org.scalatest" % "scalatest_2.10" % "2.1.2" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test",
  //"com.jolbox" % "bonecp" % "0.8.0.RELEASE",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  //"io.argonaut" %% "argonaut" % "6.0.3" ,
  "com.ning" % "async-http-client" % "1.8.4"
)
