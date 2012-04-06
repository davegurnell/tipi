package tipi.core

import org.scalatest._

class ExpandSuite extends FunSuite {

  val parser = Parser("{{", "}}")

  test("static binding") {
    val source =
      """
      |{{def x "x1"}}
      |{{#def a}}{{x}}{{/def}}
      |{{def x "x2"}}
      |{{#def b}}{{x}}{{/def}}
      |{{a}}{{b}}
      """.trim.stripMargin

    assert(
      Render(Expand((Env.basic, parser(source).get))) ===
      """
      |
      |
      |
      |
      |x1x2
      """.trim.stripMargin
    )
  }

  test("function arguments") {
    val source =
      """
      |{{def x "x1"}}
      |{{#def a x}}{{x}}{{/def}}
      |{{a "x2"}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.basic, parser(source).get))) ===
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
      |{{def x "x1"}}
      |{{#def a}}{{x}}{{/def}}
      |{{#a}}{{#bind x}}x2{{/bind}}{{/a}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.basic, parser(source).get))) ===
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
      |{{def x "x"}}
      |{{def y x}}
      |{{y}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.basic, parser(source).get))) ===
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
      |{{def y "y1"}}
      |{{def z "z1"}}
      |{{#def x}}c{{y}}d{{/def}}
      |{{def z "z2"}}
      |{{#x}}{{#bind y}}{{z}}{{/bind}}{{/x}}
      """.trim.stripMargin

    assert {
      println("====================================")
      Render(Expand((Env.basic, parser(source).get))) ===
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
      |{{def x "x"}}
      |{{x}}{{y}}{{x}}
      """.trim.stripMargin

    assert {
      Render(Expand((Env.basic, parser(source).get))) ===
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
      Render(Expand((Env.basic, parser(source).get))) ===
      """
      |
      """.trim.stripMargin
    }
  }
}