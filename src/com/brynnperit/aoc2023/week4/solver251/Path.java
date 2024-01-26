package com.brynnperit.aoc2023.week4.solver251;

import java.util.*;

public record Path(List<Node> visitedNodes, List<Edge> traversedEdges) {

    public Path(Path other){
        this(new ArrayList<>(other.visitedNodes), new ArrayList<>(other.traversedEdges));
    }

    public Node currentNode(){
        return visitedNodes.get(visitedNodes.size()-1);
    }

    public int length(){
        return visitedNodes.size();
    }

    public void go(Node toGo){
        visitedNodes.add(toGo);
    }
}
