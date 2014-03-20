import AssemblyKeys._

assemblySettings

// Skipping tests
test in assembly := {}

jarName in assembly := "NLPService.jar"

//mainClass in assembly := Some("it.dtk.Main")