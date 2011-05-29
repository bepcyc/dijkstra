package org.seaton.dijkstra.core

import org.seaton.dijkstra.cases._
import collection.mutable.ListBuffer
import collection.mutable.HashMap
import route.{ShortestRouteError, ShortestRouteDoesNotExist, ShortestRouteInvalidSourceOrTarget, ShortestRoute}
import java.lang.{RuntimeException, StringBuilder}
import scala.Some
import org.seaton.dijkstra.util.GraphUtil

/**
 * Represents a graph with a map of nodes and a list of edges.
 *
 * @param nodes map of graph nodes with node id as key
 * @param edges list of graph edges
 */
class Graph(val nodes: Map[String, Node], val edges: List[Edge]) {

	// determine bad edge node ids (edge node ids that don't exist in nodes map)
	private val badEdgeNodeIds = new ListBuffer[String]()
	edges foreach (edge => {
		if (!nodes.contains(edge.nodeA)) badEdgeNodeIds += edge.nodeA
		if (!nodes.contains(edge.nodeB)) badEdgeNodeIds += edge.nodeB
	})
	if (badEdgeNodeIds.size > 0) throw (new RuntimeException("invalid node ids in edges: " + badEdgeNodeIds.toList))

	/**
	 * Graph as nodes and node distances.
	 */
	lazy val net = {
		val ndist = new HashMap[String, Map[String, Double]]()
		nodes foreach (node => {
			val n2n = new HashMap[String, Double]()
			neighborsOf(node._1) match {
				case Some(neighbors) => neighbors foreach (nid => {
					distanceBetween(node._1, nid) match {
						case Some(dist) => n2n += nid -> dist
						case _ => // no distance was calculated between nodes
					}
				})
				case _ => // no neighbors mean unconnected node
			}
			ndist += node._1 -> n2n.toMap
		})
		ndist.toMap
	}

	/**
	 * Pulls the neighbors from the graph as nodes and distances structure.
	 *
	 * @param nid source node to find neighbors of
	 *
	 * @return Option[List[String]] list of nieghbor node ids
	 */
	private def neighbors(nid: String): Option[List[String]] = {
		try {
			Some(for (nbr <- net(nid).toList) yield nbr._1)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Pulls the distances between nodes from graph as nodes and distances structure.
	 *
	 * @param src source node id (direction is not a factor)
	 * @param dest destination node id
	 *
	 * @return Option[Double] distance between src and dst nodes
	 */
	private def distances(src: String, dest: String): Option[Double] = {
		try {
			net(src).get(dest)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Determines the mininum distance node id.
	 *
	 * @param dist map of node distances with node id as key
	 * @param work list of currently active node ids
	 * @return node id of the node with least distance
	 */
	private def minDistanceId(dist: HashMap[String, Double], work: ListBuffer[String]): Option[String] = {
		try {
			var min: Double = Graph.INFINITE
			var mid: String = null
			dist foreach (d => {
				if (work.contains(d._1)) {
					if (d._2 < min) {
						min = d._2
						mid = d._1
					}
				}
			})
			if ((mid) == null) None else Some(mid)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Determines the neighbors of a node.
	 *
	 * @param nid node id
	 *
	 * @return Option[List[String]] list of node ids of neighboring nodes (connected by edge)
	 */
	private def neighborsOf(nid: String): Option[List[String]] = {
		try {
			Some(for (e <- edges; if (e.nodeA.equals(nid)) || (e.nodeB.equals(nid))) yield (if (e.nodeA.equals(nid)) e.nodeB else e.nodeA))
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Calculates the Cartesian distance between two (2) nodes (x,y).
	 *
	 * @param aid first node id
	 * @param bid second node id
	 *
	 * @return Option[Double] distance (Cartesian) between first and second node
	 */
	private def distanceBetween(aid: String, bid: String): Option[Double] = {
		try {
			Some(scala.math.sqrt(scala.math.pow((nodes(aid).x - nodes(bid).x), 2) + scala.math.pow((nodes(aid).y - nodes(bid).y), 2)))
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Calculates the shortest route (if any) between two (2) nodes in this graph.
	 *
	 * @param source (starting) node id
	 * @param target (ending) node id
	 *
	 * @return Option[ShortestRoute(List[Node])] for success, other graph cases for failed calculation
	 */
	def shortest(source: String, target: String): Option[GraphCase] = {
		try {
			if (source == target) {
				Some(ShortestRoute(List(nodes(source))))
			} else if (!nodes.contains(source) || !nodes.contains(target)) {
				Some(ShortestRouteInvalidSourceOrTarget())
			} else {
				val distance = HashMap.empty[String, Double]
				val previous = HashMap.empty[String, String]
				val working = ListBuffer[String]()
				nodes foreach (kv => {
					distance += (kv._1 -> Graph.INFINITE)
					previous += (kv._1 -> Graph.UNDEFINED)
					working += kv._1
				})
				distance += (source -> 0.0)
				var errMsg: String = null
				var closest: String = Graph.UNDEFINED
				while (working.size > 0) {
					minDistanceId(distance, working) match {
						case Some(mid) =>
							closest = mid
							if (distance(closest) == Graph.INFINITE) {
								println("no other nodes are accessible")
								closest = Graph.UNDEFINED
								working.clear()
							} else {
								working -= closest
								neighbors(closest) match {
									case Some(neighbrs) =>
										neighbrs foreach (neighbor => {
											distances(closest, neighbor) match {
												case Some(dist) =>
													val alternate = distance(closest) + dist
													if (alternate < distance(neighbor)) {
														distance(neighbor) = alternate
														previous(neighbor) = closest
													}
												case _ => println("""distance calc failed for edge %s and %s""".format(closest, neighbor))
											}
										})
									case _ =>
										errMsg = """Error determining neighbors for %s""".format(closest)
										working.clear()
								}
							}
						case _ => working.clear() // no more connected nodes to source
					}
				}
				if ((closest == Graph.UNDEFINED) || (distance(closest) == Graph.INFINITE)) {
					Some(ShortestRouteDoesNotExist())
				} else if (errMsg != null) {
					Some(ShortestRouteError())
				} else {
					val route = ListBuffer[Node]()
					var location = target
					while (previous(location) != Graph.UNDEFINED) {
						route.insert(0, nodes(previous(location)))
						location = previous(location)
					}
					if (route.size == 0) {
						Some(ShortestRouteDoesNotExist())
					} else {
						route += nodes(target)
						Some(ShortestRoute(route.toList))
					}
				}
			}
		} catch {
			case e: Exception =>
				e.printStackTrace()
				Some(ShortestRouteError()) // e.getMessage))
		}
	}

	/**
	 * Clones a new instance of the <code>Graph</code>.
	 *
	 * @return a new cloned instance of this <code>Graph</code>
	 */
	override def clone(): Graph = Graph(nodes, edges)


	/**
	 * Lazy string representation of the graph.
	 */
	lazy val str: String = {
		val sb = new StringBuilder()
		sb.append("nodes: ")
		nodes foreach (nd => sb.append("\n" + nd._2.toString))
		sb.append("\nedges: ")
		edges foreach (edge => sb.append("\n" + edge.toString()))
		sb.toString
	}

	/**
	 * Overridden toString that returns the lazy val str.
	 */
	override def toString: String = str

}

/**
 * Companion object to Graph class with simple factory apply() and a few constants.
 */
object Graph {
	/**
	 * Factory for Graph class.
	 *
	 * @param nodes map of nodes in graph with node id as key
	 * @param edges list of edges in graph
	 *
	 * @return new Graph instance
	 */
	def apply(nodes: Map[String, Node], edges: List[Edge]) = new Graph(nodes, edges)


	/**
	 * Represents an infinite distance while calculating distances between nodes.
	 */
	val INFINITE = Double.MaxValue

	/**
	 * Represents an undefined or uninitialized state.
	 */
	val UNDEFINED = "***undef***"

	/**
	 * Calculates via folding the distance traversed by a list of connected nodes.
	 *
	 * @param graph the graph to be traverse
	 * @param traversed the list of node ids to be traversed
	 *
	 * @return Option[Double] is the distance travelled by traversing the nodes
	 */
	def traversedDistance(graph: Graph, traversed: List[String]): Option[Double] = {
		try {
			var prev = Graph.UNDEFINED
			Some(traversed.foldLeft(0.0)((acc, nid) => {
				if (prev.equals(Graph.UNDEFINED)) {
					prev = nid
					0.0
				} else {
					val pv = prev
					prev = nid
					acc + graph.net(nid)(pv)
				}
			}))
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}
}
