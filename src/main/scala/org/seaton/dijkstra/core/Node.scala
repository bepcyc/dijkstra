package org.seaton.dijkstra.core

/**
 * Represents a node in a graph with Cartesian coordinates (x,y) and a string id.
 *
 * @param id node id
 * @param x X-coordinate of node
 * @param y Y-coordinate of node
 */
class Node[S >: Null <: AnyRef](val id: S, val x: Double, val y: Double) {
	/**
	 * Lazy string representation of Node.
	 */
	lazy val str = id.toString + ": x=" + x + "; y=" + y
	/**
	 * Overridden toString() default string representation of Node.
	 * @return lazy str representation
	 */
	override def toString: String = str
}

/**
 * Companion object to Node class with simple factory apply().
 */
object Node {
	/**
	 * Factory for Node class.
	 *
	 * @param id node id
	 * @param x X-coordinate of node
	 * @param y Y-coordinate of node
	 */
	def apply[S >: Null <: AnyRef](id: S, x: Double, y: Double) = new Node(id, x, y)
}