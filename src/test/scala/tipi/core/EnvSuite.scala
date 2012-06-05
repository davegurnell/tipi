package tipi.core

import org.scalatest._

class EnvSuite extends FunSuite {

  val titleId = Id("title")
  val titleTx = Transform.Constant(Text("Default title"))

  val authorId = Id("author")
  val authorTx = Transform.Constant(Text("Default author"))

  val undefinedId = Id("undefined")

  test("MapEnv") {
    val env = MapEnv(Map(titleId -> titleTx, authorId -> authorTx))
    expect(Some(titleTx))(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))
  }

  test("SingleEnv") {
    val env = SingleEnv(titleId, titleTx)
    expect(Some(titleTx))(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))
  }

  test("CompoundEnv") {
    val env = CompoundEnv(SingleEnv(titleId, titleTx), SingleEnv(authorId, authorTx))
    expect(Some(titleTx))(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))
  }

  test("PrefixEnv") {
    val env = PrefixEnv(
      Id("prefix:"),
      CompoundEnv(
        SingleEnv(titleId, titleTx),
        SingleEnv(authorId, authorTx)
      )
    )

    expect(Some(titleTx))(env.get(titleId.prefix(Id("prefix:"))))
    expect(Some(authorTx))(env.get(authorId.prefix(Id("prefix:"))))
    expect(None)(env.get(undefinedId.prefix(Id("prefix:"))))

    expect(None)(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))
  }

  test("FilterEnv") {
    val env = FilterEnv(
      List(titleId),
      CompoundEnv(
        SingleEnv(titleId, titleTx),
        SingleEnv(authorId, authorTx)
      )
    )

    expect(Some(titleTx))(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))
  }

  test("CustomEnv") {
    val env = new CustomEnv {
      def title(env: Env, doc: Doc): (Env, Doc) = {
        (env, Text("title"))
      }

      def author(binding: (Env, Doc)): (Env, Doc) = {
        (binding._1, Text("author"))
      }
    }

    expect(Some(Text("title")))(env.get(titleId).map(_.apply(Env.basic, Text(""))._2))
    expect(None)(env.get(authorId).map(_.apply(Env.basic, Text(""))))
    expect(None)(env.get(undefinedId))
  }
}