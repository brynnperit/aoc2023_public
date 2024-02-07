package com.brynnperit.aoc2023.week3.solver171;

import java.util.*;

public class Path {
    final private Path previousPath;
    final private Direction goingTo;
    final private int consecutiveDirections;
    final private int elapsedCost;
    final private int totalCost;
    final private int minTravel;
    final private int maxTravel;
    final private Coord2D position;
    final private Coord2D destination;
    final private CityBlock cityBlock;
    final private City city;
    final private Edge edge;

    public Coord2D position() {
        return position;
    }

    public int consecutiveDirections() {
        return consecutiveDirections;
    }

    public int totalCost() {
        return totalCost;
    }

    public int elapsedCost() {
        return elapsedCost;
    }

    public Path(Path previousPath, Direction goingTo, int consecutiveDirections, City city, Edge edge, int minTravel, int maxTravel) {
        this.minTravel = minTravel;
        this.maxTravel = maxTravel;
        this.previousPath = previousPath;
        this.goingTo = goingTo;
        this.edge = edge;
        this.consecutiveDirections = consecutiveDirections;
        this.city = city;
        this.position = new Coord2D(previousPath.position);
        this.position.go(goingTo);
        this.destination = previousPath.destination;
        this.cityBlock = city.getCityBlock(position);
        this.elapsedCost = previousPath.elapsedCost + cityBlock.cost();
        this.totalCost = elapsedCost + position.getOrthogonalDistance(destination);
    }

    public Path(Coord2D start, Coord2D destination, City city, int minTravel, int maxTravel) {
        this.minTravel = minTravel;
        this.maxTravel = maxTravel;
        this.edge = null;
        this.previousPath = null;
        this.goingTo = null;
        this.consecutiveDirections = 1;
        this.city = city;
        this.position = start;
        this.destination = destination;
        this.cityBlock = city.getCityBlock(position);
        this.elapsedCost = 0;
        this.totalCost = elapsedCost + position.getOrthogonalDistance(destination);
    }

    public boolean isValid() {
        boolean isValid = true;
        if (consecutiveDirections >= minTravel && edge != null) {
            int lowestCostSoFar = edge.lowestPathThrough(consecutiveDirections);
            if (elapsedCost >= lowestCostSoFar) {
                isValid = false;
            } else {
                edge.setLowestPathThrough(consecutiveDirections, elapsedCost);
            }
        }
        return isValid;
    }

    public List<Path> getNextPaths() {
        List<Path> nextPaths = new ArrayList<>(3);
        Map<Direction, Edge> edges = cityBlock.getEdges();
        for (Direction direction : edges.keySet()) {
            Edge edge = edges.get(direction);
            boolean sameDirection = direction == goingTo;
            boolean goingBackwards = direction == goingTo.opposite();
            if (sameDirection && consecutiveDirections < minTravel) {
                nextPaths.add(new Path(this, direction, consecutiveDirections + 1, city, edge,minTravel,maxTravel));
            } else if (sameDirection && consecutiveDirections < maxTravel) {
                if (elapsedCost < edge.lowestPathThrough(consecutiveDirections + 1)) {
                    nextPaths.add(new Path(this, direction, consecutiveDirections + 1, city, edge,minTravel,maxTravel));
                }
            } else if (!sameDirection && !goingBackwards && consecutiveDirections >= minTravel) {
                //if (elapsedCost < edge.lowestPathThrough(MIN_TRAVEL)) {
                    nextPaths.add(new Path(this, direction, 1, city, edge,minTravel,maxTravel));
                //}
            }
        }
        return nextPaths;
    }

    public List<Path> getNextPathsNoChecks() {
        List<Path> nextPaths = new ArrayList<>(3);
        Map<Direction, Edge> edges = cityBlock.getEdges();
        for (Direction direction : edges.keySet()) {
            Edge edge = edges.get(direction);
            nextPaths.add(new Path(this, direction, 1, city, edge,minTravel,maxTravel));
        }
        return nextPaths;
    }

    @Override
    public String toString() {
        List<StringBuilder> rowStringBuilders = city.getRowStringBuilders();
        Path currentPath = this;
        while (currentPath != null) {
            Direction currentDirection = currentPath.goingTo;
            if (currentDirection != null) {
                Coord2D position = currentPath.position;
                rowStringBuilders.get(position.y()).setCharAt(position.x(), currentDirection.symbol());
            }
            currentPath = currentPath.previousPath;
        }
        StringBuilder combinedStringBuilder = new StringBuilder();
        rowStringBuilders.forEach(sb -> combinedStringBuilder.append(sb));
        combinedStringBuilder
                .append(String.format("T:%d,E:%d,C:%d%n", totalCost, elapsedCost, consecutiveDirections));
        return combinedStringBuilder.toString();
    }
}