package tipi.core

import org.scalatest._

class TipiSuite extends FunSuite {
  val tipi = Tipi()

  test("success") {
    assert(
      tipi(
        """
        |{{def x="x1"}}
        |{{x}}
        """.trim.stripMargin
      ) === Right(
        """
        |
        |x1
        """.trim.stripMargin
      )
    )
  }

  test("syntax error") {
    assert(
      tipi(
        """
        |{{def x="x1"}}
        |{{x}
        """.trim.stripMargin
      ) === Left("""[2.4] failure: `}}' expected but `}' found

{{x}
   ^""")
    )
  }

  test("invalid def block") {
    assert(
      tipi(
        """
        |{{def x="x1"}}
        |{{x}
        """.trim.stripMargin
      ) === Left("""[2.4] failure: `}}' expected but `}' found

{{x}
   ^""")
    )
  }

  test("this keyword (adapted from sbt-tipi)") {
    assert(
      tipi(
        """
        |{{# def import }}
        |{{ def template="template" }}
        |File2
        |{{ template }}
        |{{/ def }}
        |
        |{{# def page title }}
        |<title>{{ title }}</title>
        |<content>{{ this }}</content>
        |{{/ def }}
        |
        |{{# page title="Title" }}
        |{{ import }}
        |{{/ page }}
        """.trim.stripMargin
      ) === Right(
        """
        |
        |
        |
        |
        |
        |<title>Title</title>
        |<content>
        |
        |
        |File2
        |template
        |
        |</content>
        |
        """.trim.stripMargin
      )
    )
  }
}