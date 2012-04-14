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

  def argList: Parser[List[Argument[_]]] =
    repsep(arg, ws)

  private def ws    = "[ \t\r\n]+".r
  private def optWs = "[ \t\r\n]*".r

  def arg: Parser[Argument[_]] =
    ( ((id <~ optWs ~ "=" ~ optWs) ~ double ) ^^ { case name ~ value =>  DoubleArgument(name, value) : Argument[_] } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ int    ) ^^ { case name ~ value =>     IntArgument(name, value) : Argument[_] } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ boolean) ^^ { case name ~ value => BooleanArgument(name, value) : Argument[_] } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ string ) ^^ { case name ~ value =>  StringArgument(name, value) : Argument[_] } ) |
    ( ((id <~ optWs ~ "=" ~ optWs) ~ id     ) ^^ { case name ~ value =>      IdArgument(name, value) : Argument[_] } ) |
    ( ( id                                  ) ^^ { case name         =>    UnitArgument(name)        : Argument[_] } )

  def id: Parser[Id] =
    ("[a-zA-Z$_][0-9a-zA-Z$_.:-]*".r ^^ (str => Id(str.toLowerCase)))

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