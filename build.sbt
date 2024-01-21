// import to add Scala Native options
import scala.scalanative.build._

scalaVersion := "3.3.1"

// set to Debug for compilation details (Info is default)
logLevel := Level.Info

lazy val root = (project in file("."))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "native-test",
    // defaults set with common options shown
    nativeConfig ~= { c =>
      c.withLTO(LTO.none) // thin
        .withMode(Mode.debug) // releaseFast
        .withGC(GC.immix) // commix
        .withLinkingOptions(Seq("-L/Applications/gpt4all/lib")) // replace to your local gpt4all deployment
    }
  )
