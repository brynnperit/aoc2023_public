package com.brynnperit.aoc2023.week4.solver251;

import java.util.*;
import java.util.regex.*;

public class Graph {
    final private static Pattern nodePattern = Pattern.compile("[a-z]+");
    private NavigableMap<String, Node> nodesByString = new TreeMap<>();
    private Set<Edge> edges = new HashSet<>();

    public Graph() {
    }

    public Graph(Graph other, Set<Edge> bannedEdges) {
        for (Node node : other.nodesByString.values()) {
            nodesByString.put(node.name(), new Node(node.name()));
        }
        Set<Edge> ourBannedEdges = new HashSet<>();
        for (Edge edge : bannedEdges) {
            ourBannedEdges.add(new Edge(nodesByString.get(edge.firstNode().name()), nodesByString.get(edge.secondNode().name())));
        }
        for (Node otherNode : other.nodesByString.values()) {
            for (Edge edge : otherNode.edges()) {
                Edge ourEdge = new Edge(nodesByString.get(edge.firstNode().name()), nodesByString.get(edge.secondNode().name()));
                if (!ourBannedEdges.contains(ourEdge)) {
                    nodesByString.get(otherNode.name()).addEdge(ourEdge);
                    edges.add(ourEdge);
                }
            }
        }
    }

    public void removeAll(Collection<Node> toRemove) {
        for (Node node : toRemove) {
            edges.removeAll(nodesByString.get(node.name()).edges());
            nodesByString.remove(node.name());
        }
    }

    public void add(String line) {
        Matcher nodeMatcher = nodePattern.matcher(line);
        nodeMatcher.find();
        String nodeToAddToString = nodeMatcher.group();
        Node nodeToAddTo = nodesByString.computeIfAbsent(nodeToAddToString, s -> new Node(s));
        while (nodeMatcher.find()) {
            String linkedNodeString = nodeMatcher.group();
            Node linkedNode = nodesByString.computeIfAbsent(linkedNodeString, s -> new Node(s));
            Edge newEdge = new Edge(nodeToAddTo, linkedNode);
            edges.add(newEdge);
            nodeToAddTo.addEdge(newEdge);
            linkedNode.addEdge(newEdge);
        }
    }

    public List<Graph> findTwoDistinctGroupsByEdgeRemoval(int edgesToRemove) {
        List<Node> nodesWithEnoughEdges = nodesByString.values().stream().filter(n -> n.connectedNodes().size() > edgesToRemove).toList();
        ListIterator<Node> firstNodeIterator = nodesWithEnoughEdges.listIterator();
        Set<Edge> commonEdges = null;
        while (firstNodeIterator.hasNext() && commonEdges == null) {
            Node firstNode = firstNodeIterator.next();
            if (firstNodeIterator.hasNext()) {
                ListIterator<Node> secondNodeIterator = nodesWithEnoughEdges.listIterator(firstNodeIterator.nextIndex());
                while (secondNodeIterator.hasNext()) {
                    Node secondNode = secondNodeIterator.next();
                    Optional<List<List<Edge>>> paths = firstNode.getShortestDistinctPathsToOtherNode(secondNode, edgesToRemove);
                    if (paths.isPresent()){
                        commonEdges = Node.findBottlenecks(paths.get());
                        break;
                    }
                }
            }
        }
        Graph firstGraph = new Graph(this, commonEdges);
        Node firstRemovalNode = firstGraph.nodesByString.firstEntry().getValue();
        Collection<Node> firstConnectedNodes = firstRemovalNode.allConnectedNodes();
        firstGraph.removeAll(firstConnectedNodes);
        Graph secondGraph = new Graph(this, commonEdges);
        Node secondRemovalNode = secondGraph.nodesByString.get(firstGraph.nodesByString.firstKey());
        Collection<Node> secondConnectedNodes = secondRemovalNode.allConnectedNodes();
        secondGraph.removeAll(secondConnectedNodes);
        return List.of(firstGraph, secondGraph);
    }

    public int nodeCount() {
        return nodesByString.size();
    }

}
