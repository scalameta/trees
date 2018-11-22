inThisBuild(
  List(
    scalaVersion := "2.12.7",
    version ~= { dynVer =>
      if (sys.env.contains("CI")) dynVer
      else "SNAPSHOT" // only for local publishng
    },
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    organization := "org.scalameta",
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
    ),
    // speed up sbt reload, don't package javadoc
    publishArtifact in packageDoc := sys.env.contains("CI")
  )
)

skip.in(publish) := true

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

lazy val common = project
  .in(file("scalameta/common/shared"))
  .settings(
    macroDependencies(hardcore = false),
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.1.4",
    // Temporary name to avoid conflicts with scalameta/scalameta modules.
    // Down the road, this build may get promoted to become the official
    // "common" and "trees" modules.
    moduleName := "common-experimental"
  )

lazy val trees = project
  .in(file("scalameta/trees/shared"))
  .settings(
    macroDependencies(hardcore = false),
    moduleName := "trees-experimental",
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

// trigger publishLocal for trees module so that `sbt test` works out of the box.
onLoad.in(Global) := {
  val fn: State => State = { s =>
    "publishTrees" :: s
  }
  fn compose onLoad.in(Global).value
}

lazy val publishTrees = taskKey[Unit]("publishTrees")
publishTrees := {
  publishLocal.in(trees).value
  publishLocal.in(common).value
}

lazy val parsers = project
  .in(file("scalameta/parsers/shared"))
  .settings(
    // Temporary name to avoid conflicts with scalameta/scalameta modules.
    // Down the road, this build may get promoted to become the official
    // "common" and "trees" modules.
    moduleName := "parsers-experimental",
    macroDependencies(hardcore = true),
    libraryDependencies ++= List(
      "org.scalameta" %% "trees-experimental" % version.in(ThisBuild).value
    ),
    unmanagedSourceDirectories.in(Compile) ++= {
      val root = baseDirectory.in(ThisBuild).value / "scalameta"
      List(
        root / "parsers" / "jvm" / "src" / "main" / "scala",
        root / "transversers" / "shared" / "src" / "main" / "scala",
        root / "quasiquotes" / "shared" / "src" / "main" / "scala"
      )
    }
  )

lazy val scalameta = project
  .in(file("scalameta/scalameta/shared"))
  .settings(
    moduleName := "scalameta-experimental"
  )
  .dependsOn(parsers)

lazy val tests = project
  .in(file("tests/shared"))
  .settings(
    skip.in(publish) := true,
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.0.5",
      "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
    )
  )
  .dependsOn(scalameta)

lazy val contrib = project
  .in(file("scalameta/contrib"))
  .settings(
    moduleName := "contrib-experimental",
    description := "Incubator for Scalameta APIs",
    scalaSource in Compile := baseDirectory.value / "shared" / "src"
  )
  .dependsOn(scalameta)

lazy val testkit = project
  .in(file("scalameta/testkit"))
  .settings(
    moduleName := "testkit-experimental",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5",
      "com.lihaoyi" %% "geny" % "0.1.2",
      // These are used to download and extract a corpus tar.gz
      "org.rauschig" % "jarchivelib" % "0.7.1",
      "commons-io" % "commons-io" % "2.5",
      "com.googlecode.java-diff-utils" % "diffutils" % "1.3.0"
    ),
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test,
    description := "Testing utilities for scalameta APIs"
  )
  .dependsOn(contrib)
