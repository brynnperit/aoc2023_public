package com.brynnperit.aoc2023.week3.solver171;

import java.util.*;

public class City {
    private final int maximumPathLength;
    List<List<CityBlock>> grid = new ArrayList<>();

    public City(int maximumPathLength){
        this.maximumPathLength = maximumPathLength;
    }

    public void addRow(String rowString) {
        List<CityBlock> row = new ArrayList<>();
        int rowIndex = grid.size();
        int columnIndex = 0;
        for (char c : rowString.toCharArray()) {
            Set<Direction> availableEdges = EnumSet.noneOf(Direction.class);
            if (rowIndex != 0) {
                availableEdges.add(Direction.NORTH);
                grid.get(rowIndex - 1).get(columnIndex).addEdge(Direction.SOUTH,maximumPathLength);
            }
            if (columnIndex != 0) {
                availableEdges.add(Direction.WEST);
                row.get(columnIndex - 1).addEdge(Direction.EAST,maximumPathLength);
            }
            row.add(new CityBlock(c - '0', availableEdges,maximumPathLength));
            columnIndex++;
        }
        grid.add(row);
    }

    public CityBlock getCityBlock(Coord2D position) {
        return grid.get(position.y()).get(position.x());
    }

    public Path getShortestPath(Coord2D startLocation, Coord2D endLocation, int minTravel, int maxTravel) {
        resetEdges();
        // As mentioned in the problem description there is no cost for the start
        // location.
        Path startPath = new Path(startLocation, endLocation, this,minTravel,maxTravel);
        Queue<Path> pathQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.totalCost(), p2.totalCost()));
        pathQueue.addAll(startPath.getNextPathsNoChecks());
        Path currentPath = null;
        Path bestPath = null;
        int cutoffCost = Integer.MAX_VALUE;
        int highestQueueSize = 0;
        while (!pathQueue.isEmpty()) {
            highestQueueSize = Math.max(highestQueueSize, pathQueue.size());
            currentPath = pathQueue.poll();
            if (currentPath.elapsedCost() < cutoffCost && currentPath.isValid()) {
                if (currentPath.position().equals(endLocation)
                        && currentPath.consecutiveDirections() >= minTravel) {
                    bestPath = currentPath;
                    cutoffCost = currentPath.elapsedCost();
                } else {
                    List<Path> newPaths = currentPath.getNextPaths();
                    pathQueue.addAll(newPaths);
                }
            }
        }
        return bestPath;
    }

    private void resetEdges() {
        grid.forEach(r->r.forEach(g->g.resetEdges()));
    }

    @Override
    public String toString() {
        StringBuilder combinedStringBuilder = new StringBuilder();
        getRowStringBuilders().forEach(sb -> combinedStringBuilder.append(sb));
        return combinedStringBuilder.toString();
    }

    public List<StringBuilder> getRowStringBuilders() {
        List<StringBuilder> rowStrings = new ArrayList<>();
        for (List<CityBlock> row : grid) {
            StringBuilder rowBuilder = new StringBuilder();
            for (CityBlock block : row) {
                rowBuilder.append(block.cost());
            }
            rowBuilder.append(String.format("%n"));
            rowStrings.add(rowBuilder);
        }
        return rowStrings;
    }

    public int columns() {
        return grid.get(0).size();
    }

    public int rows() {
        return grid.size();
    }

}