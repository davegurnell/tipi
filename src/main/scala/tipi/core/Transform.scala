package tipi.core

trait Transform extends Function1[(Env, Doc), (Env, Doc)]

object Transform {
  case class Full(val func: Function1[(Env, Doc), (Env, Doc)]) extends Transform {
    def apply(in: (Env, Doc)) = func(in)
  }

  case class Simple(val func: Function[Doc, Doc]) extends Transform {
    def apply(in: (Env, Doc)) = {
      val (env, doc) = in
      (env, func(doc))
    }
  }

  case class Constant(doc: Doc) extends Transform {
    def apply(in: (Env, Doc)) = (in._1, doc)
  }

  case object Identity extends Transform {
    def apply(in: (Env, Doc)) = (in._1, in._2)
  }

  case object Empty extends Transform {
    def apply(in: (Env, Doc)) = (in._1, Range.Empty)
  }

  def argToTransform(arg: Argument[_], env: Env): Transform = {
    arg match {
      case IdArgument(name, value)      => env.bindings.get(value).getOrElse(Constant(Range.Empty))
      case StringArgument(name, value)  => Constant(Text(value))
      case IntArgument(name, value)     => Constant(Text(value.toString))
      case DoubleArgument(name, value)  => Constant(Text(value.toString))
      case BooleanArgument(name, value) => Constant(Text(value.toString))
      case UnitArgument(name)           => Constant(Range.Empty)
    }
  }
}
