package org.seaton.dijkstra.cases.generate

import org.seaton.dijkstra.core.Graph
import org.seaton.dijkstra.cases.GraphCase

/**
 * Case class wrapping a generated graph.
 */
case class GeneratedGraph[S >: Null <: AnyRef](graph: Graph[S]) extends GraphCase

/**
 * Case class for an error occurring during the generation of a graph.
 */
case class GeneratedGraphFailed(msg: String) extends GraphCase