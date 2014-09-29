libraryDependencies += "org.specs2" % "specs2-core_2.10" % "2.4.4-scalaz-7.0.6" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

  // Read here for optional dependencies:
  // http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)