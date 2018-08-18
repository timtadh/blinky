package org.spideruci.analysis.dynamic.flow;

import com.ifunsoftware.thirdparty.linloglayout.Edge;
import com.ifunsoftware.thirdparty.linloglayout.LinLogLayout;
import com.ifunsoftware.thirdparty.linloglayout.Node;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.spideruci.analysis.trace.EventType;
import org.spideruci.analysis.trace.TraceEvent;
import org.spideruci.analysis.trace.io.TraceScanner;

public class DynamicCfgBuilder {

  public static void main(String[] args) {
    ExecutionFlowGraph dynCfg = buildFromTrace(args[0]);

    for (Node node : dynCfg.nodes) {
      System.out.println(node);
    }
    for (Edge edge : dynCfg) {
      System.out.println(edge);
    }

    //    System.exit(0);

  }

  public static ExecutionFlowGraph buildFromTrace(String filePath) {
    File file = new File(filePath);
    return buildFromTrace(file);
  }

  public static ExecutionFlowGraph buildFromTrace(File file) {
    if (file == null) {
      throw new RuntimeException("File is null.");
    }

    TraceScanner scanner = new TraceScanner(file);
    Node previousNode = null;
    String previousRawNode = null;
    Map<String, Map<String, Double>> rawGraph = new HashMap<>();

    for (TraceEvent event : scanner) {
      // System.out.println(event);
      EventType type = event.getType();
      if (type.isDecl()) {
        System.out.println(
            String.format(
                "declaring id:%d, class:%s, name:%s",
                event.getId(), event.getDeclOwner(), event.getDeclName()));
      } else if (type.isInsn()) {
        int id = event.getId();
        int hostId = event.getInsnDeclHostId(); // eg. the method id
        int line = event.getInsnLine();
        System.out.println(String.format("node id:%d, in-method:%d, line:%d", id, hostId, line));
      } else if (type.isExec()) {
        String rawNode = event.getExecInsnEventId();
        //        Node node = new Node(event.getExecInsnId(), 0);
        if (previousRawNode != null) {
          if (!rawGraph.containsKey(previousRawNode)) {
            rawGraph.put(previousRawNode, new HashMap<String, Double>());
          }

          Map<String, Double> destRawNodes = rawGraph.get(previousRawNode);
          if (destRawNodes == null) {
            continue;
          }

          if (destRawNodes.containsKey(rawNode)) {
            double edgeWt = destRawNodes.get(rawNode);
            destRawNodes.put(rawNode, edgeWt + 1);
          } else {
            destRawNodes.put(rawNode, 1.0);
          }
        }
        previousRawNode = rawNode;
      }
    }

    rawGraph = LinLogLayout.makeSymmetricGraph(rawGraph);

    ExecutionFlowGraph efg = ExecutionFlowGraph.create();
    for (String from : rawGraph.keySet()) {
      Map<String, Double> outgoingNodes = rawGraph.get(from);
      if (from == null || outgoingNodes == null) {
        continue;
      }

      Node fromNode = new Node(from, 0);

      for (String to : outgoingNodes.keySet()) {
        Double weight = outgoingNodes.get(to);
        if (to == null || weight == null) {
          continue;
        }

        Node toNode = new Node(to, 0);
        Edge edge = new Edge(fromNode, toNode, weight);
        efg.addEdge(edge);
      }
    }

    return efg;
  }
}
