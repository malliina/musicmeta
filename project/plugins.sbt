scalaVersion := "2.12.4"
resolvers ++= Seq(
  ivyRepo("bintray-sbt-plugin-releases",
    "http://dl.bintray.com/content/sbt/sbt-plugin-releases"),
  ivyRepo("malliina bintray sbt",
    "https://dl.bintray.com/malliina/sbt-plugins/"),
  Resolver.bintrayRepo("malliina", "maven")
)
scalacOptions ++= Seq("-unchecked", "-deprecation")

addSbtPlugin("com.malliina" %% "sbt-play" % "1.2.0")

def ivyRepo(name: String, urlString: String) =
  Resolver.url(name, url(urlString))(Resolver.ivyStylePatterns)
