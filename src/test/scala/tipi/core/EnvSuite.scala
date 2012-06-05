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

    expect(Seq(titleId, authorId))(env.ids)
  }

  test("SingleEnv") {
    val env = SingleEnv(titleId, titleTx)
    expect(Some(titleTx))(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId))(env.ids)
  }

  test("CompoundEnv") {
    val env = CompoundEnv(SingleEnv(titleId, titleTx), SingleEnv(authorId, authorTx))
    expect(Some(titleTx))(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId, authorId))(env.ids)
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

    expect(Seq(titleId.prefix(Id("prefix:")), authorId.prefix(Id("prefix:"))))(env.ids)
  }

  test("OnlyEnv") {
    val env = OnlyEnv(
      List(titleId),
      CompoundEnv(
        SingleEnv(titleId, titleTx),
        SingleEnv(authorId, authorTx)
      )
    )

    expect(Some(titleTx))(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId))(env.ids)
  }

  test("ExceptEnv") {
    val env = ExceptEnv(
      List(titleId),
      CompoundEnv(
        SingleEnv(titleId, titleTx),
        SingleEnv(authorId, authorTx)
      )
    )

    expect(None)(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(authorId))(env.ids)
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

    expect(Seq(titleId))(env.ids)
  }
}