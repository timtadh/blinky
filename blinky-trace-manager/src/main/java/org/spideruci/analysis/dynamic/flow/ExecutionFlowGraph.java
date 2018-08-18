package org.spideruci.analysis.dynamic.flow;

import com.ifunsoftware.thirdparty.linloglayout.Edge;
import com.ifunsoftware.thirdparty.linloglayout.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExecutionFlowGraph implements Iterable<Edge> {
  public final List<Node> nodes;
  public final List<Edge> edges;

  public static ExecutionFlowGraph create() {
    return new ExecutionFlowGraph();
  }

  private ExecutionFlowGraph() {
    nodes = new ArrayList<>();
    edges = new ArrayList<>();
  }

  private void addNode(Node node) {
    if (nodes.contains(node)) {
      return;
    }
    this.nodes.add(node);
  }

  public void addEdge(Edge edge) {
    int index = edges.indexOf(edge);
    if (index != -1) {

      this.changeWeight(edge, edges.get(index).weight + 1);
      return;
    }

    this.addNode(edge.startNode);
    this.addNode(edge.endNode);
    this.edges.add(edge);
  }

  /**
   * If {@code edge} exists in the graph, then replace it with a new edge with the specified {@code
   * weight}.
   *
   * @param edge
   * @param weight
   * @return The new edge, if the weight got updated, or {@code null} if the original {@code edge}
   *     never existed to begin with.
   */
  public Edge changeWeight(Edge edge, double weight) {
    if (edges.contains(edge)) {
      Edge copyEdge = Edge.copyWithNewWt(edge, weight);
      edges.add(copyEdge);
      edges.remove(edge);
      return copyEdge;
    }
    return null;
  }

  @Override
  public Iterator<Edge> iterator() {
    return edges.iterator();
  }
}
