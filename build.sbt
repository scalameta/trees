inThisBuild(
  List(
    scalaVersion := "2.12.7",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    organization := "com.geirsson",
    homepage := Some(url("https://github.com/scalameta/sbt-scalafmt")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "xeno-by",
        "Eugene Burmako",
        "",
        url("http://xeno.by")
      ),
      Developer(
        "olafurpg",
        "Ólafur Páll Geirsson",
        "olafurpg@gmail.com",
        url("https://geirsson.com")
      ),
      Developer(
        "DavidDudson",
        "David Dudson",
        "",
        url("https://daviddudson.github.io/")
      ),
      Developer(
        "mutcianm",
        "Mikhail Mutcianko",
        "",
        url("https://github.com/mutcianm")
      ),
      Developer(
        "maxov",
        "Max Ovsiankin",
        "",
        url("https://github.com/maxov")
      ),
      Developer(
        "gabro",
        "Gabriele Petronella",
        "",
        url("https://buildo.io")
      ),
      Developer(
        "densh",
        "Denys Shabalin",
        "",
        url("http://den.sh")
      )
    )
  ))

lazy val common = project
  .in(file("scalameta/common/shared"))
  .settings(
    enableMacros
  )

lazy val trees = project
  .in(file("scalameta/trees/shared"))
  .settings(
    enableMacros,
    libraryDependencies ++= List(
      "org.scalameta" %% "semanticdb" % "4.0.0",
      "com.lihaoyi" %% "fastparse" % "1.0.0"
    ),
    unmanagedSourceDirectories.in(Compile) ++= {
      val root = baseDirectory.in(ThisBuild).value / "scalameta"
      List(
        root / "io" / "shared" / "src" / "main" / "scala",
        root / "io" / "jvm" / "src" / "main" / "scala",
        root / "tokenizers" / "shared" / "src" / "main" / "scala",
        root / "tokenizers" / "jvm" / "src" / "main" / "scala",
        root / "tokens" / "shared" / "src" / "main" / "scala",
        root / "dialects" / "shared" / "src" / "main" / "scala",
        root / "dialects" / "shared" / "src" / "main" / s"scala-${scalaBinaryVersion.value}",
        root / "inputs" / "shared" / "src" / "main" / "scala"
      )
    }
  )
  .dependsOn(common)

lazy val enableMacros = macroDependencies(hardcore = false)

lazy val enableHardcoreMacros = macroDependencies(hardcore = true)

def macroDependencies(hardcore: Boolean) = libraryDependencies ++= {
  val scalaReflect =
    Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided")
  val scalaCompiler = {
    if (hardcore)
      Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided")
    else Nil
  }
  scalaReflect ++ scalaCompiler
}
