package org.seaton.dijkstra.util

import org.seaton.dijkstra.cases._
import org.seaton.dijkstra.core._
import collection.mutable._
import generate.{GeneratedGraph, GeneratedGraphFailed}
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}
import com.sun.image.codec.jpeg.JPEGCodec
import java.io.{FileInputStream, FileOutputStream, InputStream}
import java.lang.{String, Double}
import route.{ShortestRoute, ShortestRouteError, ShortestRouteInvalidSourceOrTarget, ShortestRouteDoesNotExist}

/**
 * Provides utility functions and runner for the Graph class illustrating the Dijkstra algorithm of finding the shortest (least costly) route between two (2) nodes.
 * The <code>GraphUtil</code> object generates a regular polygon (an n-sided polygon with each side of equal length).
 *
 * This is done out of convenience since the <code>Graph</code> class can contain any number of nodes and edges in any configuration and still determine the shortest route.
 *
 * <h3>Usage</h3>
 *
 * <pre>
 * $ scala GraphUtil [&lt;#-of-nodes&gt; [&lt;source-node-id&gt; [&lt;target-node-id&gt; [&lt;spike-node-enabled&gt;]]]]
 *
 * sbt> run [&lt;#-of-nodes&gt; [&lt;source-node-id&gt; [&lt;target-node-id&gt; [&lt;spike-node-enabled&gt;]]]]
 * </pre>
 *
 * where
 *
 * <h4>Arguments:</h4>
 * <ul>
 * <li>
 * <em>#-of-nodes</em> is the number of nodes generated in a regular polygon graph with edges forming the sides of regular polygon (defaults to 23)
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
 * $ scala GraphUtil 17 0 8
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
 * $ scala GraphUtil 17 0a 8b true
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
 * The <code>GraphUtil</code> object automatically exports an image representing the graph and illustrating the shortest route traversal.
 *
 * The exported graph images are saved in the <code>.../<em>&lt;root&gt;</em>/exported-graph-images/</code> folder.
 *
 */
object GraphUtil {

	/**
	 * Length of 'spikes' that have an 'a' and 'b' node from a regular polygon node.
	 */
	private val spike = 12

	/**
	 * Delta on spike theta angle.  Used for 'spikes' off polygon graph nodes.
	 */
	private val delta = 0 // scala.math.Pi

	/**
	 * Simple date format (yyyyMMdd.HHmmss) for natural ordering of dates.
	 */
	private val SDF = new SimpleDateFormat("yyyyMMdd.HHmmss")

	/**
	 * Simple date format (yyyy-MM-dd HH:mm:ss Z) with better human readability and time offset from GTM.
	 */
	private val SDF_NICE = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss Z")

	/**
	 * Exported graph image background color.
	 */
	private val BACKGROUND_COLOR = Color.WHITE

	/**
	 * Exported graph image node color.
	 */
	private val NODE_COLOR = Color.LIGHT_GRAY

	/**
	 * Export graph image node source (start) node color for node traversals.
	 */
	private val NODE_SOURCE_COLOR = Color.GREEN

	/**
	 * Export graph image node target (end) node color for node traversals.
	 */
	private val NODE_TARGET_COLOR = Color.ORANGE

	/**
	 * Export graph image traversed node (node in traversal but not source or target).
	 */
	private val NODE_TRAVERSED_COLOR = Color.RED

	/**
	 * Export graph image node text (id) color.
	 */
	private val NODE_TEXT_COLOR = Color.BLACK

	/**
	 * Export graph image edge color.
	 */
	private val EDGE_COLOR = Color.LIGHT_GRAY

	/**
	 * Export graph image traversed edge color.
	 */
	private val EDGE_TRAVERSED_COLOR = Color.RED

	/**
	 * Export graph image node size/radius.
	 */
	private val NODE_SIZE = 2

	/**
	 * Paints a graph node onto a graphics object.
	 *
	 * @param g graphics2d to paint onto
	 * @param x X-coordinate for node paint
	 * @param y Y-cooridnate for node paint
	 * @param nodeSize size of node
	 */
	private def paintNode(g: Graphics2D, x: Int, y: Int, nodeSize: Int) {
		val nx = x - (nodeSize / 2)
		val ny = y - (nodeSize / 2)
		g.fillOval(nx, ny, nodeSize, nodeSize)
	}

	/**
	 * Paints an image onto a graphics object at a point.
	 *
	 * @param g graphics2d to paint onto
	 * @param x X-coordinate for image paint
	 * @param y Y-cooridnate for image paint
	 * @param is input stream of jpeg image
	 */
	private def paintImage(g: Graphics2D, x: Int, y: Int, is: InputStream) {
		try {
			val img = ImageIO.read(is)
			val x1 = x - (img.getWidth / 2)
			val y1 = y - (img.getHeight / 2)
			g.drawImage(img, x1, y1, null)
		} catch {
			case e: Exception => e.printStackTrace()
		}
	}

	/**
	 * Exports a graph as an image (jpg) with optional information, zoom, and traversal route highlighting.
	 *
	 * @param graph graph to export as image
	 * @param height height of image
	 * @param width width of image
	 * @param fn filename of exported jpeg image
	 * @param lineUpperLeft text to draw on upper-left corner of image (defaults to "")
	 * @param lineLowerLeft text to draw on lower-left corner of image (defaults to "")
	 * @param xoffset x offset to paint/draw on image (defaults to 0)
	 * @param yoffset y offset to paint/draw on image (defaults to 0)
	 * @param zoom zoom factor for exported image (defaults to 1.0)
	 * @param traveral list of node ids to illustrate node traversal (defaults to null/ignored)
	 * @param logo filename of jpeg image to center in the export jpeg image (defaults to null/ignored)
	 *
	 * @return Option[String] for success with export jpeg filename; otherwise None
	 */
	def exportGraphImage(graph: Graph, height: Int, width: Int, fn: String, lineUpperLeft: String = "", lineLowerLeft: String = "",
						 xoffset: Int = 0, yoffset: Int = 0, zoom: Double = 1.0, traversal: List[String] = null, logo: String = null): Option[String] = {
		try {

			val w = (width * zoom).asInstanceOf[Int]
			val h = (height * zoom).asInstanceOf[Int]
			val image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
			val g = image.createGraphics()

			// background
			g.setColor(BACKGROUND_COLOR)
			g.fillRect(0, 0, w, h)

			// text
			g.setColor(NODE_TEXT_COLOR)
			g.drawString(lineUpperLeft, 10, 20)
			g.drawString(lineLowerLeft, 10, h - 10)

			// logo
			if (logo != null) {
				try {
					val fis = new FileInputStream(logo)
					paintImage(g, w / 2, h / 2, fis)
				} catch {
					case e: Exception => e.printStackTrace()
				}
			}

			// edges
			g.setColor(EDGE_COLOR)
			graph.edges foreach (edge => {
				val na = graph.nodes(edge.nodeA)
				val nb = graph.nodes(edge.nodeB)
				g.drawLine((na.x * zoom).asInstanceOf[Int] + xoffset, (na.y * zoom).asInstanceOf[Int] + yoffset,
					(nb.x * zoom).asInstanceOf[Int] + xoffset, (nb.y * zoom).asInstanceOf[Int] + yoffset)
			})

			val nodeSize = (NODE_SIZE * zoom).asInstanceOf[Int]

			// nodes as circles
			graph.nodes foreach (nds => {
				val node = nds._2
				val x = (node.x * zoom).asInstanceOf[Int] + xoffset
				val y = (node.y * zoom).asInstanceOf[Int] + yoffset
				g.setColor(NODE_COLOR)
				paintNode(g, x, y, nodeSize)
				g.setColor(NODE_TEXT_COLOR)
				g.drawString(node.id, x, y)
			})

			// traversed route
			if (traversal != null) {
				var prev: String = null
				traversal foreach (nid => {
					if (prev != null) {
						g.setColor(EDGE_TRAVERSED_COLOR)
						val na = graph.nodes(nid)
						val nb = graph.nodes(prev)
						g.drawLine((na.x * zoom).asInstanceOf[Int] + xoffset, (na.y * zoom).asInstanceOf[Int] + yoffset,
							(nb.x * zoom).asInstanceOf[Int] + xoffset, (nb.y * zoom).asInstanceOf[Int] + yoffset)
						val x = (na.x * zoom).asInstanceOf[Int] + xoffset
						val y = (na.y * zoom).asInstanceOf[Int] + yoffset
						g.setColor(NODE_TRAVERSED_COLOR)
						paintNode(g, x, y, nodeSize)
						g.setColor(NODE_TEXT_COLOR)
						g.drawString(na.id, x, y)
					} else {
						g.setColor(NODE_SOURCE_COLOR)
						val na = graph.nodes(nid)
						val x = (na.x * zoom).asInstanceOf[Int] + xoffset
						val y = (na.y * zoom).asInstanceOf[Int] + yoffset
						paintNode(g, x, y, 2 * nodeSize)
						g.setColor(NODE_TEXT_COLOR)
						g.drawString(na.id, x, y)
					}
					prev = nid
				})
				g.setColor(NODE_TARGET_COLOR)
				val na = graph.nodes(prev)
				val x = (na.x * zoom).asInstanceOf[Int] + xoffset
				val y = (na.y * zoom).asInstanceOf[Int] + yoffset
				paintNode(g, x, y, 2 * nodeSize)
				g.setColor(NODE_TEXT_COLOR)
				g.drawString(na.id, x, y)
			}

			val fos = new FileOutputStream(fn)
			val encoder = JPEGCodec.createJPEGEncoder(fos)
			encoder.encode(image)

			Some(fn)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Creates 'spike' nodes (a and b) off a regular polygon node.
	 *
	 * @param key id of the polygon node to attach two 'spike' nodes
	 * @param theta polygon slice angle used to angle the spike nodes
	 * @param x X-cooridnate of polygon node
	 * @param y Y-coordinate of polygon node
	 * @param spike length/distance of spike nodes from polygon node
	 *
	 * @return Option[(Node, Node)] with spike nodes; otherwise None
	 */
	private def polySpikes(key: String, theta: Double, x: Double, y: Double, spike: Int): Option[(Node, Node)] = {
		try {
			val x1 = spike + (spike * scala.math.cos(theta + delta))
			val y1 = spike + (spike * scala.math.sin(theta + delta))
			val na = Node(key + "a", x + x1, y + y1)
			val nb = Node(key + "b", x - x1, y - y1)
			Some(na, nb)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Generates a graph with edges connecting the nodes forming a regular polygon with an arbitrary number of sides (nodes/edge).
	 *
	 * @param slices equivalent to the number of sides/nodes of a regular polygon
	 * @param radius radius of the unit circle used to generate regular polygon nodes
	 * @param spiky true to add spikes (two nodes connected to each polygon node); false for no spike nodes
	 *
	 * @return Option[GeneratedGraph(Graph)] for success; Option[GraphGeneratedFailed(msg)] otherwise
	 *
	 */
	def polygonGraph(slices: Int, radius: Double, spiky: Boolean = true): Option[GraphCase] = {
		try {
			val slice = (2 * scala.math.Pi) / (1.0 * slices)
			val nodes: HashMap[String, Node] = HashMap.empty[String, Node]
			val edges = new ListBuffer[Edge]()
			(0 until slices) foreach (n => {
				val theta = slice * n
				val x = radius + (radius * scala.math.cos(theta))
				val y = radius + (radius * scala.math.sin(theta))
				val key = String.valueOf(n)
				nodes += (key -> Node(key, x, y))
				if (spiky) {
					polySpikes(key, theta, x, y, spike) match {
						case Some(ntuple) =>
							val na = ntuple._1
							val nb = ntuple._2
							nodes += (na.id -> na)
							nodes += (nb.id -> nb)
							edges += Edge(key, na.id)
							edges += Edge(key, nb.id)
						case _ => println("No spike nodes for node(" + key + ")")
					}
				}
				if (n > 0) {
					edges += Edge(String.valueOf(n - 1), key)
				} else {
					edges += Edge(String.valueOf(slices - 1), "0")
				}
			})
			Some(GeneratedGraph(Graph(nodes.toMap, edges.toList)))
		} catch {
			case e: Exception =>
				e.printStackTrace()
				Some(GeneratedGraphFailed(e.getMessage))
		}
	}

	/**
	 * Determines value equality for Edge based on node ids with node order is irrelevant.
	 *
	 * @param edge the Edge to compare against
	 * @param na first node id
	 * @param nb second node id
	 *
	 * @return true if the edge contains both node ids
	 */
	def isEdge(edge: Edge, na: String, nb: String): Boolean = {
		(edge.nodeA.equals(na) && edge.nodeB.equals(nb)) || (edge.nodeA.equals(nb) && edge.nodeB.equals(na))
	}

	/**
	 * Prints usage and examples to console.
	 */
	private def usage() {
		println("scala GraphUtil [<nodes> [<source> [<target> [<spikes>]]]]")
		println("ex. scala GraphUtil 17 1 7 false")
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

			// args foreach (arg => println("arg: " + arg))

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

			val fn = "exported-graph-images/graph." + slices + "." + source + "." + target + "." + SDF.format(new Date()) + ".jpg"
			val imgFn = "src/main/resources/novus-logo.jpg"

			polygonGraph(slices, unit, spikes) match {
				case Some(graphCase) =>
					graphCase match {
						case GeneratedGraph(graph) =>
							graph.shortest(source, target) match {
								case Some(graphCase) =>
									graphCase match {
										case ShortestRoute(nodes) =>
											print("Shortest route generated: ")
											val info = """%dn;%de::""".format(graph.nodes.size, graph.edges.size)
											val nodeIds = new ListBuffer[String]()
											val sb = new StringBuilder()
											nodes foreach (node => {
												print(" -> " + node.id)
												nodeIds += node.id
												if (sb.size > 0) sb.append("->")
												sb.append(node.id)
											})
											println()
											val tdist = Graph.traversedDistance(graph, nodeIds.toList) match {
												case Some(dist) => dist
												case _ => -1.0
											}
											println("""Distance: %f""".format(tdist))
											GraphUtil.exportGraphImage(graph, (unit * 2).asInstanceOf[Int] + xoffset, (unit * 2).asInstanceOf[Int] + yoffset, fn,
												SDF_NICE.format(new Date()), info + sb.toString, xoffset, yoffset, zoom, nodeIds.toList, imgFn)
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