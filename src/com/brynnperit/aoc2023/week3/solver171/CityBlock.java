package com.brynnperit.aoc2023.week3.solver171;

import java.util.*;

public class CityBlock {
    final private int cost;
    final private Map<Direction, Edge> edges = new EnumMap<>(Direction.class);

    public int cost() {
        return cost;
    }

    public CityBlock(int cost, Set<Direction> availableDirections, int maximumPathLength) {
        this.cost = cost;
        for (Direction direction : availableDirections) {
            edges.put(direction, new Edge(maximumPathLength));
        }
    }

    public Map<Direction, Edge> getEdges() {
        return edges;
    }

    public void addEdge(Direction direction, int maximumPathLength) {
        edges.put(direction, new Edge(maximumPathLength));
    }

    public void resetEdges() {
        edges.values().forEach(e->e.reset());
    }
}