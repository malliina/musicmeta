import com.malliina.sbt.unix.LinuxKeys._
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Linux, Universal}
import com.typesafe.sbt.packager
import play.sbt.PlayImport
import sbt.Keys._
import sbt._

object PlayBuild {
  lazy val p = PlayProject.default("musicmeta")
    .settings(commonSettings: _*)

  val malliinaGroup = "com.malliina"
  val commonSettings = linuxSettings ++ Seq(
    version := "1.5.0",
    scalaVersion := "2.11.8",
    resolvers += Resolver.bintrayRepo("malliina", "maven"),
    libraryDependencies ++= Seq(
      malliinaGroup %% "play-base" % "3.3.3",
      PlayImport.specs2 % Test
    )
  )

  def linuxSettings = {
    LinuxPlugin.playSettings ++ Seq(
      httpPort in Linux := Option("disabled"),
      httpsPort in Linux := Option("8460"),
      packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
      javaOptions in Universal ++= {
        val linuxName = (name in Linux).value
        Seq(
          s"-Dgoogle.oauth=/etc/$linuxName/google-oauth.key",
          "-Dlogger.resource=logger.xml",
          s"-Dcover.dir=${(appHome in Linux).value getOrElse s"/opt/$linuxName"}/covers",
          "-Dfile.encoding=UTF-8",
          "-Dsun.jnu.encoding=UTF-8"
        )
      }
    )
  }
}
