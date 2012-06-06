package tipi.core

import org.scalatest._

class ArgumentSuite extends FunSuite with Implicits {
  val args = Arguments(List(
    UnitArgument("unit"),
    ConstantArgument("const", Text("foo")),
    VariableArgument("var", "x")
  ))

  val testEnv = Env.Empty + ("x" -> Text("y"))

  test("defined") {
    assert(args.defined(Env.Empty, "unit") === false)
    assert(args.defined(Env.Empty, "const") === true)
    assert(args.defined(Env.Empty, "var") === false)
    assert(args.defined(testEnv, "var") === true)
    assert(args.defined(Env.Empty, "missing") === false)
  }

  test("apply") {
    intercept[ArgumentNotFoundException]{ args(Env.Empty, "unit") }
    assert(args(Env.Empty, "const") === "foo")
    intercept[ArgumentNotFoundException]{ args(Env.Empty, "var") }
    intercept[ArgumentNotFoundException]{ args(Env.Empty, "missing") }

    intercept[ArgumentNotFoundException]{ args(testEnv, "unit") }
    assert(args(testEnv, "const") === "foo")
    assert(args(testEnv, "var") === "y")
    intercept[ArgumentNotFoundException]{ args(testEnv, "missing") }
  }

  test("transform") {
    assert(args.transform(Env.Empty, "unit") === None)
    assert(args.transform(Env.Empty, "const") === Some(Transform.Constant(Text("foo"))))
    assert(args.transform(Env.Empty, "var") === None)
    assert(args.transform(Env.Empty, "missing") === None)

    assert(args.transform(testEnv, "unit") === None)
    assert(args.transform(testEnv, "const") === Some(Transform.Constant(Text("foo"))))
    assert(args.transform(testEnv, "var") === Some(Transform.Constant(Text("y"))))
    assert(args.transform(testEnv, "missing") === None)
  }

  test("doc") {
    assert(args.doc(Env.Empty, "unit") === None)
    assert(args.doc(Env.Empty, "const") === Some(Text("foo")))
    assert(args.doc(Env.Empty, "var") === None)
    assert(args.doc(Env.Empty, "missing") === None)

    assert(args.doc(testEnv, "unit") === None)
    assert(args.doc(testEnv, "const") === Some(Text("foo")))
    assert(args.doc(testEnv, "var") === Some(Text("y")))
    assert(args.doc(testEnv, "missing") === None)
  }

  test("string") {
    assert(args.string(Env.Empty, "unit") === None)
    assert(args.string(Env.Empty, "const") === Some("foo"))
    assert(args.string(Env.Empty, "var") === None)
    assert(args.string(Env.Empty, "missing") === None)

    assert(args.string(testEnv, "unit") === None)
    assert(args.string(testEnv, "const") === Some("foo"))
    assert(args.string(testEnv, "var") === Some("y"))
    assert(args.string(testEnv, "missing") === None)
  }
}