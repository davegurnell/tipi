package tipi.core

trait Transform extends PartialFunction[(Env, Doc), (Env, Doc)]

object Transform {
  case class Constant(doc: Doc) extends Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = (in._1, doc)
    override def toString = "constant(%s)".format(doc)
  }

  case object Identity extends Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = in
    override def toString = "identity"
  }

  case object Empty extends Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = (in._1, Range.Empty)
    override def toString = "empty"
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

trait TransformImplicits {
  implicit def docToTransform(doc: Doc): Transform = Transform.Constant(doc)
}