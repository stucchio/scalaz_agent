import sbt._
import Defaults._
import Keys._

object ApplicationBuild extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "scalaz.agent",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.10.0",
    version := "0.01",
    resolvers ++= myResolvers,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    name := "Scalaz Agents",
    //fork := true,
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.0.1",
      "org.scalaz.stream" %% "scalaz-stream" % "0.3.1"
    )
  )

  val myResolvers = Seq("Sonatatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
			"Sonatatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
			"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
			"Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
			"Coda Hale" at "http://repo.codahale.com",
                        "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
		      )

  lazy val scalazAgent = Project("scalaz_agent", file("."), settings = commonSettings)
}
