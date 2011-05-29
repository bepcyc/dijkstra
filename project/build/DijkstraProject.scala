import sbt._

class DijkstraProject(info: ProjectInfo) extends DefaultProject(info) {
    val specs = "org.scala-tools.testing" % "specs_2.9.0" % "1.6.8" // specs testing lib
    lazy val demo = runTask(Some("org.seaton.dijkstra.util.GraphUtil"), runClasspath).dependsOn(compile) describedAs "Runs the Dijkstra demo."
}
