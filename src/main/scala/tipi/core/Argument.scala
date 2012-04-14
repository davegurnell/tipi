package tipi.core

import util.parsing.input.Positional

trait Argument[T] extends Positional {
  val name: Id
  val value: T
  val valueManifest: Manifest[T]
  override def toString = "%s=%s".format(name.name, value)
}

object Argument {
  def unapply[T](arg: Argument[T]): Option[T] = {
    Some(arg.value)
  }
}

case class IdArgument(val name: Id, val value: Id) extends Argument[Id] {
  val valueManifest = manifest[Id]
}

case class StringArgument(val name: Id, val value: String) extends Argument[String] {
  val valueManifest = manifest[String]
}

case class IntArgument(val name: Id, val value: Int) extends Argument[Int] {
  val valueManifest = manifest[Int]
}

case class DoubleArgument(val name: Id, val value: Double) extends Argument[Double] {
  val valueManifest = manifest[Double]
}

case class BooleanArgument(val name: Id, val value: Boolean) extends Argument[Boolean] {
  val valueManifest = manifest[Boolean]
}

case class UnitArgument(val name: Id) extends Argument[Unit] {
  val value = ()
  val valueManifest = manifest[Unit]
  override def toString = name.name
}

case class ArgumentNotFoundException(val id: Id) extends Exception("Argument not found: " + id.name)

case class ArgumentTypeException(val id: Id, val expected: String) extends Exception("Argument is not of type %s: %s".format(expected, id.name))

case class Arguments(arguments: List[Argument[_]]) {
  def contains[T : Manifest](name: Id): Boolean = {
    get[T](name).isDefined
  }

  def apply[T : Manifest](name: Id): T = {
    arguments.find(_.name == name) match {
      case Some(arg) =>
        if(arg.valueManifest <:< manifest[T]) {
          arg.value.asInstanceOf[T]
        } else {
          throw new ArgumentTypeException(name, manifest[T].erasure.getSimpleName)
        }
      case _ =>
        throw new ArgumentNotFoundException(name)
    }
  }

  def get[T : Manifest](name: Id): Option[T] = {
    arguments.find(_.name == name).flatMap {
      case arg: Argument[_] if arg.valueManifest <:< manifest[T] => Some(arg.value.asInstanceOf[T])
      case _ => None
    }
  }

  def toList = arguments
}

object Arguments {
  val Empty = Arguments(Nil)
}
