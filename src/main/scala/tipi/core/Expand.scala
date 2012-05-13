package tipi.core

object Expand {
  def apply(in: (Env, Doc)): (Env, Doc) = {
    val (env, doc) = in

    // println(
    //   """
    //   |====================
    //   |Expand
    //   |  %s
    //   |  %s
    //   """.trim.stripMargin.format(env, doc)
    // )

    doc match {
      case doc @ Block(name, _, _) =>
        env.get(name).getOrElse(Transform.Empty).apply((env, doc))

      case Range(children) =>
        var accumEnv = env

        val accumDoc = Range {
          children.map { child =>
            val (childEnv, childDoc) = Expand(accumEnv, child)
            accumEnv = childEnv
            childDoc
          }
        }

        (accumEnv, accumDoc)

      case _ : Text =>
        (env, doc)
    }
  }
}