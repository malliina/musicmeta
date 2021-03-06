scalaVersion := "2.12.6"
resolvers ++= Seq(
  ivyRepo("bintray-sbt-plugin-releases",
    "http://dl.bintray.com/content/sbt/sbt-plugin-releases"),
  ivyRepo("malliina bintray sbt",
    "https://dl.bintray.com/malliina/sbt-plugins/"),
  Resolver.bintrayRepo("malliina", "maven")
)
scalacOptions ++= Seq("-unchecked", "-deprecation")

classpathTypes += "maven-plugin"

Seq(
  "com.malliina" %% "sbt-play" % "1.3.1",
  "com.typesafe.sbt" % "sbt-gzip" % "1.0.2",
  "com.typesafe.sbt" % "sbt-digest" % "1.1.4",
  "com.typesafe.sbt" % "sbt-less" % "1.1.2",
  "org.scala-js" % "sbt-scalajs" % "0.6.25",
  "org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0",
  "com.vmunier" % "sbt-web-scalajs" % "1.0.6"
).map(addSbtPlugin)

dependencyOverrides ++= Seq(
  "org.scala-js" % "sbt-scalajs" % "0.6.25",
  "org.webjars" % "webjars-locator-core" % "0.33",
  "org.codehaus.plexus" % "plexus-utils" % "3.0.17",
  "com.google.guava" % "guava" % "23.0"
)

def ivyRepo(name: String, urlString: String) =
  Resolver.url(name, url(urlString))(Resolver.ivyStylePatterns)
