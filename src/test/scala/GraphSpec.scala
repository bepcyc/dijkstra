import collection.mutable.ListBuffer
import org.seaton.dijkstra.core._
import org.seaton.dijkstra.util.GraphUtil
import org.specs.Specification
import org.seaton.dijkstra.cases._
import generate.{GeneratedGraphFailed, GeneratedGraph}
import route._

class GraphSpec extends Specification {

	/*
	Matchers applicable to Options

		* a must beNone is ok if a is None
		* a must beAsNoneAs(b) is ok if a is None when b is None or a is not None when b is None

		* a must beSome[Type] is ok if a is Some(t: Type)
		* a must beSomething is ok if a is Some(a: Any)
		* a must beSome(value) is ok if a is Some(value)

		* an Option matcher can be extended with a which condition

	  Some(x) must beSome[String].which(_.startWith("abc"))


	 */

	val triNodes = Map("a" -> Node("a", 0.0, 0.0), "b" -> Node("b", 100.0, 100.0), "c" -> Node("c", 100.0, 0.0))
	val goodTriEdges = List(Edge("a", "b"), Edge("b", "c"), Edge("c", "a"))
	val badTriEdges = Edge("d", "e") :: goodTriEdges

	val triGraph = Graph(triNodes, goodTriEdges)

	val poly5graph: Graph = GraphUtil.polygonGraph(5, 100.0, false) match {
		case Some(graphCase) =>
			graphCase match {
				case GeneratedGraph(graph) => graph
				case _ => null // anything else is an error
			}
		case _ =>
			println("Polygon graph not generated.")
			null
	}

	val wedges = new ListBuffer[Edge]()
	poly5graph.edges foreach (edge => {
		if (!GraphUtil.isEdge(edge, "0", "4") && !GraphUtil.isEdge(edge, "2", "3")) wedges += edge
	})
	val disjoint5Graph = Graph(poly5graph.nodes, wedges.toList)

	val poly10graph: Graph = GraphUtil.polygonGraph(10, 100.0, true) match {
		case Some(graphCase) =>
			graphCase match {
				case GeneratedGraph(graph) => graph
				case _ => null // anything else is an error
			}
		case _ =>
			println("Polygon graph not generated.")
			null
	}

	"graph instantiation should fail for edges with invalid node ids" in {
		Graph(triNodes, badTriEdges) must throwA[RuntimeException] // try to create graph with bad node ids in edges
	}

	"graph shortest route should fail with invalid target node id" in {
		triGraph.shortest("a", "zzz") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"graph shortest route should fail with invalid source node id" in {
		triGraph.shortest("zzz", "a") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"graph shortest route should fail with invalid source and target node ids" in {
		triGraph.shortest("yyy", "zzz") must beSome(ShortestRouteInvalidSourceOrTarget())
	}

	"polygon graph 10 sides node 0 to 4 shortest route 0>1>2>3>4" in {
		val shortest: List[String] = {
			poly10graph.shortest("0", "4") match {
				case Some(graphCase) =>
					graphCase match {
						case ShortestRoute(nodes) => for (node <- nodes.asInstanceOf[List[Node]]) yield node.id
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must haveSameElementsAs(List("0", "1", "2", "3", "4"))
	}

	"polygon graph 10 sides node 0 to 6 shortest route 0>9>8>7>6" in {
		val shortest: List[String] = {
			poly10graph.shortest("0", "6") match {
				case Some(graphCase) =>
					graphCase match {
						case ShortestRoute(nodes) => for (node <- nodes.asInstanceOf[List[Node]]) yield node.id
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must haveSameElementsAs(List("0", "9", "8", "7", "6"))
	}

	"polygon graph 10 sides source and target node being '0' should have shortest route of just node '0'" in {
		val shortest: List[String] = {
			poly10graph.shortest("0", "0") match {
				case Some(graphCase) =>
					graphCase match {
						case ShortestRoute(nodes) => for (node <- nodes.asInstanceOf[List[Node]]) yield node.id
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must haveSameElementsAs(List("0"))
	}

	"non-connected 5-sided polygon graph (missing edges between 2,3 and 0,4) shortest route between 0,2 = 0>1>2" in {
		val shortest: List[String] = {
			disjoint5Graph.shortest("0", "2") match {
				case Some(graphCase) =>
					graphCase match {
						case ShortestRoute(nodes) => for (node <- nodes.asInstanceOf[List[Node]]) yield node.id
						case ShortestRouteDoesNotExist() => fail("no shortest route")
						case ShortestRouteInvalidSourceOrTarget() => fail("invalid source/target")
						case ShortestRouteError() => fail("shortest route error")
						case _ => fail("error calculating shortest route")
					}
				case _ => fail("error calculating shortest route: unknown state")
			}
		}
		shortest must haveSameElementsAs(List("0", "1", "2"))
	}

	"non-connected 5-sided polygon graph (missing edges between 2,3 and 0,4) shortest route doesn't exist betwwen 0,3" in {
		disjoint5Graph.shortest("0", "3") must beSome(ShortestRouteDoesNotExist())
	}

	"testing all shortest routes 0..(all nodes to n-1) for each polygon graph with 3 to 40 nodes; (sumof(x) for x:3->40 = 817 tests)" in {
		(3 to 40) foreach (n => {
			val pgraph: Graph = GraphUtil.polygonGraph(n, 100.0, false) match {
				case Some(graphCase) =>
					graphCase match {
						case GeneratedGraph(graph) => graph
						case GeneratedGraphFailed(msg) => fail("""error creating polygon graph: %d sided; msg=%s""".format(n, msg))
						case _ => fail("""error creating polygon graph: %d sided""".format(n))
					}
				case _ => fail("""error creating polygon graph: %d sided""".format(n))
			}
			(0 until n) foreach (tgt => {
				val shortest: List[String] = {
					pgraph.shortest("0", String.valueOf(tgt)) match {
						case Some(graphCase) =>
							graphCase match {
								case ShortestRoute(nodes) => for (node <- nodes.asInstanceOf[List[Node]]) yield node.id
								case ShortestRouteDoesNotExist() => fail("""no shortest route: 0->%d""".format(tgt))
								case ShortestRouteInvalidSourceOrTarget() => fail("""invalid source/target: 0->%d""".format(tgt))
								case ShortestRouteError() => fail("""shortest route error: 0->%d""".format(tgt))
								case _ => fail("""error calculating shortest route: 0->%d""".format(tgt))
							}
						case _ => fail("""error calculating shortest route: unknown state: 0->%d""".format(tgt))
					}
				}
				var expectedRoute = ListBuffer("0")
				if (tgt > n / 2) {
					var ctr = n - 1
					while (ctr >= tgt) {
						expectedRoute += String.valueOf(ctr)
						ctr -= 1
					}
				} else {
					(1 to tgt) foreach (trav => expectedRoute += String.valueOf(trav))
				}
				shortest must haveSameElementsAs(expectedRoute)
			})
		})
	}

}