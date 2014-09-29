package org.seaton.dijkstra.core

/**
 * Represents an edge in a graph by holding two (2) node ids.
 *
 * @author Greg Seaton (seatongs@gmail.com)
 * @param nodeA id for the first node of the edge
 * @param nodeB id for the second node of the edge
 */
class Edge[S >: Null <: AnyRef](val nodeA: S, val nodeB: S) {

	/**
	 * Lazy string representation of the edge; will not be instantiated until called.
	 */
	lazy val str = nodeA.toString + " <-> " + nodeB.toString

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
	def apply[S >: Null <: AnyRef](nodeA: S, nodeB: S) = new Edge(nodeA, nodeB)
}