import sbt.Keys._
import sbt._

object BuildBuild {
  // "build.sbt" goes here
  lazy val settings = sbtPlugins ++ Seq(
    scalaVersion := "2.10.6",
    resolvers ++= Seq(
      ivyRepo("bintray-sbt-plugin-releases",
        "http://dl.bintray.com/content/sbt/sbt-plugin-releases"),
      ivyRepo("malliina bintray sbt",
        "https://dl.bintray.com/malliina/sbt-plugins/"),
      Resolver.bintrayRepo("malliina", "maven")
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  def ivyRepo(name: String, urlString: String) =
    Resolver.url(name, url(urlString))(Resolver.ivyStylePatterns)

  def sbtPlugins = Seq(
    "com.malliina" %% "sbt-play" % "0.9.4"
  ) map addSbtPlugin
}
