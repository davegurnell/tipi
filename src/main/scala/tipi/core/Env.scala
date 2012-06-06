package tipi.core

trait Env {
  import Env._

  def get(id: Id): Option[Transform]
  def ids: Seq[Id]

  def + (binding: (Id, Transform)): Env = Union(Env(binding), this)
  def ++ (that: Env): Env = Union(that, this)
  def prefix(prefix: Id): Env = Prefix(prefix, this)
  def only(ids: Id *) = Only(ids.toList, this)
  def except(ids: Id *) = Except(ids.toList, this)
}

object Env {
  def apply(bindings: (Id, Transform) *): Env = {
    Env.Simple(Map(bindings : _*))
  }

  def loadClass(name: String): Env = {
    Class.forName(name).newInstance.asInstanceOf[Env]
  }

  case object Empty extends Env {
    def get(id: Id): Option[Transform] = None

    def ids: Seq[Id] = Seq.empty[Id]
  }

  case class Simple(bindings: Map[Id, Transform]) extends Env {
    def get(id: Id): Option[Transform] = {
      bindings.get(id)
    }

    def ids: Seq[Id] = {
      bindings.keys.toList
    }
  }

  case class Union(a: Env, b: Env) extends Env {
    def get(id: Id): Option[Transform] = {
      a.get(id) orElse b.get(id)
    }

    def ids: Seq[Id] = {
      a.ids ++ b.ids
    }
  }

  case class Prefix(prefix: Id, inner: Env) extends Env {
    def get(id: Id): Option[Transform] = {
      if(id.name startsWith prefix.name) {
        inner.get(Id(id.name substring prefix.name.length))
      } else None
    }

    def ids: Seq[Id] = {
      inner.ids.map(_ prefix prefix)
    }
  }

  case class Only(allowed: List[Id], inner: Env) extends Env {
    def get(id: Id): Option[Transform] = {
      if(allowed contains id) inner.get(id) else None
    }

    def ids: Seq[Id] = {
      inner.ids filter (allowed contains _)
    }
  }

  case class Except(allowed: List[Id], inner: Env) extends Env {
    def get(id: Id): Option[Transform] = {
      if(allowed contains id) None else inner.get(id)
    }

    def ids: Seq[Id] = {
      inner.ids filterNot (allowed contains _)
    }
  }

  trait Custom extends Env {
    lazy val bindings = Map {
      this.getClass.getMethods.toList.filter(methodTypesOk _).map { method =>
        Id(method.getName) -> methodToTransform(method)
      } : _*
    }

    private def methodTypesOk(method: java.lang.reflect.Method): Boolean = {
      method.getParameterTypes.toList == Env.Custom.transformArgTypes &&
      method.getReturnType            == Env.Custom.transformReturnType
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

  object Custom {
    val transformArgTypes   = List(classOf[Env], classOf[Doc])
    val transformReturnType = classOf[Tuple2[Env, Doc]]
  }

  object Basic extends Env.Custom {
    def `def`(envIn: Env, docIn: Doc): (Env, Doc) = {
      docIn match {
        case Block(_, args, Range.Empty) =>
          (args.toEnv(envIn), Range.Empty)

        case block @ Block(_, Arguments(UnitArgument(name) :: _), _) =>
          (envIn + (name -> Template(envIn, block)), Range.Empty)

        case _ =>
          sys.error("Invalid 'def' block")
      }
    }

    def `import`(envIn: Env, docIn: Doc): (Env, Doc) = docIn match {
      case Block(_, args, _) =>
        args.string(envIn, Id("class")) match {
          case Some(name) =>
            val prefix = args.string(envIn, Id("prefix")).getOrElse("")
            (envIn ++ Env.loadClass(name).prefix(Id(prefix)), Range.Empty)

          case _ =>
            sys.error("Bad import tag: no 'source' or 'class' parameter")
        }
    }
  }
}