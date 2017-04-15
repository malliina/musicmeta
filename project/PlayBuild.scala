import com.malliina.sbt.unix.LinuxKeys._
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Linux, Universal}
import com.typesafe.sbt.packager.Keys.maintainer
import sbt.Keys._
import sbt._

object PlayBuild {
  lazy val p = PlayProject.server("musicmeta")
    .settings(commonSettings: _*)

  val malliinaGroup = "com.malliina"
  val utilPlayDep = malliinaGroup %% "util-play" % "3.6.8"
  val commonSettings = linuxSettings ++ Seq(
    version := "1.5.8",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      malliinaGroup %% "logstreams-client" % "0.0.6",
      utilPlayDep,
      utilPlayDep % Test classifier "tests"
    )
  )

  def linuxSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("disabled"),
    httpsPort in Linux := Option("8460"),
    maintainer := "Michael Skogberg <malliina123@gmail.com>",
    javaOptions in Universal ++= {
      val linuxName = (name in Linux).value
      val metaHome = (appHome in Linux).value
      Seq(
        s"-Ddiscogs.oauth=/etc/$linuxName/discogs-oauth.key",
        s"-Dgoogle.oauth=/etc/$linuxName/google-oauth.key",
        s"-Dcover.dir=$metaHome/covers"
      )
    }
  )
}
