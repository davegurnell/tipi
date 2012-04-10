package tipi.core

import org.scalatest._

class ComplianceSuite extends FunSuite {
  val tipi = Tipi()

  val indexPath = "/compliance.index"

  def resourceSource(path: String) =
    io.Source.fromURL(getClass.getResource(path))

  lazy val tipiTests: List[(String, String)] = {
    for {
      line  <- resourceSource(indexPath).getLines.toList
      files <- line.split("=>").toList match {
                 case src :: des :: Nil => Some((src.trim, des.trim))
                 case _ => None
               }
    } yield files
  }

  for {
    (src, des) <- tipiTests
  } {
    test(src + " => " + des) {
      assert(tipi(resourceSource(src).mkString) === des)
    }
  }
}