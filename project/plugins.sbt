logLevel := Level.Warn

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-atmos" % "0.3.3")

resolvers += Resolver.url("Kamon Releases", url("http://repo.kamon.io"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.9.2")

