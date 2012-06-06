package tipi.core

import org.scalatest._

class TestEnv extends Env.Custom {
  def foo(env: Env, doc: Doc) = {
    (env, Text("foo"))
  }

  def bar(env: Env, doc: Doc) = {
    (env, Text("BAR"))
  }
}

class ExpandSuite extends FunSuite {

  val parser = Parser("{{", "}}")

  def withLines[T](fn : => T): T = {
    println("----------------")
    val ans = fn
    println("----------------")
    ans
  }

  test("static binding") {
    val source =
      """
      |{{def x="x1"}}
      |{{#def a}}{{x}}{{/def}}
      |{{def x="x2"}}
      |{{#def b}}{{x}}{{/def}}
      |{{a}}{{b}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |
      |
      |
      |x1x2
      """.trim.stripMargin
    }
  }

  test("function arguments") {
    val source =
      """
      |{{def x="x1"}}
      |{{#def a x}}{{x}}{{/def}}
      |{{a x="x2"}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |
      |x2
      """.trim.stripMargin
    }
  }

  test("function bindings") {
    val source =
      """
      |{{def x="x1"}}
      |{{#def a x}}{{x}}{{/def}}
      |{{#a}}{{#bind x}}x2{{/bind}}{{/a}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |
      |x2
      """.trim.stripMargin
    }
  }

  test("variable bound to variable") {
    val source =
      """
      |{{def x="x"}}
      |{{def y=x}}
      |{{y}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |
      |x
      """.trim.stripMargin
    }
  }

  test("bind tags expanded at the call site") {
    val source =
      """
      |{{def y="y1"}}
      |{{def z="z1"}}
      |{{#def x y}}c{{y}}d{{/def}}
      |{{def z="z2"}}
      |{{#x}}{{#bind y}}{{z}}{{/bind}}{{/x}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |
      |
      |
      |cz2d
      """.trim.stripMargin
    }
  }

  test("definition not found") {
    val source =
      """
      |{{def x="x"}}
      |{{x}}{{y}}{{x}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |xx
      """.trim.stripMargin
    }
  }

  test("invalid def block") {
    val source =
      """
      |{{def x y z}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      """.trim.stripMargin
    }
  }

  test("import") {
    val source =
      """
      |{{ import class="tipi.core.TestEnv" }}
      |{{ foo }}
      |{{ bar }}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.Basic, parser(source).get))) ===
      """
      |
      |foo
      |BAR
      """.trim.stripMargin
    }
  }

  test("import - class not found") {
    val source =
      """
      |{{ import class="tipi.core.MissingEnv" }}
      """.trim.stripMargin

    intercept[java.lang.reflect.InvocationTargetException] {
      Render(Expand((Env.Basic, parser(source).get)))
    }
  }
}