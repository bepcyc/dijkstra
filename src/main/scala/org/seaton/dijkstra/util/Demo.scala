package org.seaton.dijkstra.util

import org.seaton.dijkstra.cases.generate.{GeneratedGraphFailed, GeneratedGraph}
import org.seaton.dijkstra.cases.route.{ShortestRouteError, ShortestRouteInvalidSourceOrTarget, ShortestRouteDoesNotExist, ShortestRoute}
import scala.Predef._
import java.util.Date
import org.seaton.dijkstra.core.Graph

/**
 * <h3>Usage</h3>
 *
 * <pre>
 * $ scala Demo [&lt;num-of-nodes&gt; [&lt;source-node-id&gt; [&lt;target-node-id&gt; [&lt;spike-node-enabled&gt;]]]]
 *
 * sbt> run [&lt;num-of-nodes&gt; [&lt;source-node-id&gt; [&lt;target-node-id&gt; [&lt;spike-node-enabled&gt;]]]]
 * </pre>
 *
 * where
 *
 * <h4>Arguments:</h4>
 * <ul>
 * <li>
 * <em>num-of-nodes</em> is the number of nodes generated in a regular polygon graph with edges forming the sides of regular polygon (defaults to 23)
 * </li>
 * <li>
 * <em>source-node-id</em> is the node id of the starting node for calculating the shortest (least costly) route using Dijkstra's algorithm (defaults to 0)
 * </li>
 * <li>
 * <em>target-node-id</em> is the node id of the ending node for calculating the shortest (least costly) route using Dijkstra's algorithm (defaults to 11)
 * </li>
 * <li>
 * <em>spike-enabled</em> is a flag to add two (2) 'spike' nodes per polygon node (see below for more information) (defaults to false)
 * </li>
 * </ul>
 *
 * <h4>Argument notes</h4>
 * <ul>
 * <li>
 * If there are *n* number of nodes, the node ids are 0 to n-1 (e.g. "0","1","2",...,"n-1").
 * </li>
 * <li>
 * The order of source and target nodes is irrelevant (other than affecting the direction of the shortest route (if any exist)).
 * </li>
 * </ul>
 *
 * <h3>Regular Polygon Graphs</h3>
 *
 * To exercise the algorithm and generate a exported graph image with shortest route traversal:
 *
 * from command-line:
 *
 * <pre>
 * $ scala Demo 17 0 8
 * </pre>
 *
 * or in <strong>sbt</strong>
 * <pre>
 * sbt&gt; run 17 0 8
 * </pre>
 *
 * Both of these commands will create a graph as a 17-sided regular polygon with edges as sides, calculate the
 * shortest route from node 0 to node 8, and then export the graph, with illustrated traversal, into the
 * <code>.../<em>&lt;root&gt;</em>/exported-graph-images/</code> folder as the file <code>graph.17.0.8.&lt;timestamp&gt;.jpg</code>.
 *
 * <h3>Regular Polygon Graphs with Spikes</h3>
 *
 * An additional element to exercise the algorithm is to create 'spikes' on the regular polygon graph.  Spikes are
 * essentially two (2) additionally nodes associated/connected to each polygon node to create multiple terminal
 * nodes and also illustrate traversal that is not part of a closed graph.
 *
 * Spike nodes have id's with an 'a' or 'b' appended to the polygon node id to which the spike node is associated.
 *
 * For each polygon node, there is an 'a' and 'b' spike node (if spike nodes are turned on).
 *
 * For example, for node "0" with spike nodes enabled, there will also be a "0a" and "0b" node with edges connecting "0" to "0a"
 * and "0" to "0b", but no edge connecting "0a" and "0b" directly.
 *
 * To exercise the algorithm with spikes and generate a graph image with the shortest route traversal:
 *
 * from command-line:
 *
 * <pre>
 * $ scala Demo 17 0a 8b true
 * </pre>
 *
 * or in <strong>sbt</strong>:
 *
 * <pre>
 * sbt&gt; run 17 0a 8b true
 * </pre>
 *
 * Both of these commands will create a graph as a 17-sided regular polygon with edges as sides and associated spike
 * nodes, calculate the shortest route from node 0a to node 8b, and then export the graph, with illustrated traversal,
 * into the <code>.../<em>&lt;root&gt;</em>/exported-graph-images/</code> folder as the file <code>graph.17.0a.8b.&lt;timestamp&gt;.jpg</code>.
 *
 * <h3>Exported Graph Images</h3>
 *
 * The <code>Demo</code> object automatically exports an image representing the graph and illustrating the shortest route traversal.
 *
 * The exported graph images are saved in the <code>.../<em>&lt;root&gt;</em>/exported-graph-images/</code> folder.
 *
 */
object Demo {

	/**
	 * Prints usage and examples to console.
	 */
	private def usage() {
		println("scala Demo [<nodes> [<source> [<target> [<spikes>]]]]")
		println("ex. scala Demo 17 1 7 false")
		println("    - creates a graph as a regular polygon with 17 sides, finds the short route between")
		println("      <source> and <target>")
	}

	/**
	 * Utility graph runner that illustrates Dijkstra's algorithm.
	 *
	 * @param args command-line arguments; 0=slices/#nodes;1=source(node id);2=target(node id);3=spikes(t,true,yes,y = true; otherwise=false)
	 */
	def main(args: Array[String]) {
		try {

			var slices = 23
			var source = "0"
			var target = "11"
			var spikes = false

			val argsLen = args.length
			if (argsLen > 0) {
				try {
					slices = Integer.parseInt(args(0))
				} catch {
					case e: Exception => e.printStackTrace()
				}
			}
			if (argsLen > 1) source = args(1)
			if (argsLen > 2) target = args(2)
			if (argsLen > 3) {
				args(3).trim().toLowerCase match {
					case "t" => spikes = true
					case "true" => spikes = true
					case "yes" => spikes = true
					case "y" => spikes = true
					case _ => spikes = false
				}
			}

			val unit = 100.0
			val xoffset = 40
			val yoffset = 40
			val zoom = 3.0

			val fn = "exported-graph-images/graph." + slices + "." + source + "." + target + "." + GraphUtil.SDF.format(new Date()) + ".jpg"
			val imgFn = "src/main/resources/novus-logo.jpg"

			GraphUtil.polygonGraph(slices, unit, spikes) match {
				case Some(graphCase) =>
					graphCase match {
						case GeneratedGraph(graph) =>
							// graph.shortestPath(source, target) match {
							Graph.shortestPath(graph, source, target) match {
								case Some(graphCase) =>
									graphCase match {
										case ShortestRoute(nodes, dist) =>
											print("Shortest route generated: ")
											val info = """%dn;%de::""".format(graph.nodes.size, graph.edges.size)
											val sb = new StringBuilder()
											nodes foreach (node => {
												print(" -> " + node)
												if (sb.size > 0) sb.append("->")
												sb.append(node)
											})
											println()
											println("""Distance: %f""".format(dist))
											GraphUtil.exportGraphImage(graph, (unit * 2).asInstanceOf[Int] + xoffset, (unit * 2).asInstanceOf[Int] + yoffset, fn,
												GraphUtil.SDF_NICE.format(new Date()), info + sb.toString, xoffset, yoffset, zoom, nodes, imgFn)
											println("""Exported graph image: '%s'""".format(fn))
										case ShortestRouteDoesNotExist() => println("no shortest route")
										case ShortestRouteInvalidSourceOrTarget() => println("invalid source/target")
										case ShortestRouteError() => println("shortest route error")
									}
								case _ => println("should never get here...")
							}
						case GeneratedGraphFailed(msg) => println("graph generation failed: " + msg)
						case _ => println("should never get here...")
					}
				case _ => println("should never get here...")
			}

		} catch {
			case e: Exception => e.printStackTrace()
		}
	}

}