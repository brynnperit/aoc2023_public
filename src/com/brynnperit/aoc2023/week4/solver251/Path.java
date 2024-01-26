package com.brynnperit.aoc2023.week4.solver251;

import java.util.*;

public record Path(List<Node> visitedNodes, HashSet<Edge> traversedEdges,Set<Node> forbiddenNodes) {

    public Path(Path other){
        this(new ArrayList<>(other.visitedNodes), new HashSet<>(other.traversedEdges),other.forbiddenNodes);
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
