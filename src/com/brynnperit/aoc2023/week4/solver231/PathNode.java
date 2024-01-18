package com.brynnperit.aoc2023.week4.solver231;

import java.util.*;

public class PathNode implements Comparable<PathNode>{
    final private Set<PathNode> connectedNodes = new TreeSet<>();
    final private Map<PathNode, Long> edges = new TreeMap<>();
    final private Coord2D position;

    public Coord2D position(){
        return new Coord2D(position);
    }

    public Set<PathNode> getConnectedNodes() {
        return Collections.unmodifiableSet(connectedNodes);
    }

    public long getDistanceTo(PathNode destinationNode){
        return edges.get(destinationNode);
    }

    public PathNode(Coord2D position) {
        this.position = new Coord2D(position);
    }

    @Override
    public int compareTo(PathNode other) {
        return position.compareTo(other.position);
    }

    public void addNode(PathNode destinationNode, long forkDistance) {
        connectedNodes.add(destinationNode);
        edges.put(destinationNode, forkDistance);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Node position:%s, connected to:[",position));
        for (PathNode visitedNode : connectedNodes){
            sb.append(String.format("%s:%d,",visitedNode.position(),edges.get(visitedNode)));
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(']');
        return sb.toString();
    }
}
