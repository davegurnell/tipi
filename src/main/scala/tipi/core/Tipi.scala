package tipi.core

import scala.util.parsing.combinator._
import scala.util.parsing.input._

case class Tipi(
  val simpleTagStart: String = "{{",
  val simpleTagEnd:   String = "}}",
  val blockStart:     String = "#",
  val blockEnd:       String = "/",
  val globalEnv:      Env    = Env.basic
) {
  val parse = Parser(
    simpleTagStart = simpleTagStart,
    simpleTagEnd   = simpleTagEnd,
    blockStart     = blockStart,
    blockEnd       = blockEnd
  )
  val expand = Expand
  val render = Render

  def apply(input: io.Source): Either[String,String] = {
    apply(input.mkString)
  }

  def apply(inputString: String): Either[String,String] = {
    parse(new CharSequenceReader(inputString)) match {
      case parse.Success(doc, _) =>
        Right(render(expand((globalEnv, doc))))

      case err: parse.NoSuccess =>
        Left(err.toString)
    }
  }
}
