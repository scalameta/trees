package org.scalameta.tests

import org.scalatest.FunSuite
import org.scalatest._
import scala.meta._
class DottyEnumTests extends FunSuite{
  def parsingCheck(program : String, treeStruct : String) = {
    val obtained = program.parse[Source].get.structure
    assert(obtained === treeStruct)
  }

  test("simple empty enum"){
    val program = "enum Foo{}"
    parsingCheck(program, "")
  }
}
