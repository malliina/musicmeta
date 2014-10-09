import com.mle.sbtplay.PlayProjects
import com.mle.sbt.unix.LinuxPlugin
import com.mle.sbtutils.{SbtProjects, SbtUtils}
import com.typesafe.sbt.SbtNativePackager
import sbt.Keys._
import sbt._

object PlayBuild extends Build {
//  PlayProjects.plainPlayProject()
  lazy val p = PlayProjects.plainPlayProject("musicmeta").enablePlugins(play.PlayScala).settings(commonSettings: _*)

  val mleGroup = "com.github.malliina"
  val commonSettings = SbtNativePackager.packagerSettings ++ LinuxPlugin.debianSettings ++ Seq(
    version := "1.0.1",
    scalaVersion := "2.11.2",
    retrieveManaged := false,
    fork in Test := true,
    resolvers ++= Seq(
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"),
    libraryDependencies ++= Seq(
      mleGroup %% "util-play" % "1.6.5",
      mleGroup %% "play-base" % "0.1.0"
    )
  )
}