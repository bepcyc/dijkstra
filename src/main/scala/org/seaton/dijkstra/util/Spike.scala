package org.seaton.dijkstra.util

import collection.mutable.ListBuffer
import org.seaton.dijkstra.core.{Graph, Edge, Node}
import org.seaton.dijkstra.cases.generate.GeneratedGraph
import org.seaton.dijkstra.cases.route.{ShortestRouteInvalidSourceOrTarget, ShortestRouteError, ShortestRouteDoesNotExist, ShortestRoute}
import java.lang.Double
import com.sun.org.apache.xpath.internal.operations.Variable

object Spike {

	def isEdge(edge: Edge, na: String, nb: String): Boolean = {
		(edge.nodeA.equals(na) && edge.nodeB.equals(nb)) || (edge.nodeA.equals(nb) && edge.nodeB.equals(na))
	}

	def generateGraph(sides: Int, units: Double = 100.0, spikes: Boolean = false): Graph = {
		GraphUtil.polygonGraph(sides, units, spikes) match {
			case Some(graphCase) =>
				graphCase match {
					case GeneratedGraph(g) => g
					case _ => null
				}
			case _ => null
		}
	}


	def shortest(g: Graph, source: String, target: String) {
		g.shortest(source, target) match {
			case Some(graphCase) =>
				graphCase match {
					case ShortestRoute(route) => println("shortest: " + route)
					case ShortestRouteDoesNotExist() => println("route not exists: ")
					case ShortestRouteError() => println("route error: ")
					case ShortestRouteInvalidSourceOrTarget() => println("src/target invalid: ")
					case _ => println("error")
				}
			case _ => println("something bad happened...")
		}
	}

	def main3(args: Array[String]) {
		try {
			val slices = 5
			val units = 100.0
			val spikes = false

			val graph = generateGraph(slices, units, spikes)
			val oedges = new ListBuffer[Edge]()

			graph.edges foreach (edge => {
				if (!isEdge(edge, "0", "4") && !isEdge(edge, "2", "3")) oedges += edge
			})

			println(graph)
			val ograph = Graph(graph.nodes, oedges.toList)
			println(ograph)

			val fn = "exported-graph-images/spike." + slices + "." + System.currentTimeMillis() + ".jpg"
			GraphUtil.exportGraphImage(ograph, 250, 250, fn, "", "", 40, 40, 2.0)

			ograph.shortest("0", "3") match {
				case Some(graphCase) =>
					graphCase match {
						case ShortestRoute(route) => println("shortest: " + route)
						case ShortestRouteDoesNotExist() => println("route not exists: ")
						case ShortestRouteError() => println("route error: ")
						case ShortestRouteInvalidSourceOrTarget() => println("src/target invalid: ")
						case _ => println("error")
					}
				case _ => println("something bad happened...")
			}

			val sing = generateGraph(1)
			shortest(sing, "0", "0")

			GraphUtil.exportGraphImage(sing, 250, 250, fn, "", "", 40, 40, 2.0)

		} catch {
			case e: Exception => e.printStackTrace()
		}
	}

	/**
	 * Converts/rounds a double to a particular decimal place.
	 *
	 * @param d double to be converted
	 * @param places decimal places to convert/round to
	 *
	 * @return converted/rounded double to the designated decimal point
	 */
	private def decimalPlaces(d: Double, places: Int): Double = {
		val f = scala.math.pow(10.0, places)
		(d * f).round / f
	}

	def main2(args: Array[String]) {
		try {
			var n = 12345.12345
			(-2 until 6) foreach (dp => println(decimalPlaces(n, dp)))
		} catch {
			case e: Exception => e.printStackTrace()
		}
	}

}