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
        connectedNodes.put(edge.getOther(this).get(), edge);
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

    public Optional<List<List<Edge>>> getShortestDistinctPathsToOtherNode(Node destinationNode, int numberOfPaths) {
        List<List<Edge>> shortestDistinctPaths = new ArrayList<>();

        Set<Edge> forbiddenEdges = new HashSet<>();
        while (true) {
            Path shortestPath = getShortestPath(this, destinationNode, forbiddenEdges).orElse(null);
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

    private static Optional<Path> getShortestPath(Node startNode, Node destinationNode, Set<Edge> forbiddenEdges) {
        PriorityQueue<Path> pathQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.length(), p2.length()));
        Path initialPath = new Path(List.of(startNode), List.of());
        pathQueue.add(initialPath);
        Set<Node> forbiddenNodes = new HashSet<>();
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
        return Optional.ofNullable(shortestPath);
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

    public static Set<Edge> findBottlenecks(List<List<Edge>> edgeLists) {
        Set<Edge> bottleNecks = new HashSet<>();
        Set<Edge> lockedEdgesAll = new HashSet<>();
        for (List<Edge> list : edgeLists) {
            lockedEdgesAll.addAll(list);
        }
        for (List<Edge> list : edgeLists) {
            Set<Edge> lockedEdgesThis = new HashSet<>(lockedEdgesAll);
            lockedEdgesThis.removeAll(list);
            bottleNecks.add(findBottleneck(list, lockedEdgesThis));
        }
        return bottleNecks;
    }

    private static Edge findBottleneck(List<Edge> possibleBottlenecks, Set<Edge> lockedEdges) {
        List<Edge> remainingEdges = new ArrayList<>(possibleBottlenecks);
        Edge lastEdge = remainingEdges.remove(remainingEdges.size() - 1);
        Edge previousEdge = remainingEdges.remove(remainingEdges.size() - 1);
        Optional<Node> optDestination = lastEdge.getOther(previousEdge.firstNode());
        if (optDestination.isEmpty()){
            optDestination = lastEdge.getOther(previousEdge.secondNode());
        }
        Node destination = optDestination.get();
        Node startNode = lastEdge.getOther(destination).get();
        while (remainingEdges.size() >= 0) {
            lockedEdges.add(lastEdge);
            Optional<Path> hasPath = getShortestPath(startNode, destination, lockedEdges);
            if (hasPath.isEmpty()) {
                return lastEdge;
            }
            lockedEdges.remove(lastEdge);
            if (remainingEdges.size() > 0) {
                startNode = previousEdge.getOther(startNode).get();
                lastEdge = previousEdge;
                previousEdge = remainingEdges.remove(remainingEdges.size() - 1);
            }
        }
        throw new IllegalArgumentException();
    }
}
