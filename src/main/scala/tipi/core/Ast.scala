package tipi.core

import util.parsing.input.Positional

trait Doc extends Positional

case class Block(val name: Id, val args: Arguments = Arguments.Empty, val body: Range = Range.Empty) extends Doc {
  override def toString = "Block(%s,Args(%s),%s)".format(name.name, args.toList.mkString(", "), body)
}

case class Range(val children: List[Doc]) extends Doc {
  override def toString = "Range(%s)".format(children.mkString(", "))
}

object Range {
  val Empty = Range(Nil)
}

case class Text(val value: String) extends Doc

case class Id(val name: String) {
  def prefix(prefix: Id): Id = {
    Id(prefix.name + name)
  }
}
