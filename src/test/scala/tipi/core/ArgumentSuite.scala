package tipi.core

import org.scalatest._

class ArgumentSuite extends FunSuite with Implicits {
  val args = Arguments(List(
    UnitArgument("unit"),
    StringArgument("string", "foo"),
    IntArgument("int", 123)
  ))

  test("contains") {
    assert(args.contains[Any]("unit") === true)
    assert(args.contains[Unit]("unit") === true)
    assert(args.contains[String]("unit") === false)
    assert(args.contains[Int]("unit") === false)
    assert(args.contains[Any]("string") === true)
    assert(args.contains[Unit]("string") === false)
    assert(args.contains[String]("string") === true)
    assert(args.contains[Int]("string") === false)
    assert(args.contains[Any]("int") === true)
    assert(args.contains[Unit]("int") === false)
    assert(args.contains[String]("int") === false)
    assert(args.contains[Int]("int") === true)
    assert(args.contains[Any]("missing") === false)
  }

  test("apply") {
    assert(args[Unit]("unit") === ())
    intercept[ArgumentTypeException]{ args[String]("unit") }
    intercept[ArgumentTypeException]{ args[Int]("unit") }
    intercept[ArgumentTypeException]{ args[Unit]("string") }
    assert(args[String]("string") === "foo")
    intercept[ArgumentTypeException]{ args[Int]("string") }
    intercept[ArgumentTypeException]{ args[Unit]("int") }
    intercept[ArgumentTypeException]{ args[String]("int") }
    assert(args[Int]("int") === 123)
    intercept[ArgumentNotFoundException]{ args[Any]("missing") }
  }

  test("get") {
    assert(args.get[Any]("unit") === Some(()))
    assert(args.get[Unit]("unit") === Some(()))
    assert(args.get[String]("unit") === None)
    assert(args.get[Int]("unit") === None)
    assert(args.get[Any]("string") === Some("foo"))
    assert(args.get[Unit]("string") === None)
    assert(args.get[String]("string") === Some("foo"))
    assert(args.get[Int]("string") === None)
    assert(args.get[Any]("int") === Some(123))
    assert(args.get[Unit]("int") === None)
    assert(args.get[String]("int") === None)
    assert(args.get[Int]("int") === Some(123))
    assert(args.get[Any]("missing") === None)
  }
}