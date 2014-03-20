name := "NLPService"

version := "1.0"

scalaVersion := "2.10.3"

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
//  "com.typesafe.akka" %% "akka-actor" % "2.2.4",
//  "com.typesafe.akka" %% "akka-testkit" % "2.2.4",
//  "com.typesafe.akka" %% "akka-cluster" % "2.2.4",
  "org.annolab.tt4j" % "org.annolab.tt4j" % "1.1.2",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
)
