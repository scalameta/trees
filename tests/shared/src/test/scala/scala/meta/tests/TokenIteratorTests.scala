package scala.meta.tests

import org.scalatest._

import scala.meta._

class TokenIteratorTests extends FunSuite {
  test("my first test") {
    val program =
      """
        |class Foo{
        | def x() : Int = 2
        | y = 3
        | object Bar{
        |   def y() : Int = 3
        |
        |
        |   y
        | }
        |}
      """.stripMargin
    println(program.parse[Source].get)
  }

}
