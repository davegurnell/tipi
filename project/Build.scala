import sbt._
import sbt.Keys._

object Build extends Build {
  val scalatest = "org.scalatest" %% "scalatest" % "1.7.1"

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      sbtPlugin := true,
      organization := "com.untyped",
      version := "0.1",
      scalaVersion := "2.9.1",
      scalacOptions += "-deprecation",
      scalacOptions += "-unchecked",
      libraryDependencies ++= Seq(
        scalatest % "test"
      )
    )
  )
}
