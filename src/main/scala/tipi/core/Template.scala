package tipi.core

import com.weiglewilczek.slf4s.Logging

case class Template(val defn: Block, val globalEnv: Env) extends Transform with TransformImplicits with Logging{
  import Template._

  lazy val defnName: Id = defn.args.head.name
  lazy val defnEnv: Env = Env.fromArgs(globalEnv, defn.args.tail)

  logger.debug(
    """
    |Define %s
    |  %s
    |  %s
    """.trim.stripMargin.format(defnName.name, defnEnv, defn)
  )

  def localEnv(callingEnv: Env, doc: Block): Env = {
    val thisKwEnv = Env.empty + (Id("this") -> doc.body)

    val argsEnv = Env.fromArgs(defnEnv, doc.args)

    val bindEnv = {
      def loop(env: Env, doc: Doc): Env = {
        doc match {
          case Block(Id("bind"), UnitArgument(name) :: _, body) =>
            env + (name -> Expand((callingEnv, body))._2)
          case Range(children) =>
            children.foldLeft(env)(loop)
          case _ => env
        }
      }

      loop(Env.empty, doc.body)
    }

    val bindKwEnv =
      Env.empty + (Id("bind") -> Transform.identity)

    argsEnv ++ bindEnv ++ thisKwEnv ++ bindKwEnv
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

    logger.debug(
      """
      |Call %s
      |  %s
      |  %s
      """.trim.stripMargin.format(defnName.name, localEnv, inDoc)
    )

    val (_, outDoc) = Expand(localEnv, defn.body)
    (callingEnv, outDoc)
  }
}
