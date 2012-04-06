package tipi.core

import scala.util.parsing.combinator._
import scala.util.parsing.input._

case class Parser(
  val simpleTagStart: String = "{{",
  val simpleTagEnd:   String = "}}",
  val blockStart:     String = "#",
  val blockEnd:       String = "/"
) extends RegexParsers {
  override val skipWhitespace = false

  def openBlockStart  = simpleTagStart + blockStart
  def openBlockEnd    = simpleTagEnd

  def closeBlockStart = simpleTagStart + blockEnd
  def closeBlockEnd   = simpleTagEnd

  def apply(input: String): ParseResult[Doc] =
    parseAll(doc, input)

  def apply(input: Reader[Char]): ParseResult[Doc] =
    parseAll(doc, input)

  def doc: Parser[Doc] =
    (rep1(block) ^^ {
      case item :: Nil => item
      case items       => Range(items)
    })

  def block: Parser[Doc] =
    text |
    (simpleTag ^^ {
      case SimpleTag(name, args) =>
        Block(name, args, Range.Empty)
    }) |
    ((openTag ~ rep(block) ~ closeTag) ^? {
      case OpenTag(openName, args) ~ body ~ CloseTag(closeName) if openName == closeName =>
        Block(openName, args, Range(body))
    })

  def text: Parser[Text] =
    rep1(not(simpleTagStart) ~! success(()) ~> "(?s).".r) ^^ { strs => Text(strs.mkString) }

  def tag: Parser[Tag] =
    openTag  |
    closeTag |
    simpleTag

  def openTag: Parser[OpenTag] =
    (((openBlockStart ~! optWs) ~> id ~ opt(ws ~> argList) <~ (optWs ~ openBlockEnd)) ^^ { case name ~ args => OpenTag(name, args.getOrElse(Nil)) })

  def closeTag: Parser[CloseTag] =
    (closeBlockStart ~! optWs ~ id ~ optWs ~ closeBlockEnd) ^^ { case _ ~ _ ~ name ~ _ ~ _ => CloseTag(name) }

  def simpleTag: Parser[SimpleTag] =
    (((simpleTagStart ~! optWs) ~> id ~ opt(ws ~> argList) <~ (optWs ~ simpleTagEnd)) ^^ { case name ~ args => SimpleTag(name, args.getOrElse(Nil)) })

  def argList: Parser[List[Argument[_]]] =
    repsep(arg, ws)

  private def ws = "[ \t]+".r
  private def optWs = "[ \t]*".r

  def arg: Parser[Argument[_]] =
    (double  ^^ doubleArgument)  |
    (int     ^^ intArgument)     |
    (boolean ^^ booleanArgument) |
    (string  ^^ stringArgument)  |
    (id      ^^ idArgument)

  private def idArgument(value: Id): Argument[_] = IdArgument(value)
  private def stringArgument(value: String): Argument[_] = StringArgument(value)
  private def intArgument(value: Int): Argument[_] = IntArgument(value)
  private def doubleArgument(value: Double): Argument[_] = DoubleArgument(value)
  private def booleanArgument(value: Boolean): Argument[_] = BooleanArgument(value)

  def id: Parser[Id] =
    "[a-zA-Z0-9_$-]+".r ^^ Id

  def string: Parser[String] =
    (""" "([^\\"]|(\\\\)|(\\"))*" """.trim.r ^^ (str => str.substring(1, str.length - 1).replace("\\\\", "\\").replace("\\\"", "\"")))

  def int: Parser[Int] =
    "-?[0-9]+".r ^^ (str => str.toInt)

  def double: Parser[Double] =
    ("-?[0-9]+[.]([0-9]*)?".r ^^ (str => str.toDouble)) |
    ("-?[.][0-9]+".r          ^^ (str => str.toDouble))

  def boolean: Parser[Boolean] =
    ("true"  ^^ (str => true)) |
    ("false" ^^ (str => false))

}