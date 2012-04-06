package tipi.core

import org.scalatest._

class TipiSuite extends FunSuite {
  val tipi = Tipi()

  test("success") {
    assert(
      tipi(
        """
        |{{def x "x1"}}
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
        |{{def x "x1"}}
        |{{x}
        """.trim.stripMargin
      ) === Left("[1,1]: `}}' expected but `}' found")
    )
  }
  
  test("invalid def block") {
    assert(
      tipi(
        """
        |{{def x "x1"}}
        |{{x}
        """.trim.stripMargin
      ) === Left("[1,1]: `}}' expected but `}' found")
    )
  }
}