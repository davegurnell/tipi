package tipi.core

import util.parsing.input.Positional

sealed trait Argument extends Positional {
  val name: Id
  def transform(env: Env): Option[Transform]
  def doc(env: Env): Option[Doc]
  def string(env: Env): Option[String]
}

case class UnitArgument(val name: Id) extends Argument {
  def transform(env: Env) = None
  def doc(env: Env) = None
  def string(env: Env) = None

  override def toString = "%s".format(name.name)
}

case class ConstantArgument(val name: Id, val value: Doc) extends Argument {
  def transform(env: Env) = Some(Transform.Constant(value))
  def doc(env: Env) = Some(value)
  def string(env: Env) = Some(Render(value))

  override def toString = "%s=\"%s\"".format(name.name, value)
}

case class VariableArgument(val name: Id, val value: Id) extends Argument {
  def transform(env: Env) = env.get(value)
  def doc(env: Env) = transform(env).map(tx => tx(env, Range.Empty)._2)
  def string(env: Env) = doc(env).map(Render(_))

  override def toString = "%s=%s".format(name.name, value)
}

case class ArgumentNotFoundException(val id: Id) extends Exception("Argument not found: " + id.name)

case class Arguments(arguments: List[Argument]) {
  def defined(env: Env, name: Id): Boolean = {
    transform(env, name).isDefined
  }

  def apply(env: Env, name: Id): String = {
    string(env, name).getOrElse(throw new ArgumentNotFoundException(name))
  }

  def transform(env: Env, name: Id): Option[Transform] = {
    arguments.find(_.name == name).flatMap(_.transform(env))
  }

  def doc(env: Env, name: Id): Option[Doc] = {
    arguments.find(_.name == name).flatMap(_.doc(env))
  }

  def string(env: Env, name: Id): Option[String] = {
    arguments.find(_.name == name).flatMap(_.string(env))
  }

  def toList = arguments
}

object Arguments {
  val Empty = Arguments(Nil)
}
