import sbt._
import sbt.Keys._

object Build extends Build {
  val tipiVersion = "0.1-M4"

  lazy val scalatest = "org.scalatest" %% "scalatest" % "1.7.1"

  val complianceFiles = SettingKey[Seq[(String, String)]]("compliance-files")
  val complianceIndex = SettingKey[File]("compliance-index")
  val writeComplianceIndex = TaskKey[Unit]("write-compliance-index")

  def complianceFilesSetting =
    (resourceDirectory in Test) apply { resourceDir =>
      (for {
        suiteDir       <- IO.listFiles(resourceDir / "compliance").toList
        inputFile      <- IO.listFiles("*.input")(suiteDir).toList
        inputFilename  <- IO.relativize(resourceDir, inputFile)
        outputFilename <- Some("[.]input".r.replaceAllIn(inputFilename, ".output")) if (resourceDir / outputFilename).exists
      } yield (inputFilename, outputFilename)) : Seq[(String, String)]
    }

  def writeComplianceIndexTask =
    (streams, complianceIndex, complianceFiles) map { (out, index, files) =>
      IO.write(
        index,
        files map { case (a, b) => "/%s => /%s".format(a, b) } mkString "\n"
      )
    }

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      organization := "com.untyped",
      name := "tipi",
      version := tipiVersion,
      scalaVersion := "2.9.2",
      scalacOptions += "-deprecation",
      scalacOptions += "-unchecked",
      libraryDependencies ++= Seq(
        scalatest % "test"
      ),
      publishTo := {
        for {
          host    <- Option(System.getenv("DEFAULT_IVY_REPO_HOST"))
          path    <- Option(System.getenv("DEFAULT_IVY_REPO_PATH"))
          user    <- Option(System.getenv("DEFAULT_IVY_REPO_USER"))
          keyfile <- Option(System.getenv("DEFAULT_IVY_REPO_KEYFILE"))
        } yield Resolver.sftp("Untyped", host, path)(Resolver.ivyStylePatterns).as(user, file(keyfile))
      },
      publishMavenStyle := false,
      complianceIndex <<= (resourceDirectory in Test)(_ / "compliance.index"),
      complianceFiles <<= complianceFilesSetting,
      writeComplianceIndex <<= writeComplianceIndexTask,
      test in Test <<= (test in Test).dependsOn(writeComplianceIndex)
    )
  )
}
