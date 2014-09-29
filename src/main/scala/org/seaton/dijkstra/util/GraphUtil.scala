package org.seaton.dijkstra.util

import org.seaton.dijkstra.cases._
import org.seaton.dijkstra.core._
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
import sun.management.counter.Units
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

/**
 * Provides utility functions and runner for the Graph class illustrating the Dijkstra algorithm of finding the shortest (least costly) route between two (2) nodes.
 * The <code>GraphUtil</code> object generates a regular polygon (an n-sided polygon with each side of equal length).
 *
 * This is done out of convenience since the <code>Graph</code> class can contain any number of nodes and edges in any configuration and still determine the shortest route.
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
	val SDF = new SimpleDateFormat("yyyyMMdd.HHmmss")

	/**
	 * Simple date format (yyyy-MM-dd HH:mm:ss Z) with better human readability and time offset from GTM.
	 */
	val SDF_NICE = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss Z")

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
	def exportGraphImage(graph: Graph[String], height: Int, width: Int, fn: String, lineUpperLeft: String = "", lineLowerLeft: String = "",
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
	private def polySpikes(key: String, theta: Double, x: Double, y: Double, spike: Int): Option[(Node[String], Node[String])] = {
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
			val nodes: HashMap[String, Node[String]] = HashMap.empty[String, Node[String]]
			val edges = new ListBuffer[Edge[String]]()
			(0 until slices) foreach (n => {
				val theta = slice * n
				var offset = 0.0
				if (n == slices - 1) offset = radius * 0.05 // % of radius offset to ensure equi-leg from start 0 goes 0>1>2...>dest route
				val x = radius + (radius * scala.math.cos(theta)) + (offset * scala.math.cos(theta))
				val y = radius + (radius * scala.math.sin(theta)) + (offset * scala.math.sin(theta))
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
	def isEdge(edge: Edge[String], na: String, nb: String): Boolean = {
		(edge.nodeA.equals(na) && edge.nodeB.equals(nb)) || (edge.nodeA.equals(nb) && edge.nodeB.equals(na))
	}

}