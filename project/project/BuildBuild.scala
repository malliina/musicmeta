import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  // "build.sbt" goes here
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.6",
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe ivy releases" at "http://repo.typesafe.com/typesafe/ivy-releases/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
      Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins"))(Resolver.ivyStylePatterns)
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    incOptions := incOptions.value.withNameHashing(true)
  ) ++ sbtPlugins
  val malliinaGroup = "com.malliina"
  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))

  def sbtPlugins = Seq(
    "com.typesafe.play" % "sbt-plugin" % "2.5.4",
    malliinaGroup %% "sbt-packager" % "2.1.0",
    malliinaGroup %% "sbt-play" % "0.7.2"
  ) map addSbtPlugin
}
