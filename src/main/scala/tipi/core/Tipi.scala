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

  def apply(input: String): Either[String,String] = {
    apply(new CharSequenceReader(input))
  }

  def apply(input: Reader[Char]): Either[String,String] = {
    parse(input) match {
      case parse.Success(doc, _) =>
        Right(render(expand((globalEnv, doc))))

      case err: parse.NoSuccess =>
        Left("[%s,%s]: %s".format(input.pos.line, input.pos.column, err.msg))
    }
  }
}
