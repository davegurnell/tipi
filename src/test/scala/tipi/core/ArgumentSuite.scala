package tipi.core

import org.scalatest._

class ArgumentSuite extends FunSuite with Implicits {
  val args = Arguments(List(
    UnitArgument("unit"),
    ConstantArgument("const", Text("foo")),
    VariableArgument("var", "x")
  ))

  val testEnv = Env.empty + ("x" -> Text("y"))

  test("defined") {
    assert(args.defined(Env.empty, "unit") === false)
    assert(args.defined(Env.empty, "const") === true)
    assert(args.defined(Env.empty, "var") === false)
    assert(args.defined(testEnv, "var") === true)
    assert(args.defined(Env.empty, "missing") === false)
  }

  test("apply") {
    intercept[ArgumentNotFoundException]{ args(Env.empty, "unit") }
    assert(args(Env.empty, "const") === "foo")
    intercept[ArgumentNotFoundException]{ args(Env.empty, "var") }
    intercept[ArgumentNotFoundException]{ args(Env.empty, "missing") }

    intercept[ArgumentNotFoundException]{ args(testEnv, "unit") }
    assert(args(testEnv, "const") === "foo")
    assert(args(testEnv, "var") === "y")
    intercept[ArgumentNotFoundException]{ args(testEnv, "missing") }
  }

  test("transform") {
    assert(args.transform(Env.empty, "unit") === None)
    assert(args.transform(Env.empty, "const") === Some(Transform.Constant(Text("foo"))))
    assert(args.transform(Env.empty, "var") === None)
    assert(args.transform(Env.empty, "missing") === None)

    assert(args.transform(testEnv, "unit") === None)
    assert(args.transform(testEnv, "const") === Some(Transform.Constant(Text("foo"))))
    assert(args.transform(testEnv, "var") === Some(Transform.Constant(Text("y"))))
    assert(args.transform(testEnv, "missing") === None)
  }

  test("doc") {
    assert(args.doc(Env.empty, "unit") === None)
    assert(args.doc(Env.empty, "const") === Some(Text("foo")))
    assert(args.doc(Env.empty, "var") === None)
    assert(args.doc(Env.empty, "missing") === None)

    assert(args.doc(testEnv, "unit") === None)
    assert(args.doc(testEnv, "const") === Some(Text("foo")))
    assert(args.doc(testEnv, "var") === Some(Text("y")))
    assert(args.doc(testEnv, "missing") === None)
  }

  test("string") {
    assert(args.string(Env.empty, "unit") === None)
    assert(args.string(Env.empty, "const") === Some("foo"))
    assert(args.string(Env.empty, "var") === None)
    assert(args.string(Env.empty, "missing") === None)

    assert(args.string(testEnv, "unit") === None)
    assert(args.string(testEnv, "const") === Some("foo"))
    assert(args.string(testEnv, "var") === Some("y"))
    assert(args.string(testEnv, "missing") === None)
  }
}