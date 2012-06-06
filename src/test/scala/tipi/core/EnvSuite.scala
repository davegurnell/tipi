package tipi.core

import org.scalatest._

class EnvSuite extends FunSuite {

  val titleId = Id("title")
  val titleTx = Transform.Constant(Text("Default title"))

  val authorId = Id("author")
  val authorTx = Transform.Constant(Text("Default author"))

  val undefinedId = Id("undefined")

  test("Env.Simple") {
    val env = Env.Simple(Map(titleId -> titleTx, authorId -> authorTx))
    expect(Some(titleTx))(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId, authorId))(env.ids)
  }

  test("Env.Union") {
    val env = Env.Union(
      Env(titleId -> titleTx),
      Env(authorId -> authorTx)
    )

    expect(Some(titleTx))(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId, authorId))(env.ids)
  }

  test("Env.Prefix") {
    val env = Env.Prefix(
      Id("prefix:"),
      Env(titleId -> titleTx, authorId -> authorTx)
    )

    expect(Some(titleTx))(env.get(titleId.prefix(Id("prefix:"))))
    expect(Some(authorTx))(env.get(authorId.prefix(Id("prefix:"))))
    expect(None)(env.get(undefinedId.prefix(Id("prefix:"))))

    expect(None)(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId.prefix(Id("prefix:")), authorId.prefix(Id("prefix:"))))(env.ids)
  }

  test("Env.Only") {
    val env = Env.Only(
      List(titleId),
      Env(titleId -> titleTx, authorId -> authorTx)
    )

    expect(Some(titleTx))(env.get(titleId))
    expect(None)(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId))(env.ids)
  }

  test("Env.Except") {
    val env = Env.Except(
      List(titleId),
      Env(titleId -> titleTx, authorId -> authorTx)
    )

    expect(None)(env.get(titleId))
    expect(Some(authorTx))(env.get(authorId))
    expect(None)(env.get(undefinedId))

    expect(Seq(authorId))(env.ids)
  }

  test("Env.Custom") {
    val env = new Env.Custom {
      def title(env: Env, doc: Doc): (Env, Doc) = {
        (env, Text("title"))
      }

      def author(binding: (Env, Doc)): (Env, Doc) = {
        (binding._1, Text("author"))
      }
    }

    expect(Some(Text("title")))(env.get(titleId).map(_.apply(Env.Basic, Text(""))._2))
    expect(None)(env.get(authorId).map(_.apply(Env.Basic, Text(""))))
    expect(None)(env.get(undefinedId))

    expect(Seq(titleId))(env.ids)
  }
}