package com.brynnperit.aoc2023.week4.solver231;

import java.util.*;

public class NodeExplorer {
    private Set<PathNode> visitedNodes = new TreeSet<>();
    private List<PathNode> orderedVisitedNodes = new ArrayList<>();
    private PathNode currentNode;
    private long lengthTravelled = 0L;

    public long lengthTravelled() {
        return lengthTravelled;
    }

    public NodeExplorer(PathNode startNode){
        this.currentNode = startNode;
        visitedNodes.add(startNode);
        orderedVisitedNodes.add(startNode);
    }

    public NodeExplorer(NodeExplorer other){
        this.currentNode = other.currentNode;
        this.visitedNodes.addAll(other.visitedNodes);
        this.orderedVisitedNodes.addAll(other.orderedVisitedNodes);
        this.lengthTravelled = other.lengthTravelled;
    }

    void visit(PathNode node){
        lengthTravelled+=currentNode.getDistanceTo(node);
        visitedNodes.add(node);
        orderedVisitedNodes.add(node);
        currentNode = node;
    }

    boolean hasVisited(PathNode node){
        return visitedNodes.contains(node);
    }

    public PathNode currentNode() {
        return currentNode;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Length travelled:%d, [",lengthTravelled));
        for (PathNode visitedNode : orderedVisitedNodes){
            sb.append(String.format("%s,",visitedNode.position()));
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(']');
        return sb.toString();
    }
}
