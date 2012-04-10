package tipi.core

import com.weiglewilczek.slf4s.Logging

object Expand extends Logging {
  def apply(in: (Env, Doc)): (Env, Doc) = {
    val (env, doc) = in

    logger.debug(
      """
      |Expand
      |  %s
      |  %s
      """.trim.stripMargin.format(env, doc)
    )

    doc match {
      case doc @ Block(name, _, _) =>
        env.get(name).apply((env, doc))

      case Range(children) =>
        var newEnv = env

        val newDoc = Range {
          children.map { child =>
            val (childEnv, childDoc) = Expand(newEnv, child)
            newEnv = childEnv
            childDoc
          }
        }

        (newEnv, newDoc)

      case _ : Text =>
        (env, doc)
    }
  }
}