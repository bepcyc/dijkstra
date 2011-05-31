package org.seaton.dijkstra.core

import org.seaton.dijkstra.cases._
import collection.mutable.ListBuffer
import collection.mutable.HashMap
import route.{ShortestRouteError, ShortestRouteDoesNotExist, ShortestRouteInvalidSourceOrTarget, ShortestRoute}
import collection.immutable.SortedMap
import java.lang.{RuntimeException, StringBuilder}

import scala.Some

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
	def shortestPath(source: String, target: String): Option[GraphCase] = {
		try {
			if (source == target) {
				Some(ShortestRoute(List(source), 0.0))
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
					val route = ListBuffer[String]()
					var location = target
					while (previous(location) != Graph.UNDEFINED) {
						route.insert(0, nodes(previous(location)).id)
						location = previous(location)
					}
					if (route.size == 0) {
						Some(ShortestRouteDoesNotExist())
					} else {
						route += target
						val tdist = Graph.traversedDistance(this, route.toList) match {
							case Some(d) => d
							case _ => -999.999
						}

						Some(ShortestRoute(route.toList, tdist))
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
 * Companion object to Graph class with simple factory apply(), constants, and functional Dijkstra's algorithm implementation.
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

	/**
	 * First-order function to determine connected nodes to given node in graph.
	 */
	val neighbors = (net: Map[String, Map[String, Double]], nid: String) => {
		try {
			Some(for (nbr <- net(nid).toList) yield nbr._1)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * First-order function to determine distance between nodes with (x,y) coordinates.
	 */
	val distance = (net: Map[String, Map[String, Double]], source: String, target: String) => {
		try {
			net(source).get(target)
		} catch {
			case e: Exception =>
				e.printStackTrace()
				None
		}
	}

	/**
	 * Recursive function to build path from distance and previous node/distance maps.
	 *
	 * @param pdist tuple with node id and distance
	 * @param preds map with node id as key and tuple of connected node id and distance
	 * @param source source node id
	 * @param path list of node ids
	 *
	 * @return Option[List[String]] with shortest (least cost) path node ids
	 */
	private def buildPathRecur(pdist: (String, Double), preds: Map[String, (String, Double)], source: String, path: List[String]
								  ): Option[List[String]] = {
		try {
			val pred = pdist._1
			if (pred == null) {
				None
			} else {
				if (pred.equals(source)) {
					Some(source :: path)
				} else {
					buildPathRecur(preds(pred), preds, source, pred :: path)
				}
			}
		} catch {
			case e: Exception => None
		}
	}

	/**
	 * Initialization build path function to calls the recursive build path function to build path from distance and previous node/distance maps.
	 *
	 * @param pdist tuple with node id and distance
	 * @param preds map with node id as key and tuple of connected node id and distance
	 * @param source source node id
	 * @param target target node id
	 * @param path list of node ids
	 *
	 * @return Option[List[String]] with shortest (least cost) path node ids
	 */
	private def buildPath(preds: Map[String, (String, Double)], source: String, target: String): Option[List[String]] = {
		try {
			buildPathRecur(preds(target), preds, source, List(target))
		} catch {
			case e: Exception => None
		}
	}

	/**
	 * Adds relative distances.
	 *
	 * @param rdists sorted map with distance as key and edges corresponding to distance.
	 * @param nid node id
	 * @param prevNid previous node id
	 * @param dist distance
	 * @param prevDist previous distance
	 *
	 * @return Option[] with updated relative distance sorted map
	 */
	private def addRdist(rdists: SortedMap[Double, Map[String, String]], nid: String, prevNid: String,
								dist: Double, prevDist: Double = -1.0): Option[SortedMap[Double, Map[String, String]]] = {
		try {
			if (prevDist < 0) {
				rdists.get(dist) match {
					case Some(nodes) => Some(rdists + (dist -> (nodes + (nid -> prevNid))))
					case _ => Some(rdists + (dist -> Map(nid -> prevNid)))
				}
			} else {
				addRdist(rdists, nid, prevNid, dist) match {
					case Some(nrdists) =>
						val minnodes = rdists(prevDist)
						val nminnodes = minnodes - nid
						if (nminnodes.isEmpty) {
							Some(nrdists - prevDist)
						} else {
							Some(nrdists + (dist -> Map(nid -> prevNid)))
						}
					case _ => println("inside addRdist: failed to return addRdist"); None
				}
			}
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}

	/**
	 * Updates the relative distances between nodes.
	 *
	 * @param rdists sorted map with distance as key and edges with those distances
	 * @param net map with node id as key and tuples with connected nodes and distance
	 * @param nid current node id to update
	 * @param dist distance
	 * @param neighbors function to determine connected nodes (edges)
	 * @param distance function to determine distance between nodes
	 *
	 * @return Option[] with updated relative distances and previous distances.
	 */
	private def updateRdists(rdists: SortedMap[Double, Map[String, String]],
								preds: Map[String, (String, Double)],
								net: Map[String, Map[String, Double]],
								nid: String,
								dist: Double,
								neighbors: ((Map[String, Map[String, Double]], String) => Option[List[String]]),
								distance: ((Map[String, Map[String, Double]], String, String) => Option[Double])
								): Option[(SortedMap[Double, Map[String, String]], Map[String, (String, Double)])] = {
		try {
			neighbors(net, nid) match {
				case Some(chds) =>
					chds.foldLeft(Option((rdists, preds))) { (orpair, neighbor) =>
						val rpair = orpair match {
							case Some(rp) => rp
							case _ => null // should never be there
						}
						val curDist: Double = distance(net, nid, neighbor) match {
							case Some(db) => (db + dist).asInstanceOf[Double]
							case _ => -1.0
						}

						val prevDist: Double = preds.get(neighbor) match {
							case Some(ppair) => ppair._2.asInstanceOf[Double]
							case _ => -1.0
						}
						val nrDists = rpair._1
						val nPreds = rpair._2
						if (prevDist == -1.0) {
							addRdist(nrDists, neighbor, nid, curDist) match {
								case Some(ard) => Some((ard, nPreds + (neighbor -> (nid, curDist))))
								case _ => println("failed to add r dist: "); orpair
							}

						} else {
							if (curDist < prevDist) {
								addRdist(nrDists, neighbor, nid, curDist, prevDist) match {
									case Some(ard) => Some((ard, nPreds + (neighbor -> (nid, curDist))))
									case _ => println("failed to add r dist: "); orpair
								}
							} else {
								orpair
							}
						}
					}
				case _ => Some((rdists, preds)) // no neighbor for node
			}
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}

	/**
	 * Determines minimum distance node.
	 *
	 * @param rdists sorted map with distance as key and edges with those distances
	 * @param preds map with node ids pointing to previous distances to other nodes
	 *
	 * @return Tuple(minimum distance node id, minimum distance, updated relative distances, update previous distances)
	 */
	private def takeMinNode(rdists: SortedMap[Double, Map[String, String]], preds: Map[String, (String, Double)]
							   ):Option[(String, Double, SortedMap[Double, Map[String, String]], Map[String, (String, Double)])] = {
		try {
			val dist = rdists.firstKey
			val minNodes = rdists(dist)
			val minNode = minNodes.head._1
			val prevNid = minNodes.head._2
			val otherNodes = minNodes.tail
			Some((minNode, dist, if (otherNodes.isEmpty) rdists - dist else rdists + (dist -> otherNodes), preds + (minNode -> (prevNid, dist))))
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}

	/**
	 * Recursively calculates the shortest route from root to destination node.
	 *
	 * @param net graph represented by nodes with connected nodes and distances
	 * @param source starting node id
	 * @param target ending node id
	 * @param children function to determine neighbors to a particular node
	 * @param distances function to calculate/retrieve distance between two connection nodes in graph
	 * @param rdists sorted map with distance as key and edges with those distances
	 * @param minNode closest node id
	 * @param preds map with node ids pointing to previous distances to other nodes
	 * @param dist current distance between nodes
	 *
	 * @return Option[] update previous distances
	 */
	private def short(net: Map[String, Map[String, Double]], source: String, target: String,
						  neighbors: ((Map[String, Map[String, Double]], String) => Option[List[String]]),
						  distance: ((Map[String, Map[String, Double]], String, String) => Option[Double]),
						  rdists: SortedMap[Double, Map[String, String]],
						  minNode: String,
						  preds: Map[String, (String, Double)],
						  dist: Double
						 ): Option[Map[String, (String, Double)]] = {
		try {
			if (rdists.isEmpty) {
				Some(preds)
			} else {
				takeMinNode(rdists, preds) match {
					case Some(take) =>
						updateRdists(take._3, take._4, net, take._1, take._2, neighbors, distance) match {
							case Some(update) => short(net, source, target, neighbors, distance, update._1, take._1, update._2, take._2)
							case _ => throw (new RuntimeException("error updated rel distances: " + take._3 + ":" + take._4 + ":" + net + ":" + take._1 + ":" + take._2))
						}
					case _ => throw (new RuntimeException("error generating takeMinNode: " + rdists + ":" + preds))
				}
			}
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}

	/**
	 * Calculates the shortest path between connected nodes.
	 *
	 * @param net graph represented by nodes with connected nodes and distances
	 * @param source source node id in graph
	 * @param target target/destination node id in graph
	 * @param neighbors function to determine neighbors to a particular node
	 * @param distance function to calculate/retrieve distance between two connection nodes in graph
	 *
	 * @return map with node id as key and relative distances to each connected node
	 */
	private def dijkstra(net: Map[String, Map[String, Double]], source: String, target: String,
				 neighbors: ((Map[String, Map[String, Double]], String) => Option[List[String]]),
				 distance: ((Map[String, Map[String, Double]], String, String) => Option[Double])
							): Option[Map[String, (String, Double)]] = {
		try {
			val rdists = SortedMap(0.0 -> Map(source -> source))
			val minNode = source
			val preds = Map(source -> (source, 0.0))
			val dist = 0.0
			short(net, source, target, neighbors, distance, rdists, minNode, preds, dist)
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}


	/**
	 * Calculates the shortest path given a graph, source and target nodes, and neighbor and distance functions.
	 *
	 * @param net graph represented by nodes with connected nodes and distances
	 * @param source source node id in graph
	 * @param target target/destination node id in graph
	 * @param neighbors function to determine neighbors to a particular node
	 * @param distances function to calculate/retrieve distance between two connection nodes in graph
	 *
	 * @return Option[List[String]] traversable list of node ids in graph representing the shortest path
	 */
	def shortestPath(net: Map[String, Map[String, Double]], source: String, target: String,
					 neighbors: ((Map[String, Map[String, Double]], String) => Option[List[String]]) = neighbors,
					 distance: ((Map[String, Map[String, Double]], String, String) => Option[Double]) = distance
						): Option[GraphCase] = {
		try {
			if (source.equals(target)) {
				Some(ShortestRoute(List(target), 0.0))
			} else 	if (!net.contains(source) || !net.contains(target)) {
				Some(ShortestRouteInvalidSourceOrTarget())
			} else {
				dijkstra(net, source, target, neighbors, distance) match {
					case Some(preds) =>
						buildPath(preds, source, target) match {
							case Some(p) => Some(ShortestRoute(p, (preds(target))._2))
							case _ => Some(ShortestRouteDoesNotExist())
						}
					case _ => println("shortest route failed"); Some(ShortestRouteError())
				}
			}
		} catch {
			case e: Exception => e.printStackTrace(); None
		}
	}

	/**
	 * Calculates the shortest path given a graph, source and target nodes, and neighbor and distance functions.
	 *
	 * @param graph graph represented by nodes and edges
	 * @param source source node id in graph
	 * @param target target/destination node id in graph
	 * @param neighbors function to determine neighbors to a particular node
	 * @param distances function to calculate/retrieve distance between two connection nodes in graph
	 *
	 * @return Option[List[String]] traversable list of node ids in graph representing the shortest path
	 */
	def shortestPath(graph: Graph, source: String, target: String): Option[GraphCase] = shortestPath(graph.net, source, target)

}