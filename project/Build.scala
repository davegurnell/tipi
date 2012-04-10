import sbt._
import sbt.Keys._

object Build extends Build {
  lazy val scalatest = "org.scalatest"           %% "scalatest" % "1.7.1"
  lazy val slf4s     = "com.weiglewilczek.slf4s" %% "slf4s"     % "1.0.7"

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      organization := "com.untyped",
      version := "0.1-SNAPSHOT",
      name := "tipi",
      scalaVersion := "2.9.1",
      scalacOptions += "-deprecation",
      scalacOptions += "-unchecked",
      libraryDependencies ++= Seq(
        scalatest % "test",
        slf4s
      ),
      publishTo := {
        for {
          host    <- Option(System.getenv("DEFAULT_IVY_REPO_HOST"))
          path    <- Option(System.getenv("DEFAULT_IVY_REPO_PATH"))
          user    <- Option(System.getenv("DEFAULT_IVY_REPO_USER"))
          keyfile <- Option(System.getenv("DEFAULT_IVY_REPO_KEYFILE"))
        } yield Resolver.sftp("Untyped", host, path)(Resolver.ivyStylePatterns).as(user, file(keyfile))
      },
      publishMavenStyle := false
    )
  )
}
