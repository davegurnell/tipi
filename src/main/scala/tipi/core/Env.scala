package tipi.core

case class Env(val bindings: Map[Id, Transform]) {
  def this(bindings: (Id, Transform) *) = {
    this(Map(bindings : _*))
  }

  def get(id: Id): Transform = {
    bindings.getOrElse(id, Transform.Empty)
  }

  def + (binding: (Id, Transform)): Env = {
    Env(this.bindings + binding)
  }

  def ++ (that: Env): Env = {
    Env(this.bindings ++ that.bindings)
  }

  def -- (that: Env): Env = {
    Env(this.bindings -- that.bindings.keys)
  }

  def -- (ids: TraversableOnce[Id]): Env = {
    Env(this.bindings -- ids)
  }

  def - (id: Id): Env = {
    Env(this.bindings - id)
  }

  def prefixWith(prefix: Id): Env = {
    prefixWith(prefix.name)
  }

  def prefixWith(prefix: String): Env = {
    Env(bindings.map { case (Id(name), value) => (Id(prefix + name), value) })
  }

  def filterKeys(test: Id => Boolean): Env = {
    Env(bindings.filterKeys(test))
  }

  override def toString = {
    "Env(%s)".format(bindings.toList.map {
      case (id, value) =>
        id.name + " -> " + value.toString
    }.mkString(", "))
  }
}

object Env {
  def apply(bindings: (Id, Transform) *): Env = {
    Env(Map(bindings : _*))
  }

  val empty = Env()

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
          case Block(_, args, Range.Empty) =>
            (Env.fromArgs(env, args), Range.Empty)

          case block @ Block(_, Arguments(UnitArgument(name) :: _), _) =>
            (env + (name -> Template(block, env)), Range.Empty)

          case _ => sys.error("Invalid 'def' block")
        }
      }

      override def toString = "define"
    }
  ))

  def fromArgs(initial: Env, args: Arguments): Env = {
    args.toList.foldLeft(initial) {
      (accum, arg) =>
        accum + (arg.name -> Transform.argToTransform(arg, accum))
    }
  }
}