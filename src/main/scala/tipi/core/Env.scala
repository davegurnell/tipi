package tipi.core

case class Env(val bindings: Map[Id, Transform]) {
  def get(id: Id): Transform = {
    bindings.getOrElse(id, Transform.empty)
  }

  def + (binding: (Id, Transform)): Env = {
    Env(this.bindings + binding)
  }

  def ++ (that: Env): Env = {
    Env(this.bindings ++ that.bindings)
  }

  override def toString = {
    "Env(%s)".format(bindings.toList.map {
      case (id, value) =>
        id.name + " -> " + value.toString
    }.mkString(", "))
  }
}

object Env {
  val empty = Env(Map())

  val basic = empty ++ Env(Map(
    Id("def") -> new Transform {
      def isDefinedAt(in: (Env, Doc)) = {
        val (env, doc) = in
        doc match {
          case Block(Id("def"), _, _) => true
          case _                      => false
        }
      }

      def apply(in: (Env, Doc)): (Env, Doc) = {
        val (env, doc) = in
        doc match {
          case Block(_, IdArgument(name) :: value :: Nil, Range.Empty) =>
            (env + (name -> Transform.argToTransform(value, env)), Range.Empty)

          // case block @ Block(_, IdArgument(name) :: args, Range.Empty) =>
          //   sys.error("Empty 'def' block")

          case block @ Block(_, IdArgument(name) :: args, _) =>
            (env + (name -> Template(block, env)), Range.Empty)

          case _ => sys.error("Invalid 'def' block")
        }
      }

      override def toString = "define"
    }
  ))
}