package tipi.core

case class Template(val defn: Block, val globalEnv: Env) extends Transform with TransformImplicits {
  import Template._

  lazy val defnName: Id = defn.args.toList.head.name
  lazy val defnArgNames = defn.args.toList.tail.map(_.name)
  lazy val defnEnv: Env = Env.fromArgs(globalEnv, Arguments(defn.args.toList.tail))

  // println(
  //   """
  //   |====================
  //   |Define %s
  //   |  %s
  //   |  %s
  //   """.trim.stripMargin.format(defnName.name, defnEnv, defn)
  // )

  def localEnv(callingEnv: Env, doc: Block): Env = {
    val thisKwEnv = Env.empty + (Id("this") -> Expand((callingEnv, doc.body))._2)

    val argsEnv = Env.fromArgs(callingEnv, doc.args).only(defnArgNames : _*)

    val bindEnv = {
      def loop(env: Env, doc: Doc): Env = {
        doc match {
          case Block(Id("bind"), args, Range.Empty) =>
            env ++ Env.fromArgs(callingEnv, args).only(defnArgNames : _*)

          case Block(Id("bind"), Arguments(UnitArgument(name) :: _), body) =>
            env + (name -> Expand((callingEnv, body))._2)

          case Range(children) =>
            children.foldLeft(env)(loop)

          case _ => env
        }
      }

      loop(Env.empty, doc.body).only(defnArgNames : _*)
    }

    val bindKwEnv =
      Env.empty + (Id("bind") -> Transform.Identity)

    defnEnv ++ argsEnv ++ bindEnv ++ thisKwEnv ++ bindKwEnv
  }

  def isDefinedAt(in: (Env, Doc)) = {
    val (env, doc) = in
    doc match {
      case Block(name, argValues, _) if name == defnName => true
      case _ => false
    }
  }

  def apply(in: (Env, Doc)) = {
    val (callingEnv, inDoc) = in

    val localEnv = this.localEnv(callingEnv, inDoc.asInstanceOf[Block])

    // println(
    //   """
    //   |====================
    //   |>> Call %s
    //   |  %s
    //   |  %s
    //   """.trim.stripMargin.format(defnName.name, localEnv, inDoc)
    // )

    val (_, outDoc) = Expand(localEnv, defn.body)

    // println(
    //   """
    //   |--------------------
    //   |<< Call %s
    //   |  %s
    //   """.trim.stripMargin.format(defnName.name, outDoc)
    // )

    (callingEnv, outDoc)
  }
}
