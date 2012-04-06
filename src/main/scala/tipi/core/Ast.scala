package tipi.core

import util.parsing.input.Positional

trait Doc extends Positional
case class Block(val name: Id, val args: List[Argument[_]], val body: Range = Range.Empty) extends Doc
case class Range(val children: List[Doc]) extends Doc
case class Text(val value: String) extends Doc

object Range {
  val Empty = Range(Nil)
}

trait Argument[T] { val value: T }
case class IdArgument(val value: Id) extends Argument[Id]
case class StringArgument(val value: String) extends Argument[String]
case class IntArgument(val value: Int) extends Argument[Int]
case class DoubleArgument(val value: Double) extends Argument[Double]
case class BooleanArgument(val value: Boolean) extends Argument[Boolean]

object Argument {
  def unapply[T](arg: Argument[T]): Option[T] = {
    Some(arg.value)
  }
}

trait Tag { val name: Id }
case class SimpleTag(val name: Id, val args: List[Argument[_]]) extends Tag
case class OpenTag(val name: Id, val args: List[Argument[_]]) extends Tag
case class CloseTag(val name: Id) extends Tag

case class Id(val name: String)
