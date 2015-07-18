import com.mle.sbt.unix.LinuxKeys._
import com.mle.sbt.unix.LinuxPlugin
import com.mle.sbtutils.SbtProjects
import com.typesafe.sbt.SbtNativePackager.{Linux, Universal}
import com.typesafe.sbt.packager
import sbt.Keys._
import sbt._

object PlayBuild extends Build {
  lazy val p = SbtProjects.testableProject("musicmeta").settings(commonSettings: _*).enablePlugins(play.sbt.PlayScala)

  val mleGroup = "com.github.malliina"
  val commonSettings = linuxSettings ++ Seq(
    version := "1.1.0",
    scalaVersion := "2.11.7",
    retrieveManaged := false,
    fork in Test := true,
    resolvers ++= Seq(
      Resolver.bintrayRepo("malliina", "maven"),
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"),
    libraryDependencies ++= Seq(
      mleGroup %% "util-play" % "2.0.1",
      mleGroup %% "play-base" % "0.5.1",
      "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
    )
  )

  def linuxSettings = LinuxPlugin.playSettings ++ Seq(
    httpPort in Linux := Option("disabled"),
    httpsPort in Linux := Option("8457"),
    packager.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
    javaOptions in Universal <<= (appHome in Linux, name in Linux).map((home, n) => {
      Seq(
        "-Dlogger.resource=logger.xml",
        s"-Dcover.dir=${home getOrElse s"/opt/$n"}/covers"
      )
    })
  )
}
