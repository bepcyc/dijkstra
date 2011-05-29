Dijkstra's Algorithm
====================

Implementation of Dijkstra's algorithm to find the shortest (least costly) route between nodes in an undirected graph.

Notes
-----
- The code base is ready for scaladoc.  In sbt, the action 'doc' will generate <code>scaladoc</code> for the project located in the standard sbt location:
*...\<root\>/target/scala_2.9.0/doc/main/api/index.html*.
- To run a demo in sbt: sbt\> demo

Requirements
------------
- [Scala](http://www.scala-lang.org)
- [sbt](http://code.google.com/p/simple-build-tool/)
 
GraphUtil
---------

Provides utility functions and runner for the Graph class illustrating the Dijkstra algorithm of finding the shortest (least costly) route between two (2) nodes.

The <code>GraphUtil</code> object generates a regular polygon (an n-sided polygon with each side of equal length).

This is done out of convenience since the <code>Graph</code> class can contain any number of nodes and edges in any configuration and still determine the shortest route.

### Usage

> $ scala GraphUtil \[\<#-of-nodes\> \[\<source-node-id\> \[\<target-node-id\> \[\<spike-node-enabled\>\]\]\]\]

> sbt> run \[\<#-of-nodes\> \[\<source-node-id\> \[\<target-node-id\> \[\<spike-node-enabled\>\]\]\]\]

where

*Arguments:*
- *#-of-nodes* is the number of nodes generated in a regular polygon graph with edges forming the sides of regular polygon (defaults to 23)
- *source-node-id* is the node id of the starting node for calculating the shortest (least costly) route using Dijkstra's algorithm (defaults to 0)
- *target-node-id* is the node id of the ending node for calculating the shortest (least costly) route using Dijkstra's algorithm (defaults to 11)
- *spike-enabled* is a flag to add two (2) 'spike' nodes per polygon node (see below for more information) (defaults to false)

*Argument notes*
- If there are *n* number of nodes, the node ids are 0 to n-1 (e.g. "0","1","2",...,"n-1").
- The order of source and target nodes is irrelevant (other than affecting the direction of the shortest route (if any exist)).

### Regular Polygon Graphs

To exercise the algorithm and generate a exported graph image with shortest route traversal:

from command-line:

> $ scala GraphUtil 17 0 8

or in **sbt**

> sbt> run 17 0 8

Both of these commands will create a graph as a 17-sided regular polygon with edges as sides, calculate the
shortest route from node 0 to node 8, and then export the graph, with illustrated traversal, into the
<code>.../*\<root\>*/exported-graph-images/</code> folder as the file <code>graph.17.0.8.\<timestamp\>.jpg</code>.


### Regular Polygon Graphs with Spikes

An additional element to exercise the algorithm is to create 'spikes' on the regular polygon graph.  Spikes are
essentially two (2) additionally nodes associated/connected to each polygon node to create multiple terminal
nodes and also illustrate traversal that is not part of a closed graph.

Spike nodes have id's with an 'a' or 'b' appended to the polygon node id to which the spike node is associated.

For each polygon node, there is an 'a' and 'b' spike node (if spike nodes are turned on).

For example, for
node "0" with spike nodes enabled, there will also be a "0a" and "0b" node with edges connecting "0" to "0a"
and "0" to "0b", but no edge connecting "0a" and "0b" directly.

To exercise the algorithm with spikes and generate a graph image with the shortest route traversal:

from command-line:

> $ scala GraphUtil 17 0a 8b true

or in **sbt**:

> sbt> run 17 0a 8b true

Both of these commands will create a graph as a 17-sided regular polygon with edges as sides and associated spike
nodes, calculate the shortest route from node 0a to node 8b, and then export the graph, with illustrated traversal,
into the <code>.../*\<root\>*/exported-graph-images/</code> folder as the file <code>graph.17.0a.8b.\<timestamp\>.jpg</code>.

### Exported Graph Images

The <code>GraphUtil</code> object automatically exports an image representing the graph and illustrating the shortest route traversal.

The exported graph images are saved in the <code>.../*\<root\>*/exported-graph-images/</code> folder.

### References

- [Dijkstra's algorithm; iterative; pseudo-code](http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
- [Dijkstra's algorithm; functional; Haskell](http://hackage.haskell.org/packages/archive/fgl/5.4.2.2/doc/html/src/Data-Graph-Inductive-Query-SP.html)
- [Dijkstra's algorithm; functional; Lisp](http://hyperlogic.wordpress.com/2010/12/07/dijkstras-algorithm-in-lisp/)
- [Dijkstra's algorithm; functional; Clojure](http://snipplr.com/view.php?codeview&id=22183)
- [Dijkstra's Algorithm revisited: the OR/MS Connexion](http://www.ifors.ms.unimelb.edu.au/tutorial/dijkstra_new/index.html)
