package tipi.core

import org.scalatest._

class TemplateSuite extends FunSuite {

  val parse = Parser("{{", "}}")

  val argsTemplate = Template(
    Env.Basic,
    parse(
      """
      |{{# def page title="Default title" author="Default author" }}
      |<html>
      |  <head>
      |    <title>{{ title }}</title>
      |    <meta name="author" value="{{ author }}">
      |  </head>
      |  <body>{{ this }}</body>
      |</html>
      |{{/ def }}
      """.trim.stripMargin
    ).get.asInstanceOf[Block]
  )

  val argsIn = (
    Env.Basic,
    parse(
      """
      |{{# page title="Title" author="Author" }}
      |  Foo
      |  Bar
      |  Baz
      |{{/ page }}
      """.trim.stripMargin
    ).get
  )

  val bindTemplate = Template(
    Env.Basic,
    parse(
      """
      |{{# def page title author body }}
      |<html>
      |  <head>
      |    <title>{{ title }}</title>
      |    <meta name="author" value="{{ author }}">
      |  </head>
      |  <body>{{ body }}</body>
      |</html>
      |{{/ def }}
      """.trim.stripMargin
    ).get.asInstanceOf[Block]
  )

  val bindIn = (
    Env.Basic,
    parse(
      """
      |{{# page }}
      |  {{# bind title }}Title{{/ bind }}
      |  {{# bind author }}Author{{/ bind }}
      |  {{# bind body }}Body{{/ bind }}
      |{{/ page }}
      """.trim.stripMargin
    ).get
  )

  val wrongNameIn = (
    Env.Basic,
    parse(
      """
      {{# pages title="Title" body="Body" }}
      {{/ pages }}
      """
    ).get
  )

  test("Template.defnEnv") {
    val expectedEnv = Env.Basic ++ Env(
      Id("author") -> Transform.Constant(Text("Default author")),
      Id("title") -> Transform.Constant(Text("Default title"))
    )

    assert(argsTemplate.defnEnv.ids === expectedEnv.ids)
    assert(bindTemplate.defnEnv === Env.Basic)
  }

  test("Template.isDefinedAt - args") {
    assert( argsTemplate.isDefinedAt(argsIn))
    assert( argsTemplate.isDefinedAt(bindIn))
    assert(!argsTemplate.isDefinedAt(wrongNameIn))
  }

  test("Template.isDefinedAt - bind") {
    assert( bindTemplate.isDefinedAt(argsIn))
    assert( bindTemplate.isDefinedAt(bindIn))
    assert(!bindTemplate.isDefinedAt(wrongNameIn))
  }

  test("Template.apply - args") {
    assert(
      Render(argsTemplate.apply(argsIn)).trim ===
      """
      |<html>
      |  <head>
      |    <title>Title</title>
      |    <meta name="author" value="Author">
      |  </head>
      |  <body>
      |  Foo
      |  Bar
      |  Baz
      |</body>
      |</html>
      """.trim.stripMargin
    )
  }

  test("Template.apply - bind") {
    assert(
      Render(bindTemplate.apply(bindIn)).trim ===
      """
      |<html>
      |  <head>
      |    <title>Title</title>
      |    <meta name="author" value="Author">
      |  </head>
      |  <body>Body</body>
      |</html>
      """.trim.stripMargin
    )
  }
}