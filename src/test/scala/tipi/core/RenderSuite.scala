package tipi.core

import org.scalatest._

class RenderSuite extends FunSuite {
  test("text") {
    assert(Render(Text("abc")) === "abc")
  }

  test("range") {
    assert(Render(Range(List(Text("a"), Text("b"), Text("c")))) === "abc")
  }

  test("block") {
    assert(Render(Block(Id("a"), Arguments.Empty, Range(List(Text("b"))))) === "b")
  }
}