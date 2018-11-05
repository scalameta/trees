package scala.meta.tests

import org.scalatest.FunSuite
import org.scalatest._
import scala.meta._
import scala.meta.dialects.Dotty

class DottyEnumTests extends FunSuite{
  def checkOK(source : String, expected : String) : Unit = {
    val name = source.slice(0, 50).replaceAll("\n", " ")
    test(name){
      val obtained = Dotty(source).parse[Source].get.structure
      assert(obtained == expected)
    }
  }

  def checkBad(source : String) : Unit = {
    val name = source.slice(0, 50).replaceAll("\n", " ")
    test(name){
      val obtainedTree = Dotty(source).parse[Source]
      assert(obtainedTree.isInstanceOf[Parsed.Error])
    }
  }

  def checkOKParse(source : String) : Unit = {
    val name = source.slice(0,50).replaceAll("\n", " ")
    test(name) {
      val obtainedTree = Dotty(source).parse[Source]
      assert(!obtainedTree.isInstanceOf[Parsed.Error])     
    }
  }

  
  checkOK(
    "enum Foo{}",
    """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, """+
    """Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), Nil))))"""
    )


  checkOK(
      """
      |enum Foo{
      | case A, B, C
      |}
    """.stripMargin,
    
    """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil), """+
    """Template(Nil, Nil, Self(Name(""), None), List(Defn.Enum.RepeatedCase(Nil, List(Defn.Enum.Name("A"), """+
    """Defn.Enum.Name("B"), Defn.Enum.Name("C"))))))))""")


  checkOK(
    """
      |enum Foo{
      | case A, B, C
      | case A(x : int)
      | val x : Int = 2
      |}
    """.stripMargin,

    """Source(List(Defn.Enum(Nil, Type.Name("Foo"), """+
    """Nil, Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), """+
    """List(Defn.Enum.RepeatedCase(Nil, List(Defn.Enum.Name("A"), Defn.Enum.Name("B"), """+
    """Defn.Enum.Name("C"))), Defn.Enum.Case(Nil, Term.Name("A"), Nil, Ctor.Primary(Nil, Name(""), """+
    """List(List(Term.Param(Nil, Term.Name("x"), Some(Type.Name("int")), None)))), Nil), Defn.Val(Nil, """+
    """List(Pat.Var(Term.Name("x"))), Some(Type.Name("Int")), Lit.Int(2)))))))""")

    checkOK(
      """
        |enum Foo{
        | case A
        |}
      """.stripMargin,
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil),"""+
      """ Template(Nil, Nil, Self(Name(""), None),"""+
      """ List(Defn.Enum.Case(Nil, Term.Name("A"), Nil, Ctor.Primary(Nil, Name(""), Nil), Nil))))))"""
    )

    checkOK(  
      """
        |enum Color(x : Int){
        | case Red extends Color(0xFF0000)
        | case Green extends Color(0x00FF00)
        | case Blue extends Color(0x0000FF)
        |}
      """.stripMargin,
      """Source(List(Defn.Enum(Nil, Type.Name("Color"), Nil, Ctor.Primary(Nil, Name(""),""" +
      """ List(List(Term.Param(Nil, Term.Name("x"), Some(Type.Name("Int")), None)))), """+
      """Template(Nil, Nil, Self(Name(""), None), List(Defn.Enum.Case(Nil, Term.Name("Red"), Nil, Ctor.Primary(Nil, Name(""), Nil), """+
      """List(Init(Type.Name("Color"), Name(""), List(List(Lit.Int(0xFF0000)))))), Defn.Enum.Case(Nil, Term.Name("Green"), Nil, """+
      """Ctor.Primary(Nil, Name(""), Nil), List(Init(Type.Name("Color"), Name(""), List(List(Lit.Int(0x00FF00)))))), """+
      """Defn.Enum.Case(Nil, Term.Name("Blue"), Nil, Ctor.Primary(Nil, Name(""), Nil), List(Init(Type.Name("Color"), Name(""), """+
      """List(List(Lit.Int(0x0000FF)))))))))))"""
    )

    checkBad("enum {}")

    checkBad("enum ; Foo{}")

    checkBad(
      """
      |enum Foo{
      | case A(x : Int), B
      |}
      """.stripMargin
    )

    checkBad(
      """
      |enum Foo{
      | case A, B(x : Int)
      |}
      """.stripMargin
    )

    checkBad(
      """
      |enum Foo{
      | case A,B(x:Int)
      |}
      """.stripMargin
    )

    checkBad("enum Foo Bar{}")

    checkOKParse(
      """
      |enum Foo{
      | case A(x : Int)
      | class A{
      |   def foo : Int = 2
      | }
      |}
      """.stripMargin
    )

    checkOK(
      """
      |enum Foo{
      | def foo : Int = 2
      | case A
      |}
      """.stripMargin,
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil), """+
      """Template(Nil, Nil, Self(Name(""), None), List(Defn.Def(Nil, Term.Name("foo"), """+
      """Nil, Nil, Some(Type.Name("Int")), Lit.Int(2)), Defn.Enum.Case(Nil, Term.Name("A"), Nil, """+
      """Ctor.Primary(Nil, Name(""), Nil), Nil))))))"""
    )

    checkOKParse(
      """
      |enum Planet(mass: Double, radius: Double) {
      |  private final val G = 6.67300E-11
      |  def surfaceGravity = G * mass / (radius * radius)
      |  def surfaceWeight(otherMass: Double) =  otherMass * surfaceGravity
      |  case MERCURY extends Planet(3.303e+23, 2.4397e6)
      |  case VENUS   extends Planet(4.869e+24, 6.0518e6)
      |  case EARTH   extends Planet(5.976e+24, 6.37814e6)
      |  case MARS    extends Planet(6.421e+23, 3.3972e6)
      |  case JUPITER extends Planet(1.9e+27,   7.1492e7)
      |  case SATURN  extends Planet(5.688e+26, 6.0268e7)
      |  case URANUS  extends Planet(8.686e+25, 2.5559e7)
      |  case NEPTUNE extends Planet(1.024e+26, 2.4746e7)
      |}
      """.stripMargin
    )

    checkOK(
      """
      |enum Foo{
      | case A
      | class A{}
      |}
      """.stripMargin,
      """Source(List(Defn.Enum(Nil, Type.Name("Foo"), Nil, Ctor.Primary(Nil, Name(""), Nil), """+
      """Template(Nil, Nil, Self(Name(""), None), List(Defn.Enum.Case(Nil, Term.Name("A"), Nil, """+
      """Ctor.Primary(Nil, Name(""), Nil), Nil), Defn.Class(Nil, Type.Name("A"), Nil, """+
      """Ctor.Primary(Nil, Name(""), Nil), Template(Nil, Nil, Self(Name(""), None), Nil)))))))"""
    )

    checkOKParse(
      """
      |enum Foo{
      | case A
      | enum Bar{
      |   case B  
      |}
      |}
      """.stripMargin
    )   
}
