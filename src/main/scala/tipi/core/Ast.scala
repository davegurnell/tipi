package tipi.core

import util.parsing.input.Positional

trait Doc extends Positional
case class Block(val name: Id, val args: List[Argument[_]], val body: Range = Range.Empty) extends Doc {
  override def toString = "Block(%s,Args(%s),%s)".format(name.name, args.mkString(", "), body)
}
case class Range(val children: List[Doc]) extends Doc {
  override def toString = "Range(%s)".format(children.mkString(", "))
}
case class Text(val value: String) extends Doc

object Range {
  val Empty = Range(Nil)
}

trait Argument[T] extends Positional {
  val name: Id
  val value: T
  override def toString = "%s=%s".format(name.name, value)
}
case class IdArgument(val name: Id, val value: Id) extends Argument[Id]
case class StringArgument(val name: Id, val value: String) extends Argument[String]
case class IntArgument(val name: Id, val value: Int) extends Argument[Int]
case class DoubleArgument(val name: Id, val value: Double) extends Argument[Double]
case class BooleanArgument(val name: Id, val value: Boolean) extends Argument[Boolean]
case class UnitArgument(val name: Id) extends Argument[Unit] {
  val value = ()
  override def toString = name.name
}

object Argument {
  def unapply[T](arg: Argument[T]): Option[T] = {
    Some(arg.value)
  }
}

trait Tag extends Positional { val name: Id }
case class SimpleTag(val name: Id, val args: List[Argument[_]]) extends Tag
case class OpenTag(val name: Id, val args: List[Argument[_]]) extends Tag
case class CloseTag(val name: Id) extends Tag

case class Id(val name: String)
