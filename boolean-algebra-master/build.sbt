name := "boolean-algebra"
organization := "de.codesourcery.booleanalgebra"
version := "0.6.2-SNAPSHOT"

crossPaths := false
autoScalaLibrary := false

mainClass in (Compile, run) := Some("de.codesourcery.booleanalgebra.Main")
fork in run := true
connectInput in run := true 

parallelExecution in Test := false
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
testOptions in Test := Seq(Tests.Filter(s => s.endsWith("ContextTest")))
unmanagedSourceDirectories in Test := Seq(baseDirectory.value / "src/test")

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.8.1" % "test",
	"commons-lang" % "commons-lang" % "2.4",
	"log4j" % "log4j" % "1.2.14",
	"org.easymock" % "easymockclassextension" % "2.4" % "test",
	"com.novocode" % "junit-interface" % "0.10" % "test"
	)
