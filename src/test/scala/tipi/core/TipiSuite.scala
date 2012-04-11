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
}