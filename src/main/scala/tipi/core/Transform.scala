package tipi.core

trait Transform extends Function1[(Env, Doc), (Env, Doc)]

object Transform {
  case class Full(val func: Function1[(Env, Doc), (Env, Doc)]) extends Transform {
    def apply(in: (Env, Doc)) = func(in)
    override def toString = "Full(" + func + ")"
  }

  case class Simple(val func: Function[Doc, Doc]) extends Transform {
    def apply(in: (Env, Doc)) = {
      val (env, doc) = in
      (env, func(doc))
    }
    override def toString = "Simple(" + func + ")"
  }

  case class Constant(doc: Doc) extends Transform {
    def apply(in: (Env, Doc)) = (in._1, doc)
    override def toString = "Constant(" + doc + ")"
  }

  case object Identity extends Transform {
    def apply(in: (Env, Doc)) = (in._1, in._2)
    override def toString = "Identity"
  }

  case object Empty extends Transform {
    def apply(in: (Env, Doc)) = (in._1, Range.Empty)
    override def toString = "Empty"
  }
}
