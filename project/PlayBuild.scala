import com.malliina.sbt.unix.LinuxKeys._
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Linux, Universal}
import com.typesafe.sbt.packager
import play.sbt.PlayImport
import sbt.Keys._
import sbt._

object PlayBuild extends Build {
  lazy val p = PlayProject("musicmeta").settings(commonSettings: _*).enablePlugins(play.sbt.PlayScala)

  val malliinaGroup = "com.malliina"
  val commonSettings = linuxSettings ++ Seq(
    version := "1.3.0",
    scalaVersion := "2.11.7",
    retrieveManaged := false,
    fork in Test := true,
    resolvers += Resolver.bintrayRepo("malliina", "maven"),
    libraryDependencies ++= Seq(
      malliinaGroup %% "play-base" % "2.5.0",
      PlayImport.specs2 % Test
    )
  )

  def linuxSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("disabled"),
    httpsPort in Linux := Option("8457"),
    packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
    javaOptions in Universal ++= Seq(
      "-Dlogger.resource=logger.xml",
      s"-Dcover.dir=${(appHome in Linux).value getOrElse s"/opt/${(name in Linux).value}"}/covers"
    )
  )
}
