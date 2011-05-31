package org.seaton.dijkstra.cases.route

import org.seaton.dijkstra.core.Node
import org.seaton.dijkstra.cases.GraphCase

/**
 * Case class for shortest route not existing.
 */
case class ShortestRouteDoesNotExist() extends GraphCase

/**
 * Case class for an invalid source or target node id.
 */
case class ShortestRouteInvalidSourceOrTarget() extends GraphCase

/**
 * Case class wrapping the shortest route between two (2) nodes in a graph.
 */
case class ShortestRoute(route: List[String], dist: Double) extends GraphCase

/**
 * Case class for an error occurring during a shortest route calculation in a graph.
 */
case class ShortestRouteError() extends GraphCase
