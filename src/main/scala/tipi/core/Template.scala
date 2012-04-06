package tipi.core

object Template {
  object IdArguments {
    def unapply(args: List[Argument[_]]): Option[List[Id]] = {
      args match {
        case Nil  => None
        case args if args.forall(_.isInstanceOf[IdArgument]) =>
          Some(args.collect{ case IdArgument(id) => id })
      }
    }
  }
}

case class Template(val defn: Block, val globalEnv: Env) extends Transform with TransformImplicits {
  import Template._

  def defnArgs: List[Id] = {
    defn.args match {
      case IdArguments(ids) => ids
      case other            => sys.error("Bad template header: " + other)
    }
  }

  lazy val tagName: Id = defnArgs.head
  lazy val argNames: List[Id] = defnArgs.tail

  def localEnv(callingEnv: Env, doc: Block): Env = {
    val thisKeywordEnv =
      Env.empty + (Id("this") -> doc.body)

    val argsEnv =
      argNames.zip(doc.args).foldLeft(Env.empty) {
        case (accum, (name, value)) =>
          accum + (name -> Transform.argToTransform(value, globalEnv))
      }

    val bindEnv = {
      def loop(env: Env, doc: Doc): Env = {
        doc match {
          case Block(Id("bind"), IdArgument(name) :: _, body) =>
            env + (name -> Expand((callingEnv, body))._2)
          case Range(children) =>
            children.foldLeft(env)(loop)
          case _ => env
        }
      }

      loop(Env.empty, doc.body)
    }

    val bindKeywordEnv =
      Env.empty + (Id("bind") -> Transform.identity)

    globalEnv ++ thisKeywordEnv ++ argsEnv ++ bindEnv ++ bindKeywordEnv
  }

  def isDefinedAt(in: (Env, Doc)) = {
    val (env, doc) = in
    doc match {
      case Block(name, argValues, _) if name == tagName && argValues.length == argNames.length => true
      case _ => false
    }
  }

  def apply(in: (Env, Doc)) = {
    val (callingEnv, inDoc) = in

    val localEnv = this.localEnv(callingEnv, inDoc.asInstanceOf[Block])

    // println(
    //   """
    //   |Call %s
    //   |  %s
    //   |  %s
    //   """.trim.stripMargin.format(tagName, localEnv, inDoc)
    // )

    val (_, outDoc) = Expand(localEnv, defn.body)
    (callingEnv, outDoc)
  }
}
