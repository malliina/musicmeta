import com.malliina.sbt.unix.LinuxKeys.{appHome, httpPort, httpsPort}
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.packager.Keys.maintainer
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import scala.sys.process.Process

import scala.util.Try

lazy val musicmetaRoot = project.in(file("root"))
  .settings(commonSettings: _*)
  .aggregate(backend, frontend)

lazy val backend = PlayProject.server("musicmeta")
  .enablePlugins(SystemdPlugin)
  .settings(backendSettings: _*)

lazy val frontend = project.in(file("frontend"))
  .settings(frontendSettings: _*)
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)

val malliinaGroup = "com.malliina"
val utilPlayVersion = "4.14.0"
val utilPlayDep = malliinaGroup %% "util-play" % utilPlayVersion

lazy val backendSettings = commonSettings ++ Seq(
  scalaJSProjects := Seq(frontend),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  libraryDependencies ++= Seq(
    malliinaGroup %% "logstreams-client" % "1.2.0",
    malliinaGroup %% "play-social" % utilPlayVersion,
    utilPlayDep,
    utilPlayDep % Test classifier "tests"
  ),
  dependencyOverrides ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.8",
    "com.typesafe.akka" %% "akka-actor" % "2.5.8"
  ),
  httpPort in Linux := Option("disabled"),
  httpsPort in Linux := Option("8460"),
  maintainer := "Michael Skogberg <malliina123@gmail.com>",
  javaOptions in Universal ++= {
    val linuxName = (name in Linux).value
    val metaHome = (appHome in Linux).value
    Seq(
      s"-Ddiscogs.oauth=/etc/$linuxName/discogs-oauth.key",
      s"-Dgoogle.oauth=/etc/$linuxName/google-oauth.key",
      s"-Dcover.dir=$metaHome/covers",
      s"-Dconfig.file=/etc/$linuxName/production.conf",
      s"-Dlogger.file=/etc/$linuxName/logback-prod.xml",
      "-Dfile.encoding=UTF-8",
      "-Dsun.jnu.encoding=UTF-8"
    )
  },
  pipelineStages := Seq(digest, gzip),

  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, "gitHash" -> gitHash),
  buildInfoPackage := "com.malliina.musicmeta",

  linuxPackageSymlinks := linuxPackageSymlinks.value.filterNot(_.link == "/usr/bin/starter")
)

lazy val frontendSettings = commonSettings ++ Seq(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalatags" % "0.6.7",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.2",
    "com.typesafe.play" %%% "play-json" % "2.6.10",
    "com.malliina" %%% "primitives" % "1.6.0",
    "org.scalatest" %%% "scalatest" % "3.0.5" % Test
  )
)

lazy val commonSettings = Seq(
  version := "1.12.0",
  scalaVersion := "2.12.6",
  scalacOptions := Seq("-unchecked", "-deprecation")
)

def gitHash: String =
  Try(Process("git rev-parse --short HEAD").lineStream.head).toOption.getOrElse("unknown")
