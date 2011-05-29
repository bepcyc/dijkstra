package org.seaton.dijkstra.core

/**
 * Represents an edge in a graph by holding two (2) node ids.
 *
 * @author Greg Seaton (seatongs@gmail.com)
 * @param nodeA id for the first node of the edge
 * @param nodeB id for the second node of the edge
 */
class Edge(val nodeA: String, val nodeB: String) {

	/**
	 * Lazy string representation of the edge; will not be instantiated until called.
	 */
	lazy val str = nodeA + " <-> " + nodeB

	/**
	 * Overridden method to return default string representation.
	 * @return lazy val str.
	 */
	override def toString(): String = str
}

/**
 * Companion object to Edge class with simple factory apply().
 */
object Edge {
	/**
	 * Factory for Edge class.
	 * @param nodeA id for the first node of the edge
	 * @param nodeB id for the second node of the edge
	 */
	def apply(nodeA: String, nodeB: String) = new Edge(nodeA, nodeB)
}