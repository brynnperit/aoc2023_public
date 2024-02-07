package com.brynnperit.aoc2023.week3.solver171;

import java.util.*;

public class Edge {
    private int[] lowestPathThrough;

    public Edge(int capacity) {
        lowestPathThrough = new int[capacity];
        reset();
    }

    public void reset() {
        Arrays.fill(lowestPathThrough,Integer.MAX_VALUE);
    }

    public int lowestPathThrough(int consecutiveDirections) {
        return lowestPathThrough[consecutiveDirections-1];
    }

    public void setLowestPathThrough(int consecutiveDirections, int elapsedCost) {
        lowestPathThrough[consecutiveDirections-1] = elapsedCost;
    }
}