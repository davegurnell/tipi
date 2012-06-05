package tipi.core

import org.scalatest._

class ImplicitsSuite extends FunSuite with TransformImplicits {
  val tipi = Tipi()

  val id = Id("foo")

  val doc = Text("bar")
  val docTrans = Transform.Constant(doc)
  val docEnv = Env(id -> docTrans)

  test("stringToId") {
    assert(("foo" : Id) === id)
  }

  test("stringAndTransformToIdAndTransform") {
    assert(Env("foo" -> docTrans) === docEnv)
  }

  test("docToTransform") {
    assert((Text("bar") : Transform) === docTrans)
  }

  test("idAndDocToIdAndTransform") {
    assert(Env(id -> doc) === docEnv)
  }

  test("stringAndDocToIdAndTransform") {
    assert(Env("foo" -> doc) === docEnv)
  }

  val docFunc: Function1[Doc, Doc] = { case Block(_, _, body) => body }
  val docFuncTrans = Transform.Simple(docFunc)
  val docFuncEnv = Env(id -> docFuncTrans)

  test("docFunctionToTransform") {
    assert((docFunc : Transform) === docFuncTrans)
  }

  test("idAndDocFunctionToIdAndTransform") {
    assert(Env(id -> docFunc) === docFuncEnv)
  }

  test("stringAndDocFunctionToIdAndTransform") {
    assert(Env("foo" -> docFunc) === docFuncEnv)
  }

  val fullFunc: Function1[(Env, Doc), (Env, Doc)] = { case (env, Block(_, _, body)) => (env, body) }
  val fullFuncTrans = Transform.Full(fullFunc)
  val fullFuncEnv = Env(id -> fullFuncTrans)

  test("fullFunctionToTransform") {
    assert((fullFunc : Transform) === fullFuncTrans)
  }

  test("idAndFullFunctionToIdAndTransform") {
    assert(Env(id -> fullFunc) === fullFuncEnv)
  }

  test("stringAndFullFunctionToIdAndTransform") {
    assert(Env("foo" -> fullFunc) === fullFuncEnv)
  }

  test("putting it all together") {
    assert(
      Env(
        "a" -> Text("a"),
        "b" -> Transform.Simple {
          case Block(_, args, body) => body
        },
        "c" -> Transform.Full {
          case (env, Block(_, args, body)) => (env, body)
        }
      ).isInstanceOf[Env]
    )
  }
}