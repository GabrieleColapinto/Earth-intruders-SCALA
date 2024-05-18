ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "Cloud version"
  )

libraryDependencies += "com.lihaoyi" %% "requests" % "0.8.0"
libraryDependencies += "com.lihaoyi" %% "ujson" % "3.3.0"