package tipi.core

import scala.util.parsing.combinator._
import scala.util.parsing.input._

object Parser {
  trait Tag extends Positional { val name: Id }
  case class SimpleTag(val name: Id, val args: List[Argument]) extends Tag
  case class OpenTag(val name: Id, val args: List[Argument]) extends Tag
  case class CloseTag(val name: Id) extends Tag
}

case class Parser(
  val simpleTagStart: String = "{{",
  val simpleTagEnd:   String = "}}",
  val blockStart:     String = "#",
  val blockEnd:       String = "/"
) extends RegexParsers {
  import tipi.core.Parser._

  override val skipWhitespace = false

  def openBlockStart  = simpleTagStart + blockStart
  def openBlockEnd    = simpleTagEnd

  def closeBlockStart = simpleTagStart + blockEnd
  def closeBlockEnd   = simpleTagEnd

  def apply(input: String): ParseResult[Doc] =
    parseAll(doc, input)

  def apply(input: Reader[Char]): ParseResult[Doc] =
    parseAll(doc, input)

  def apply(input: java.io.File): ParseResult[Doc] =
    parseAll(doc, io.Source.fromFile(input).mkString)

  def doc: Parser[Doc] =
    (rep1(block) ^^ {
      case item :: Nil => item
      case items       => Range(items)
    })

  def block: Parser[Doc] =
    positioned(
      text |
      ((openTag ~ rep(block) ~ closeTag) ^? {
        case OpenTag(openName, args) ~ body ~ CloseTag(closeName) if openName == closeName =>
          Block(openName, Arguments(args), Range(body))
      }) |
      (simpleTag ^^ {
        case SimpleTag(name, args) =>
          Block(name, Arguments(args), Range.Empty)
      })
    )

  def text: Parser[Text] =
    rep1(not(simpleTagStart) ~! success(()) ~> "(?s).".r) ^^ { strs => Text(strs.mkString) }

  def openTag: Parser[OpenTag] =
    (((openBlockStart ~! optWs) ~> id ~ opt(ws ~> argList) <~ (optWs ~ openBlockEnd)) ^^ { case name ~ args => OpenTag(name, args.getOrElse(Nil)) })

  def closeTag: Parser[CloseTag] =
    (((closeBlockStart ~! optWs) ~> id ~ opt(ws ~> argList) <~ (optWs ~ closeBlockEnd)) ^^ { case name ~ args => CloseTag(name) })

  def simpleTag: Parser[SimpleTag] =
    (((simpleTagStart ~! optWs) ~> id ~ opt(ws ~> argList) <~ (optWs ~ simpleTagEnd)) ^^ { case name ~ args => SimpleTag(name, args.getOrElse(Nil)) })

  def argList: Parser[List[Argument]] =
    repsep(arg, ws)

  private def ws    = "[ \t\r\n]+".r
  private def optWs = "[ \t\r\n]*".r

  def arg: Parser[Argument] =
    ( ((id <~ optWs ~ "=" ~ optWs) ~ double ) ^^ { case name ~ value => ConstantArgument(name, value) } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ int    ) ^^ { case name ~ value => ConstantArgument(name, value) } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ boolean) ^^ { case name ~ value => ConstantArgument(name, value) } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ string ) ^^ { case name ~ value => ConstantArgument(name, value) } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ id     ) ^^ { case name ~ value => VariableArgument(name, value) } ) |
    ( ( id                                  ) ^^ { case name         =>     UnitArgument(name)        } )

  def id: Parser[Id] =
    ("[a-zA-Z$_][0-9a-zA-Z$_.:-]*".r ^^ (str => Id(str.toLowerCase)))

  def string: Parser[Text] =
    (""" "([^\\"]|(\\\\)|(\\"))*" """.trim.r ^^ (str => Text(str.substring(1, str.length - 1).replace("\\\\", "\\").replace("\\\"", "\""))))

  def int: Parser[Text] =
    ("-?[0-9]+".r ^^ Text.apply)

  def double: Parser[Text] =
    ("-?[0-9]+[.]([0-9]*)?".r ^^ Text.apply) |
    ("-?[.][0-9]+".r          ^^ Text.apply)

  def boolean: Parser[Text] =
    ("true"  ^^ Text.apply) |
    ("false" ^^ Text.apply)

}