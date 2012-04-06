package tipi.core

trait Transform extends PartialFunction[(Env, Doc), (Env, Doc)]

object Transform {
  def constant(doc: Doc) = new Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = (in._1, doc)
    override def toString = "constant(%s)".format(doc)
  }

  val identity = new Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = in
    override def toString = "identity"
  }

  val empty = new Transform {
    def isDefinedAt(in: (Env, Doc)) = true
    def apply(in: (Env, Doc)) = (in._1, Range.Empty)
    override def toString = "empty"
  }

  def argToTransform(arg: Argument[_], env: Env): Transform = {
    arg match {
      case IdArgument(id)         => env.bindings.get(id).getOrElse(Transform.constant(Text(id.name)))
      case StringArgument(value)  => Transform.constant(Text(value))
      case IntArgument(value)     => Transform.constant(Text(value.toString))
      case DoubleArgument(value)  => Transform.constant(Text(value.toString))
      case BooleanArgument(value) => Transform.constant(Text(value.toString))
    }
  }
}

trait TransformImplicits {
  implicit def docToTransform(doc: Doc): Transform = {
    Transform.constant(doc)
  }
}