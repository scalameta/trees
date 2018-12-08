package scala.meta.tests.prettyprinters

import org.scalatest.FunSuite

import scala.meta._
import scala.meta.dialects.Dotty
import scala.meta.internal.trees._

class DottyEnumSyntacticSuite extends FunSuite {

  def testOK(source: String): Unit = {
    val name = source.take(50).replace("\n", " ")
    test(name) {
      Dotty(source).parse[Source] match {
        case Parsed.Success(t) =>
          assert(t.transform { case tree: Tree => tree.withOrigin(Origin.None) }.syntax == source)
        case _ => fail("Cannot parse given source")
      }
    }
  }

  testOK("enum Foo { case A[T](x: Int) extends Foo }")
  testOK("enum Foo { case A[T](x: Int) extends Foo with A with B }")
  testOK("enum Foo { case A }")
  testOK("enum Foo { case A, B, C }")
  testOK("enum Foo { class Bar }")
  
  testOK(
    """enum Foo {
      |  val x: Int = 3
      |  case A, B, C
      |}""".stripMargin
  )

  testOK(
    """class Foo {
      |  enum Bar {
      |    case A, B, C
      |    val x: Int = 3
      |  }
      |}""".stripMargin
  )

  testOK(
    """enum Foo {
      |  case Bar, Bat
      |  case Baz
      |}""".stripMargin
  )
}
