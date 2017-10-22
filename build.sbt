import com.malliina.sbt.unix.LinuxKeys.{appHome, httpPort, httpsPort}
import com.malliina.sbtplay.PlayProject
import com.typesafe.sbt.packager.Keys.maintainer
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import scala.sys.process.Process

import scala.util.Try

lazy val p = PlayProject.server("musicmeta").enablePlugins(SystemdPlugin)

val malliinaGroup = "com.malliina"
val utilPlayDep = malliinaGroup %% "util-play" % "4.3.10"

version := "1.7.6"
scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.11", scalaVersion.value)
scalacOptions := Seq("-unchecked", "-deprecation")
libraryDependencies ++= Seq(
  malliinaGroup %% "logstreams-client" % "0.0.9",
  utilPlayDep,
  utilPlayDep % Test classifier "tests"
)
httpPort in Linux := Option("disabled")
httpsPort in Linux := Option("8460")
maintainer := "Michael Skogberg <malliina123@gmail.com>"
javaOptions in Universal ++= {
  val linuxName = (name in Linux).value
  val metaHome = (appHome in Linux).value
  Seq(
    s"-Ddiscogs.oauth=/etc/$linuxName/discogs-oauth.key",
    s"-Dgoogle.oauth=/etc/$linuxName/google-oauth.key",
    s"-Dcover.dir=$metaHome/covers"
  )
}

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, "gitHash" -> gitHash)
buildInfoPackage := "com.malliina.musicmeta"

def gitHash: String =
  Try(Process("git rev-parse --short HEAD").lineStream.head).toOption.getOrElse("unknown")
