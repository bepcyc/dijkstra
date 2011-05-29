import java.lang.String
import javax.print.event.PrintEvent
import org.seaton.dijkstra.core.Graph
import org.seaton.dijkstra.util.GraphUtil
import org.specs.Specification
import org.seaton.dijkstra.cases._
import generate.{GeneratedGraphFailed, GeneratedGraph}

class GraphUtilSpec extends Specification {

	"generated polygon graph n = 3 - 40 sides must have n nodes and n edges and each node should have edges to two (2) neighboring nodes only" in {
		(3 to 40) foreach (n => {
			val graph: Graph = GraphUtil.polygonGraph(n, 100.0, false) match {
				case Some(graphCase) =>
					graphCase match {
						case GeneratedGraph(g) => g
						case GeneratedGraphFailed(msg) => fail("""error creating polygon graph: %d sided; msg=%s""".format(n, msg))
						case _ => fail("""error creating polygon graph: %d sided""".format(n))
					}
				case _ => fail("""error creating polygon graph: %d sided""".format(n))
			}
			graph.nodes must haveSize(n)
			graph.edges must haveSize(n)
			graph.net foreach (nd => {
				val index = Integer.parseInt(nd._1)
				val prev = if (index == 0) (n - 1) else (index - 1)
				val next = if (index + 1 >= n) 0 else (index + 1)
				val expectedEdgeNodeIds = List(String.valueOf(prev), String.valueOf(next))
				val actualEdgeNodeIds = for (d <- nd._2) yield d._1
				actualEdgeNodeIds must haveTheSameElementsAs(expectedEdgeNodeIds)
			})
		})
	}

}