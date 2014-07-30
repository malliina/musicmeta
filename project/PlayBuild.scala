import com.mle.sbt.unix.LinuxPlugin
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.web.Import.Assets
import sbt.Keys._
import sbt._

object PlayBuild extends Build {

  lazy val p = Project("musicmeta", file(".")).enablePlugins(play.PlayScala).settings(commonSettings: _*)

  val mleGroup = "com.github.malliina"
  val commonSettings = SbtNativePackager.packagerSettings ++ LinuxPlugin.debianSettings ++ Seq(
    version := "0.1.2",
    scalaVersion := "2.11.2",
    retrieveManaged := false,
    fork in Test := true,
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.0" % "test",
      "org.scalatestplus" %% "play" % "1.1.0" % "test",
      mleGroup %% "play-base" % "0.1.0",
      mleGroup %% "util-play" % "1.5.0",
      mleGroup %% "logback-rx" % "0.1.0"
    ),
    mappings in(Compile, packageBin) ++= {
      (unmanagedResourceDirectories in Assets).value flatMap
        (assetDir => (assetDir ***) pair relativeTo(baseDirectory.value))
    }
  )
}