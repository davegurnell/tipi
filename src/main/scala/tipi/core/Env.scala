package tipi.core

trait Env {
  def get(id: Id): Option[Transform]
  def ids: Seq[Id]

  def + (binding: (Id, Transform)): Env = CompoundEnv(SingleEnv(binding), this)
  def ++ (that: Env): Env = CompoundEnv(that, this)
  def prefix(prefix: Id): Env = PrefixEnv(prefix, this)
  def filter(names: List[Id]) = FilterEnv(names, this)
}

case class CompoundEnv(a: Env, b: Env) extends Env {
  def get(id: Id): Option[Transform] = {
    a.get(id) orElse b.get(id)
  }

  def ids: Seq[Id] = {
    a.ids ++ b.ids
  }
}

case class SingleEnv(binding: (Id, Transform)) extends Env {
  val id = binding._1
  val transform = binding._2

  def get(id: Id): Option[Transform] = {
    if(id == this.id) Some(transform) else None
  }

  def ids: Seq[Id] = {
    List(id)
  }
}

case class PrefixEnv(prefix: Id, inner: Env) extends Env {
  def get(id: Id): Option[Transform] = {
    if(id.name startsWith prefix.name) {
      inner.get(Id(id.name substring prefix.name.length))
    } else None
  }

  def ids: Seq[Id] = {
    inner.ids.map(_ prefix prefix)
  }
}

case class FilterEnv(allowed: List[Id], inner: Env) extends Env {
  def get(id: Id): Option[Transform] = {
    if(allowed contains id) {
      inner.get(id)
    } else None
  }

  def ids: Seq[Id] = {
    inner.ids filter (allowed contains _)
  }
}

case class MapEnv(val bindings: Map[Id, Transform]) extends Env {
  def this(bindings: (Id, Transform) *) = {
    this(Map(bindings : _*))
  }

  def get(id: Id): Option[Transform] = {
    bindings.get(id)
  }

  def ids: Seq[Id] = {
    bindings.keys.toList
  }
}

trait CustomEnv extends Env {
  lazy val bindings = Map {
    this.getClass.getMethods.toList.filter(methodTypesOk _).map { method =>
      Id(method.getName) -> methodToTransform(method)
    } : _*
  }

  private def methodTypesOk(method: java.lang.reflect.Method): Boolean = {
    method.getParameterTypes.toList == CustomEnv.transformArgTypes &&
    method.getReturnType            == CustomEnv.transformReturnType
  }

  private def methodToTransform(method: java.lang.reflect.Method): Transform = {
    Transform.Full((in: (Env, Doc)) => method.invoke(this, in._1, in._2).asInstanceOf[(Env, Doc)])
  }

  def get(id: Id): Option[Transform] = {
    bindings.get(id)
  }

  def ids: Seq[Id] = {
    bindings.keySet.toList
  }
}

object CustomEnv {
  val transformArgTypes   = List(classOf[Env], classOf[Doc])
  val transformReturnType = classOf[Tuple2[Env, Doc]]
}

object Env {
  def apply(bindings: (Id, Transform) *): Env = {
    MapEnv(Map(bindings : _*))
  }

  val empty = Env()

  val basic = empty ++ Env(
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
  )

  def fromArgs(initial: Env, args: Arguments): Env = {
    args.toList.foldLeft(initial) {
      (accum, arg) =>
        arg.transform(accum) match {
          case Some(tx) => accum + (arg.name -> tx)
          case None     => accum
        }
    }
  }
}