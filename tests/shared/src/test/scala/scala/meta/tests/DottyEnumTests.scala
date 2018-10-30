package scala.meta.tests

import org.scalatest.FunSuite
import org.scalatest._
import scala.meta._
import scala.meta.dialects.Dotty

class DottyEnumTests extends FunSuite{
  def parsingCheck(program : String, treeStruct : String) = {
    val obtained = Dotty(program).parse[Source].get.structure
    assert(obtained === treeStruct)
  }

  test("simple empty enum"){
    val program = "enum Foo{}"
    val struct =
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), Nil))))"""
    parsingCheck(program, struct)
  }

  test("enum with one repeated case"){
    val program =
      """
        |enum Foo{
        | case A, B, C
        |}
      """.stripMargin

    val struct =
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), List(Defn.Enum.RepeatedCase(Nil, List(Defn.Enum.Name("A"), Defn.Enum.Name("B"), Defn.Enum.Name("C"))))))))"""

    parsingCheck(program, struct)
  }

  test("test ; problem"){
    val program =
      """
        |enum Foo{
        | case A, B, C
        | case A(x : int)
        | val x : Int = 2
        |}
      """.stripMargin

    val struct =
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), """+
          """Nil, Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), """+
          """List(Defn.Enum.RepeatedCase(Nil, List(Defn.Enum.Name("A"), Defn.Enum.Name("B"), """+
          """Defn.Enum.Name("C"))), Defn.Enum.Case(Nil, Term.Name("A"), Nil, Ctor.Primary(Nil, Name(""), """+
          """List(List(Term.Param(Nil, Term.Name("x"), Some(Type.Name("int")), None)))), Nil), Defn.Val(Nil, """+
          """List(Pat.Var(Term.Name("x"))), Some(Type.Name("Int")), Lit.Int(2)))))))"""

    parsingCheck(program, struct)
  }
}
