import com.mle.play.PlayProjects
import com.mle.sbt.unix.LinuxPlugin
import com.typesafe.sbt.SbtNativePackager
import sbt.Keys._
import sbt._

object PlayBuild extends Build {

  lazy val p = PlayProjects.playProject("musicmeta").settings(commonSettings: _*)

  val mleGroup = "com.github.malliina"
  val commonSettings = SbtNativePackager.packagerSettings ++ LinuxPlugin.debianSettings ++ Seq(
    version := "1.0.1",
    scalaVersion := "2.11.2",
    retrieveManaged := false,
    fork in Test := true,
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(
      mleGroup %% "play-base" % "0.1.0"
    )
  )
}