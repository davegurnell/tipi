package tipi.core

import org.scalatest._
import scala.util.parsing.combinator._

class ParserSuite extends FunSuite {
  trait ParserTest {
    self: RegexParsers =>
    def runRule[T](rule: Parser[T], input: String): Option[T] = {
      self.parseAll(rule, input).map(Some(_)).getOrElse(None)
    }
  }

  val p1 = new Parser("{{", "}}") with ParserTest
  val p2 = new Parser("[:", ":]") with ParserTest

  test("id") {
    import p1._
    assert(runRule(id, "abc") === Some(Id("abc")))
    assert(runRule(id, "\"abc\"") === None)
    assert(runRule(id, "a.b.c") === None)
  }

  test("string") {
    import p1._
    assert(runRule(string, """ abc """.trim) === None)
    assert(runRule(string, """ "abc" """.trim) === Some("abc"))
    assert(runRule(string, """ "a\\b\"c" """.trim) === Some("a\\b\"c"))
    assert(runRule(string, """ "a\\b\"c" """.trim) === Some("a\\b\"c"))
    assert(runRule(string, """ "a\b\"c" """.trim) === None)
    assert(runRule(string, """ "a\\b"c" """.trim) === None)
    assert(runRule(string, """ a.b.c """.trim) === None)
  }

  test("int") {
    import p1._
    assert(runRule(int, "123") === Some(123))
    assert(runRule(int, "-123") === Some(-123))
    assert(runRule(int, "") === None)
    assert(runRule(int, "-") === None)
    assert(runRule(int, "a") === None)
  }

  test("double") {
    import p1._
    assert(runRule(double, "123") === None)
    assert(runRule(double, "-123") === None)
    assert(runRule(double, "123.456") === Some(123.456))
    assert(runRule(double, "-123.456") === Some(-123.456))
    assert(runRule(double, ".123") === Some(0.123))
    assert(runRule(double, "-.123") === Some(-0.123))
    assert(runRule(double, "1.0") === Some(1.0))
    assert(runRule(double, "") === None)
    assert(runRule(double, "-") === None)
    assert(runRule(double, ".") === None)
    assert(runRule(double, "-.") === None)
    assert(runRule(double, "a") === None)
  }

  test("boolean") {
    import p1._
    assert(runRule(boolean, "true") === Some(true))
    assert(runRule(boolean, "false") === Some(false))
    assert(runRule(boolean, "a") === None)
  }

  test("arg") {
    import p1._
    assert(runRule(arg, "x=a") === Some(IdArgument(Id("x"), Id("a"))))
    assert(runRule(arg, "x = a") === Some(IdArgument(Id("x"), Id("a"))))
    assert(runRule(arg, "x=\"a\"") === Some(StringArgument(Id("x"), "a")))
    assert(runRule(arg, "x = \"a\"") === Some(StringArgument(Id("x"), "a")))
    assert(runRule(arg, "x=\"a\\\"b\"") === Some(StringArgument(Id("x"), "a\"b")))
    assert(runRule(arg, "x = \"a\\\"b\"") === Some(StringArgument(Id("x"), "a\"b")))
    assert(runRule(arg, "x=1") === Some(IntArgument(Id("x"), 1)))
    assert(runRule(arg, "x = 1") === Some(IntArgument(Id("x"), 1)))
    assert(runRule(arg, "x=1.0") === Some(DoubleArgument(Id("x"), 1.0)))
    assert(runRule(arg, "x = 1.0") === Some(DoubleArgument(Id("x"), 1.0)))
    assert(runRule(arg, "x=true") === Some(BooleanArgument(Id("x"), true)))
    assert(runRule(arg, "x = true") === Some(BooleanArgument(Id("x"), true)))
    assert(runRule(arg, "x") === Some(UnitArgument(Id("x"))))
  }

  test("argList") {
    import p1._
    assert(runRule(argList, "i=a j =1 k= 1.0 l m = \"a\" n  =  true") === Some(List(
      IdArgument(Id("i"), Id("a")),
      IntArgument(Id("j"), 1),
      DoubleArgument(Id("k"), 1.0),
      UnitArgument(Id("l")),
      StringArgument(Id("m"), "a"),
      BooleanArgument(Id("n"), true)
    )))
  }

  test("tag - {{ }}") {
    import p1._
    assert(runRule(tag, "{{a}}") === Some(SimpleTag(Id("a"), Nil)))
    assert(runRule(tag, "{{ a b=1 }}") === Some(SimpleTag(Id("a"), List(IntArgument(Id("b"),1)))))
    assert(runRule(tag, "{{ a 1 }}") === None)
    assert(runRule(tag, "{{#a}}") === Some(OpenTag(Id("a"), Nil)))
    assert(runRule(tag, "{{# a b=1 }}") === Some(OpenTag(Id("a"), List(IntArgument(Id("b"), 1)))))
    assert(runRule(tag, "{{# a 1 }}") === None)
    assert(runRule(tag, "{{/a}}") === Some(CloseTag(Id("a"))))
    assert(runRule(tag, "{{/ a b=1 }}") === None)
    assert(runRule(tag, "[:a:]") === None)
  }

  test("tag - [: :]") {
    import p2._
    assert(runRule(tag, "[:a:]") === Some(SimpleTag(Id("a"), Nil)))
    assert(runRule(tag, "[: a b=1 :]") === Some(SimpleTag(Id("a"), List(IntArgument(Id("b"), 1)))))
    assert(runRule(tag, "[: a 1 :]") === None)
    assert(runRule(tag, "[:#a:]") === Some(OpenTag(Id("a"), Nil)))
    assert(runRule(tag, "[:# a b=1 :]") === Some(OpenTag(Id("a"), List(IntArgument(Id("b"), 1)))))
    assert(runRule(tag, "[:# a 1 :]") === None)
    assert(runRule(tag, "[:/a:]") === Some(CloseTag(Id("a"))))
    assert(runRule(tag, "[:/ a 1 :]") === None)
    assert(runRule(tag, "{{a}}") === None)
  }

  test("text - {{ }}") {
    import p1._
    assert(runRule(text, "abc") === Some(Text("abc")))
    assert(runRule(text, "abc{") === Some(Text("abc{")))
    assert(runRule(text, "abc{ {") === Some(Text("abc{ {")))
    assert(runRule(text, "abc{{") === None)
    assert(runRule(text, " x ") === Some(Text(" x ")))
    assert(runRule(text, "abc[") === Some(Text("abc[")))
    assert(runRule(text, "abc[:") === Some(Text("abc[:")))
    assert(runRule(text, "a\nb") === Some(Text("a\nb")))
  }

  test("text - [: :]") {
    import p2._
    assert(runRule(text, "abc") === Some(Text("abc")))
    assert(runRule(text, "abc{") === Some(Text("abc{")))
    assert(runRule(text, "abc{ {") === Some(Text("abc{ {")))
    assert(runRule(text, "abc{{") === Some(Text("abc{{")))
    assert(runRule(text, " x ") === Some(Text(" x ")))
    assert(runRule(text, "abc[") === Some(Text("abc[")))
    assert(runRule(text, "abc[:") === None)
  }

  test("block - {{ }}") {
    import p1._
    assert(runRule(block, "abc") === Some(Text("abc")))
    assert(runRule(block, "{{ abc }}") === Some(Block(Id("abc"), Nil)))
    assert(runRule(block, "{{# abc }}") === None)
    assert(runRule(block, "{{# abc }}{{/ abc }}") === Some(Block(Id("abc"), Nil)))
    assert(runRule(block, "{{# abc }}{{/ def }}") === None)
    assert(runRule(block, "{{# abc }}{{ def }}{{/ abc }}") === Some(
      Block(
        Id("abc"),
        Nil,
        Range(List(Block(Id("def"), Nil)))
      )
    ))
    assert(runRule(block, "{{# abc }}{{ def }}{{ ghi }}{{/ abc }}") === Some(
      Block(
        Id("abc"),
        Nil,
        Range(List(
          Block(Id("def"), Nil),
          Block(Id("ghi"), Nil)
        ))
      )
    ))
    assert(runRule(block, "{{# abc }} x {{/ abc }}") === Some(
      Block(
        Id("abc"),
        Nil,
        Range(List(
          Text(" x ")
        ))
      )
    ))
    assert(runRule(block, " {{# abc }} x {{ def }} y {{/ abc }} ") === None)
    assert(runRule(block, "[:# abc :] x [:/ abc :]") === Some(Text("[:# abc :] x [:/ abc :]")))
    // Newlines:
    assert(runRule(block, "[:# abc :]\nx\n[:/ abc :]") === Some(Text("[:# abc :]\nx\n[:/ abc :]")))
  }

  test("block - [: :]") {
    import p2._
    assert(runRule(block, "{{# abc }} x {{/ abc }}") === Some(Text("{{# abc }} x {{/ abc }}")))
    assert(runRule(block, "[:# abc :] x [:/ abc :]") === Some(
      Block(
        Id("abc"),
        Nil,
        Range(List(
          Text(" x ")
        ))
      )
    ))
  }

  test("doc - {{ }}") {
    import p1._
    assert(runRule(doc, " {{# abc }} x {{ def }} y {{/ abc }} ") === Some(Range(List(
      Text(" "),
      Block(Id("abc"), Nil, Range(List(
        Text(" x "),
        Block(Id("def"), Nil),
        Text(" y "))
      )),
      Text(" ")
    ))))
    assert(runRule(doc, " {{# abc }} x [: def :] y {{/ abc }} ") === Some(Range(List(
      Text(" "),
      Block(Id("abc"), Nil, Range(List(
        Text(" x [: def :] y ")
      ))),
      Text(" ")
    ))))
  }

  test("doc - [: :]") {
    import p2._
    assert(runRule(doc, " {{# abc }} x [: def :] y {{/ abc }} ") === Some(Range(List(
      Text(" {{# abc }} x "),
      Block(Id("def"), Nil),
      Text(" y {{/ abc }} ")
    ))))
  }
}