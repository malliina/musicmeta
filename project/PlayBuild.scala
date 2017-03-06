import com.malliina.sbt.unix.LinuxKeys._
import com.malliina.sbt.unix.LinuxPlugin
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.SbtNativePackager.{Debian, Linux, Universal}
import com.typesafe.sbt.packager
import com.typesafe.sbt.packager.Keys.serverLoading
import com.typesafe.sbt.packager.archetypes.{JavaServerAppPackaging, ServerLoader}
import play.sbt.PlayImport
import sbt.Keys._
import sbt._

object PlayBuild {
  lazy val p = PlayProject.default("musicmeta")
    .enablePlugins(JavaServerAppPackaging)
    .settings(commonSettings: _*)

  val malliinaGroup = "com.malliina"
  val commonSettings = linuxSettings ++ Seq(
    version := "1.5.2",
    scalaVersion := "2.11.8",
    resolvers += Resolver.bintrayRepo("malliina", "maven"),
    libraryDependencies ++= Seq(
      malliinaGroup %% "play-base" % "3.3.3",
      PlayImport.specs2 % Test
    )
  )

  def linuxSettings = {
    LinuxPlugin.playSettings ++ Seq(
      httpPort in Linux := Option("8460"),
      httpsPort in Linux := Option("disabled"),
      packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
      javaOptions in Universal ++= {
        val linuxName = (name in Linux).value
        val metaHome = (appHome in Linux).value getOrElse s"/var/run/$linuxName"
        Seq(
          s"-Ddiscogs.oauth=/etc/$linuxName/discogs-oauth.key",
          s"-Dgoogle.oauth=/etc/$linuxName/google-oauth.key",
          s"-Dcover.dir=$metaHome/covers",
          "-Dlogger.resource=logger.xml",
          "-Dfile.encoding=UTF-8",
          "-Dsun.jnu.encoding=UTF-8"
        )
      },
      serverLoading in Debian := ServerLoader.Systemd
    )
  }
}
