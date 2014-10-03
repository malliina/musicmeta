import sbt._
import sbt.Keys._

/**
 *
 * @author mle
 */
object BuildBuild extends Build {
  // "build.sbt" goes here
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe ivy releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    incOptions := incOptions.value.withNameHashing(true)
  ) ++ sbtPlugins

  def sbtPlugins = Seq(
    "com.github.malliina" %% "sbt-packager" % "1.2.2",
    "com.github.malliina" %% "sbt-play" % "0.0.1"
  ) map addSbtPlugin

  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}

