import sbt.Keys._
import sbt._

object BuildBuild {
  // "build.sbt" goes here
  lazy val settings = Seq(
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

  def sbtPlugins = Seq(
    malliinaGroup %% "sbt-packager" % "2.2.0",
    malliinaGroup %% "sbt-play" % "0.9.1"
  ) map addSbtPlugin
}
