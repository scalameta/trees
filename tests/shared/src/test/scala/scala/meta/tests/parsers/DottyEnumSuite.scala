package scala.meta.tests.parsers

import org.scalatest.FunSuite
import org.scalatest._

import scala.meta._
import scala.meta.dialects.Dotty

class DottyEnumSuite extends FunSuite {
  def newParserTest(source: String, customName: Option[String] = None)(body: Parsed[Source] => Unit) {
    val name = customName.getOrElse(source.take(50).replace("\n", " "))
    test(name) {
      val parsed = Dotty(source).parse[Source]
      body(parsed)
    }
  }

  def checkOK(source: String, expected: String): Unit = {
    newParserTest(source)(parsed => {
      assert(parsed.get.structure == expected)
    })
  }

  def checkError(source: String): Unit = {
    newParserTest(source)(parsed => {
      assert(parsed.isInstanceOf[Parsed.Error])
    })
  }

  def checkOK(source: String): Unit = {
    newParserTest(source)(parsed => {
      assert(!parsed.isInstanceOf[Parsed.Error])
    })
  }
  checkError("enum {}")
  checkError("enum ; Foo{}")
  checkError("enum Foo{ case A(x : Int), B}")
  checkError("enum Foo{ case A, B(x : Int) }")
  checkError("enum Foo{ case A,B(x:Int) }")
  checkError("enum Foo Bar{}")
  checkError("enum Foo{ case A, case B }")
  checkError("enum Foo{ case A => 1 }")
  checkError("enum Foo{ case A(x: Int), B(x: Int) }")
  checkError("enum Foo{ case A extends C, B extends D}")
  checkError("final enum Foo {}")
  checkError("lazy enum Foo {}")
  checkError("implicit enum Foo {}")
  checkError("sealed enum Foo {}")
  checkError("override enum Foo {}")
  checkError("abstract enum Foo {}")
  checkError("final override enum Foo {}")
  checkError("enum Foo { private case A }")
  checkError("enum Foo { override case A}")
  checkError("enum Foo { implicit case A }")
  checkError("enum Foo { lazy case A }")
  checkOK("enum Foo { case A extends Foo with A }")
  checkOK("private enum Foo {}")
  checkOK("protected enum Foo {}")
  checkOK("enum Foo{ case A(x: Int, y: Int) extends B }")
  checkOK("enum Foo { case `Hello world !`}")

  checkError(
    """
      |enum Foo{
      | case A(x: Int){
      |   def x: Int = 1
      | }
      |}
    """.stripMargin
  )

  checkError(
    """
      |enum Foo{
      | case A, B(x: Int){
      |   val x = 2
      | }
      |}
    """.stripMargin
  )

  checkOK(
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
      | case A
      | enum Bar{
      |   case B  
      | }
      |}
      """.stripMargin
  )

  checkOK(
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
    "enum Foo{}",
    """Source(List(
        |Defn.Enum(
          |Nil,
          | Type.Name("Foo"),
          | Nil,
          | Ctor.Primary(
            |Nil,
            | Name(""),
            | Nil),
            | Template(
              |Nil,
              | Nil,
              | Self(Name(""), None), Nil))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | case A, B, C
      |}
    """.stripMargin,
    """Source(
      |List(
        |Defn.Enum(
          |Nil,
          | Type.Name("Foo"),
          | Nil,
          | Ctor.Primary(Nil, Name(""), Nil),
          | Template(
            |Nil,
            | Nil,
            | Self(Name(""),
            | None),
          | List(
            |Defn.Enum.RepeatedCase(
              |Nil,
              | List(
                |Defn.Enum.Name("A"),
                | Defn.Enum.Name("B"),
                | Defn.Enum.Name("C"))))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | case A, B, C
      | case A(x : int)
      | val x : Int = 2
      |}
    """.stripMargin,
    """Source(
        |List(
          |Defn.Enum(
            |Nil,
            | Type.Name("Foo"),
            | Nil,
            | Ctor.Primary(
              |Nil,
              | Name(""),
              | Nil),
          | Template(
            |Nil,
            | Nil,
            | Self(Name(""), None),
            | List(
              |Defn.Enum.RepeatedCase(
                |Nil,
                | List(
                  |Defn.Enum.Name("A"),
                  | Defn.Enum.Name("B"),
                  | Defn.Enum.Name("C"))),
                  | Defn.Enum.Case(
                    |Nil,
                    | Term.Name("A"),
                    | Nil,
                    | Ctor.Primary(
                      |Nil,
                      | Name(""),
                      | List(
                        |List(
                        |Term.Param(
                          |Nil,
                          | Term.Name("x"),
                          | Some(Type.Name("int")), None)))),
                          | Nil),
                  | Defn.Val(
                    |Nil,
                    | List(Pat.Var(Term.Name("x"))),
                    | Some(Type.Name("Int")),
                    | Lit.Int(2)))))))""".stripMargin.replace("\n", "")
  )

  checkOK("enum Foo{ case A }",
    """Source(
          |List(
            |Defn.Enum(
              |Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(
                |Nil,
                | Name(""),
                | Nil),
              | Template(
              |Nil,
              | Nil,
              | Self(Name(""), None),
              | List(
              |Defn.Enum.Case(Nil, Term.Name("A"), Nil, Ctor.Primary(Nil, Name(""), Nil), Nil))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Color(x : Int){
      | case Red extends Color(0xFF0000)
      | case Green extends Color(0x00FF00)
      | case Blue extends Color(0x0000FF)
      |}
      """.stripMargin,
    """Source(
          |List(
            |Defn.Enum(
              |Nil,
              | Type.Name("Color"),
              | Nil,
              | Ctor.Primary(Nil, Name(""),
                | List(
                  |List(
                    |Term.Param(Nil, Term.Name("x"), Some(Type.Name("Int")), None)))),
              | Template(
                |Nil,
                | Nil,
                | Self(Name(""), None),
                | List(
                  |Defn.Enum.Case(
                    |Nil,
                    | Term.Name("Red"),
                    | Nil,
                    | Ctor.Primary(Nil, Name(""), Nil),
                    | List(
                      |Init(
                        |Type.Name("Color"),
                        | Name(""),
                        | List(List(Lit.Int(0xFF0000)))))),
                | Defn.Enum.Case(
                  |Nil,
                  | Term.Name("Green"),
                  | Nil,
                  | Ctor.Primary(
                    |Nil,
                    | Name(""),
                    | Nil),
                  | List(
                    |Init(
                      |Type.Name("Color"),
                      | Name(""),
                      | List(List(Lit.Int(0x00FF00)))))),
                | Defn.Enum.Case(
                  |Nil,
                  | Term.Name("Blue"),
                  | Nil,
                  | Ctor.Primary(Nil, Name(""), Nil),
                  | List(
                    |Init(
                    |Type.Name("Color"),
                    | Name(""),
                    | List(List(Lit.Int(0x0000FF)))))))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    "enum Foo extends A with B with C {}",
    """
      |Source(
        |List(
          |Defn.Enum(
            |Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(Nil, Name(""), Nil),
              | Template(
                |Nil,
                | List(
                  |Init(Type.Name("A"), Name(""), Nil),
                  | Init(Type.Name("B"), Name(""), Nil),
                  | Init(Type.Name("C"), Name(""), Nil)),
                | Self(Name(""), None), Nil))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | case A
      | x match{
      |   case 1 => 2
      |   case _ =>
      | }
      |}
      """.stripMargin,
    """
        |Source(
          |List(
            |Defn.Enum(Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(Nil, Name(""), Nil),
              | Template(
                |Nil,
                | Nil,
                | Self(Name(""), None),
                | List(
                  |Defn.Enum.Case(
                    |Nil,
                    | Term.Name("A"),
                    | Nil,
                    | Ctor.Primary(Nil, Name(""), Nil), Nil),
                    | Term.Match(
                      |Term.Name("x"),
                      | List(
                        |Case(Lit.Int(1), None, Lit.Int(2)),
                        | Case(Pat.Wildcard(), None, Term.Block(Nil)))))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | def foo : Int = 2
      | case A
      |}
      """.stripMargin,
    """Source(
          |List(
            |Defn.Enum(
              |Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(Nil, Name(""), Nil),
              | Template(
                |Nil,
                | Nil,
                | Self(Name(""), None),
                | List(
                  |Defn.Def(
                    |Nil,
                    | Term.Name("foo"),
                    | Nil,
                    | Nil,
                    | Some(Type.Name("Int")),
                    | Lit.Int(2)),
                  | Defn.Enum.Case(
                    |Nil,
                    | Term.Name("A"),
                    | Nil,
                    | Ctor.Primary(Nil, Name(""), Nil), Nil))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | case A
      | class A{}
      |}
      """.stripMargin,
    """Source(
          |List(
            |Defn.Enum(
              |Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(Nil, Name(""), Nil),
              | Template(
                |Nil,
                | Nil,
                | Self(Name(""), None),
                | List(
                |Defn.Enum.Case(
                  |Nil,
                  | Term.Name("A"),
                  | Nil,
                  | Ctor.Primary(Nil, Name(""), Nil),
                  | Nil),
                | Defn.Class(
                  |Nil,
                  | Type.Name("A"),
                  | Nil,
                  | Ctor.Primary(Nil, Name(""), Nil),
                  | Template(
                    |Nil,
                    | Nil,
                    | Self(Name(""), None),
                    | Nil)))))))""".stripMargin.replace("\n", "")
  )

  checkOK(
    """
      |enum Foo{
      | case A,
      |      B,
      |      C
      | case D
      |}
      """.stripMargin,
    """Source(
          |List(
            |Defn.Enum(
              |Nil,
              | Type.Name("Foo"),
              | Nil,
              | Ctor.Primary(Nil, Name(""),
              | Nil),
              | Template(
                |Nil,
                | Nil,
                | Self(Name(""), None),
                | List(
                  |Defn.Enum.RepeatedCase(
                    |Nil,
                    | List(
                      |Defn.Enum.Name("A"),
                      | Defn.Enum.Name("B"),
                      | Defn.Enum.Name("C"))),
                  | Defn.Enum.Case(
                    |Nil,
                    | Term.Name("D"),
                    | Nil,
                    | Ctor.Primary(Nil, Name(""), Nil), Nil))))))""".stripMargin.replace("\n", "")
  )
}
