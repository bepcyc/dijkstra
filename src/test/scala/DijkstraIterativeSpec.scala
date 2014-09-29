import collection.mutable.ListBuffer
import org.seaton.dijkstra.core._
import org.seaton.dijkstra.util.GraphUtil
import org.seaton.dijkstra.cases._
import generate.{GeneratedGraphFailed, GeneratedGraph}
import route._
import org.specs2.mutable.Specification
import org.specs2.matcher.ThrownMessages

class DijkstraIterativeSpec extends Specification with ThrownMessages {

	val triNodes = Map("a" -> Node("a", 0.0, 0.0), "b" -> Node("b", 100.0, 100.0), "c" -> Node("c", 100.0, 0.0))
	val goodTriEdges = List(Edge[String]("a", "b"), Edge[String]("b", "c"), Edge[String]("c", "a"))

	val triGraph = Graph[String](triNodes, goodTriEdges)

	val poly5graph: Graph[String] = GraphUtil.polygonGraph(5, 100.0, false) match {
		case Some(graphCase) =>
			graphCase match {
				case generatedGraph : GeneratedGraph[String] => generatedGraph.graph
				case _ => null // anything else is an error
			}
		case _ =>
			println("Polygon graph not generated.")
			null
	}

	val wedges = new ListBuffer[Edge[String]]()
	poly5graph.edges foreach (edge => {
		if (!GraphUtil.isEdge(edge, "0", "4") && !GraphUtil.isEdge(edge, "2", "3")) wedges += edge
	})
	val disjoint5graph = Graph[String](poly5graph.nodes, wedges.toList)

	val poly10graph: Graph[String] = GraphUtil.polygonGraph(10, 100.0, true) match {
		case Some(graphCase) =>
			graphCase match {
				case generatedGraph : GeneratedGraph[String] => generatedGraph.graph
				case _ => null // anything else is an error
			}
		case _ =>
			println("Polygon graph not generated.")
			null
	}

	"iterative: graph shortest route should fail with invalid target node id" in {
		triGraph.shortestPath("a", "zzz") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"iterative: graph shortest route should fail with invalid source node id" in {
		triGraph.shortestPath("zzz", "a") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"iterative: graph shortest route should fail with invalid source and target node ids" in {
		triGraph.shortestPath("yyy", "zzz") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"iterative: polygon graph 10 sides node 0 to 4 shortest route 0>1>2>3>4" in {
		val shortest: List[String] = {
			poly10graph.shortestPath("0", "4") match {
				case Some(graphCase) =>
					graphCase match {
						case shortestRoute : ShortestRoute[String] => shortestRoute.route
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must beEqualTo(List("0", "1", "2", "3", "4"))
	}

	"iterative: polygon graph 10 sides node 0 to 6 shortest route 0>9>8>7>6" in {
		val shortest: List[String] = {
			poly10graph.shortestPath("0", "6") match {
				case Some(graphCase) =>
					graphCase match {
						case shortestRoute : ShortestRoute[String] => shortestRoute.route
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must beEqualTo(List("0", "9", "8", "7", "6"))
	}

	"iterative: polygon graph 10 sides source and target node being '0' should have shortest route of just node '0'" in {
		val shortest: List[String] = {
			poly10graph.shortestPath("0", "0") match {
				case Some(graphCase) =>
					graphCase match {
						case shortestRoute : ShortestRoute[String] => shortestRoute.route
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must beEqualTo(List("0"))
	}

	"iterative: non-connected 5-sided polygon graph (missing edges between 2,3 and 0,4) shortest route between 0,2 = 0>1>2" in {
		val shortest: List[String] = {
			disjoint5graph.shortestPath("0", "2") match {
				case Some(graphCase) =>
					graphCase match {
						case shortestRoute : ShortestRoute[String] => shortestRoute.route
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must beEqualTo(List("0", "1", "2"))
	}

	"iterative: non-connected 5-sided polygon graph (missing edges between 2,3 and 0,4) shortest route doesn't exist betwwen 0,3" in {
		disjoint5graph.shortestPath("0", "3") must beSome(ShortestRouteDoesNotExist())
	}

//	"iterative: testing all shortest routes 0..(all nodes to n-1) for each polygon graph with 3 to 100 nodes" in {
//		(3 to 100) foreach (n => {
//			val pgraph: Graph[String] = GraphUtil.polygonGraph(n, 100.0, false) match {
//				case Some(graphCase) =>
//					graphCase match {
//						case GeneratedGraph(graph) => graph
//						case GeneratedGraphFailed(msg) => fail("""error creating polygon graph: %d sided; msg=%s""".format(n, msg))
//						case _ => fail("""error creating polygon graph: %d sided""".format(n))
//					}
//				case _ => fail("""error creating polygon graph: %d sided""".format(n))
//			}
//			(0 until n) foreach (tgt => {
//				val shortest: List[String] = {
//					pgraph.shortestPath("0", String.valueOf(tgt)) match {
//						case Some(graphCase) =>
//							graphCase match {
//								case ShortestRoute(nodes, dist) => nodes
//								case ShortestRouteDoesNotExist() => fail("""no shortest route: 0->%d""".format(tgt))
//								case ShortestRouteInvalidSourceOrTarget() => fail("""invalid source/target: 0->%d""".format(tgt))
//								case ShortestRouteError() => fail("""shortest route error: 0->%d""".format(tgt))
//								case _ => fail("""error calculating shortest route: 0->%d""".format(tgt))
//							}
//						case _ => fail("""error calculating shortest route: unknown state: 0->%d""".format(tgt))
//					}
//				}
//				var expectedRoute = ListBuffer("0")
//				if (tgt > n / 2) {
//					var ctr = n - 1
//					while (ctr >= tgt) {
//						expectedRoute += String.valueOf(ctr)
//						ctr -= 1
//					}
//				} else {
//					(1 to tgt) foreach (trav => expectedRoute += String.valueOf(trav))
//				}
//				shortest must beTheSameAs(expectedRoute.toList)
//			})
//		})
//	}

}