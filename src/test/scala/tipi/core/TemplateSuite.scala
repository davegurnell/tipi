package tipi.core

import org.scalatest._

class TemplateSuite extends FunSuite {

  val parse = Parser("{{", "}}")

  val argsTemplate = Template(
    parse(
      """
      |{{# def page title author }}
      |<html>
      |  <head>
      |    <title>{{ title }}</title>
      |    <meta name="author" value="{{ author }}">
      |  </head>
      |  <body>{{ this }}</body>
      |</html>
      |{{/ def }}
      """.trim.stripMargin
    ).get.asInstanceOf[Block],
    Env.empty
  )

  val argsIn = (
    Env.empty,
    parse(
      """
      |{{# page "Title" "Author" }}
      |  Foo
      |  Bar
      |  Baz
      |{{/ page }}
      """.trim.stripMargin
    ).get
  )

  val bindTemplate = Template(
    parse(
      """
      |{{# def page }}
      |<html>
      |  <head>
      |    <title>{{ title }}</title>
      |    <meta name="author" value="{{ author }}">
      |  </head>
      |  <body>{{ body }}</body>
      |</html>
      |{{/ def }}
      """.trim.stripMargin
    ).get.asInstanceOf[Block],
    Env.empty
  )

  val bindIn = (
    Env.empty,
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
    Env.empty,
    parse(
      """
      {{# pages "Title" "Body" }}
      {{/ pages }}
      """
    ).get
  )

  test("Template.argNames") {
    assert(argsTemplate.argNames === List(Id("title"), Id("author")))
    assert(bindTemplate.argNames === List())
  }

  test("Template.isDefinedAt - args") {
    assert( argsTemplate.isDefinedAt(argsIn))
    assert(!argsTemplate.isDefinedAt(bindIn))
    assert(!argsTemplate.isDefinedAt(wrongNameIn))
  }

  test("Template.isDefinedAt - bind") {
    assert(!bindTemplate.isDefinedAt(argsIn))
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