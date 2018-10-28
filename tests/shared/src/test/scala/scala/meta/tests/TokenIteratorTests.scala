package scala.meta.tests

import org.scalatest._

import scala.meta._

class TokenIteratorTests extends FunSuite {
  test("my first test") {
    val program =
      """
        |class Foo{
        | def x() : Int = 2
        |}
      """.stripMargin
    println(program.parse[Source].get)
  }

}
