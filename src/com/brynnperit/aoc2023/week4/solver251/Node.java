package com.brynnperit.aoc2023.week4.solver251;

import java.util.*;

public class Node {
    private final String name;
    private Set<Edge> edges = new HashSet<>();
    private Map<Node, Edge> connectedNodes = new HashMap<>();

    public String name() {
        return name;
    }

    public Node(String name) {
        this.name = name;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
        connectedNodes.put(edge.getOther(this), edge);
    }

    public Edge getEdge(Node connectedNode) {
        return connectedNodes.get(connectedNode);
    }

    public Collection<Node> connectedNodes() {
        return Collections.unmodifiableCollection(connectedNodes.keySet());
    }

    public Collection<Node> allConnectedNodes() {
        Set<Node> allConnectedNodes = new HashSet<>();
        allConnectedNodes.add(this);
        addConnectedNodesToSet(allConnectedNodes);
        return allConnectedNodes;
    }

    private void addConnectedNodesToSet(Set<Node> allConnectedNodes) {
        for (Node node : connectedNodes()) {
            if (!allConnectedNodes.contains(node)) {
                allConnectedNodes.add(node);
                node.addConnectedNodesToSet(allConnectedNodes);
            }
        }
    }

    public Optional<List<Set<Edge>>> getShortestDistinctPathsToOtherNode(Node destinationNode, int numberOfPaths) {
        List<Set<Edge>> shortestDistinctPaths = new ArrayList<>();
        PriorityQueue<Path> pathQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.length(), p2.length()));
        Set<Edge> forbiddenEdges = new HashSet<>();
        Set<Node> forbiddenNodes = new HashSet<>();
        while (true) {
            pathQueue.clear();
            forbiddenNodes.clear();
            Path initialPath = new Path(List.of(this), new HashSet<>(), forbiddenNodes);
            pathQueue.add(initialPath);
            Path shortestPath = getShortestPath(destinationNode, pathQueue, forbiddenEdges, forbiddenNodes);
            if (shortestPath == null) {
                break;
            } else if (shortestDistinctPaths.size() == numberOfPaths) {
                return Optional.empty();
            } else {
                shortestDistinctPaths.add(shortestPath.traversedEdges());
                forbiddenEdges.addAll(shortestPath.traversedEdges());
            }
        }
        if (shortestDistinctPaths.size() == numberOfPaths) {
            return Optional.of(shortestDistinctPaths);
        }
        return Optional.empty();
    }

    private Path getShortestPath(Node destinationNode, PriorityQueue<Path> pathQueue, Set<Edge> forbiddenEdges, Set<Node> forbiddenNodes) {
        Path shortestPath = null;
        while (!pathQueue.isEmpty()) {
            Path currentPath = pathQueue.poll();
            Node currentNode = currentPath.currentNode();
            if (currentNode.equals(destinationNode)) {
                shortestPath = currentPath;
                break;
            }
            for (Node nextNode : currentNode.connectedNodes()) {
                if (!forbiddenNodes.contains(nextNode)) {
                    Edge currentEdge = currentNode.getEdge(nextNode);
                    if (!forbiddenEdges.contains(currentEdge)) {
                        Path newPath = new Path(currentPath);
                        forbiddenNodes.add(nextNode);
                        newPath.go(nextNode);
                        newPath.traversedEdges().add(currentEdge);
                        pathQueue.add(newPath);
                    }
                }
            }
        }
        return shortestPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(':');
        for (Node connected : connectedNodes.keySet()) {
            sb.append(' ');
            sb.append(connected.name);
        }
        return sb.toString();
    }

    public Set<Edge> edges() {
        return Collections.unmodifiableSet(edges);
    }
}
