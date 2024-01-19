package com.brynnperit.aoc2023.week3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver172 {
    private static final int MIN_TRAVEL = 4;
    private static final int MAX_TRAVEL = 10;

    private static class Coord2D implements Comparable<Coord2D> {
        private int x;
        private int y;

        public Coord2D(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coord2D(Coord2D other) {
            this.x = other.x;
            this.y = other.y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public void go(Direction toGo) {
            x = toGo.goX(x);
            y = toGo.goY(y);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Coord2D))
                return false;
            Coord2D other = (Coord2D) o;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(x);
            result = 31 * result + Integer.hashCode(y);
            return result;
        }

        @Override
        public int compareTo(Coord2D other) {
            int result = Integer.compare(x, other.x);
            if (result == 0) {
                result = Integer.compare(y, other.y);
            }
            return result;
        }

        public int getOrthogonalDistance(Coord2D other) {
            return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
        }
    }

    public enum Direction {
        NORTH(0, -1, '^'),
        EAST(1, 0, '>'),
        SOUTH(0, 1, 'v'),
        WEST(-1, 0, '<');

        final private int x;
        final private int y;
        final private char symbol;

        Direction(int x, int y, char symbol) {
            this.x = x;
            this.y = y;
            this.symbol = symbol;
        }

        private static final Map<Character, Direction> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Direction> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        public char symbol() {
            return symbol;
        }

        public static Direction getDirection(Coord2D start, Coord2D end) {
            int xDifference = end.x() - start.x();
            int yDifference = end.y() - start.y();
            if (xDifference > 0) {
                return Direction.EAST;
            } else if (xDifference < 0) {
                return Direction.WEST;
            } else if (yDifference < 0) {
                return Direction.NORTH;
            }
            return Direction.SOUTH;
        }

        public Direction next() {
            return Direction.values()[(this.ordinal() + 1) % 4];
        }

        public Direction previous() {
            return Direction.values()[(this.ordinal() + 3) % 4];
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int goX(int x) {
            return x + this.x;
        }

        public int goY(int y) {
            return y + this.y;
        }

        public Coord2D asCoord() {
            return new Coord2D(x, y);
        }

        public Direction opposite() {
            return Direction.values()[(this.ordinal() + 2) % 4];
        }
    }

    private static class Edge {
        private int[] lowestPathThrough;

        public Edge() {
            lowestPathThrough = new int[(MAX_TRAVEL + 1) - MIN_TRAVEL];
            for (int x = 0; x < lowestPathThrough.length; x++) {
                lowestPathThrough[x] = Integer.MAX_VALUE;
            }
        }

        public int lowestPathThrough(int consecutiveDirections) {
            return lowestPathThrough[consecutiveDirections - MIN_TRAVEL];
        }

        public void setLowestPathThrough(int consecutiveDirections, int elapsedCost) {
            lowestPathThrough[consecutiveDirections - MIN_TRAVEL] = elapsedCost;
            // for (int index = lowestPathThrough.length - 1; index >= consecutiveDirections
            // - MIN_TRAVEL; index--) {
            // lowestPathThrough[index] = Math.min(lowestPathThrough[index], elapsedCost);
            // }
        }
    }

    private static class CityBlock {
        final private int cost;
        final private Map<Direction, Edge> edges = new EnumMap<>(Direction.class);

        public int cost() {
            return cost;
        }

        public CityBlock(int cost, Set<Direction> availableDirections) {
            this.cost = cost;
            for (Direction direction : availableDirections) {
                edges.put(direction, new Edge());
            }
        }

        public Map<Direction, Edge> getEdges() {
            return edges;
        }

        public void addEdge(Direction direction) {
            edges.put(direction, new Edge());
        }
    }

    private static class City {
        List<List<CityBlock>> grid = new ArrayList<>();

        public void addRow(String rowString) {
            List<CityBlock> row = new ArrayList<>();
            int rowIndex = grid.size();
            int columnIndex = 0;
            for (char c : rowString.toCharArray()) {
                Set<Direction> availableEdges = EnumSet.noneOf(Direction.class);
                if (rowIndex != 0) {
                    availableEdges.add(Direction.NORTH);
                    grid.get(rowIndex - 1).get(columnIndex).addEdge(Direction.SOUTH);
                }
                if (columnIndex != 0) {
                    availableEdges.add(Direction.WEST);
                    row.get(columnIndex - 1).addEdge(Direction.EAST);
                }
                row.add(new CityBlock(c - '0', availableEdges));
                columnIndex++;
            }
            grid.add(row);
        }

        public CityBlock getCityBlock(Coord2D position) {
            return grid.get(position.y()).get(position.x());
        }

        public Path getShortestPath(Coord2D startLocation, Coord2D endLocation) {
            // As mentioned in the problem description there is no cost for the start
            // location.
            Path startPath = new Path(startLocation, endLocation, this);
            Queue<Path> pathQueue = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.totalCost(), p2.totalCost()));
            pathQueue.addAll(startPath.getNextPathsNoChecks());
            Path currentPath = null;
            Path bestPath = null;
            int cutoffCost = Integer.MAX_VALUE;
            int pathsConsidered = 0;
            int highestQueueSize = 0;
            while (!pathQueue.isEmpty()) {
                highestQueueSize = Math.max(highestQueueSize, pathQueue.size());
                currentPath = pathQueue.poll();
                pathsConsidered++;
                // System.out.println(currentPath);
                if (pathsConsidered % 1000 == 0) {
                    // System.out.printf("Paths considered: %d, queue size:%d,cutoff:%d%n",
                    //         pathsConsidered, pathQueue.size(), cutoffCost);
                }
                // try {
                // Thread.sleep(40);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                if (currentPath.elapsedCost() < cutoffCost && currentPath.isValid()) {
                    if (currentPath.position().equals(endLocation)
                            && currentPath.consecutiveDirections() >= MIN_TRAVEL) {
                        bestPath = currentPath;
                        cutoffCost = currentPath.elapsedCost();
                        // System.out.println(currentPath);
                        System.out.printf("Paths considered: %d, queue size:%d,cutoff:%d%n", pathsConsidered,
                                pathQueue.size(), cutoffCost);
                    } else {
                        List<Path> newPaths = currentPath.getNextPaths();
                        pathQueue.addAll(newPaths);
                        // System.out.printf("Added %d paths%n", newPaths.size());
                    }
                } else {
                    // System.out.printf("Terminated %d>%d%n",currentPath.elapsedCost,cutoffCost);
                }
            }
            // System.out.println(currentPath);
            // System.out.println(bestPath);
            System.out.printf("Paths considered: %d%n", pathsConsidered);
            System.out.printf("Highest queue size: %d%n", highestQueueSize);
            return bestPath;
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

    private static class Path {
        final private Path previousPath;
        final private Direction goingTo;
        final private int consecutiveDirections;
        final private int elapsedCost;
        final private int totalCost;
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

        public Path(Path previousPath, Direction goingTo, int consecutiveDirections, City city, Edge edge) {
            this.previousPath = previousPath;
            this.goingTo = goingTo;
            this.edge = edge;
            this.consecutiveDirections = consecutiveDirections;
            this.city = city;
            this.position = new Coord2D(previousPath.position);
            this.position.go(goingTo);
            this.destination = previousPath.destination;
            this.cityBlock = city.getCityBlock(position);
            this.elapsedCost = previousPath.elapsedCost + cityBlock.cost;
            this.totalCost = elapsedCost + position.getOrthogonalDistance(destination);
        }

        public Path(Coord2D start, Coord2D destination, City city) {
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
            if (consecutiveDirections >= MIN_TRAVEL && edge != null) {
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
                if (sameDirection && consecutiveDirections < MIN_TRAVEL) {
                    nextPaths.add(new Path(this, direction, consecutiveDirections + 1, city, edge));
                } else if (sameDirection && consecutiveDirections < MAX_TRAVEL) {
                    if (elapsedCost < edge.lowestPathThrough(consecutiveDirections + 1)) {
                        nextPaths.add(new Path(this, direction, consecutiveDirections + 1, city, edge));
                    }
                } else if (!sameDirection && !goingBackwards && consecutiveDirections >= MIN_TRAVEL) {
                    //if (elapsedCost < edge.lowestPathThrough(MIN_TRAVEL)) {
                        nextPaths.add(new Path(this, direction, 1, city, edge));
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
                nextPaths.add(new Path(this, direction, 1, city, edge));
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
                    rowStringBuilders.get(position.y).setCharAt(position.x, currentDirection.symbol());
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

    public static void main(String[] args) {
        long lowestElapsedCost = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week3/input_17").toPath())) {
            City city = new City();
            inputLines.forEachOrdered(line -> city.addRow(line));
            Path shortestPath = city.getShortestPath(new Coord2D(0, 0),
                    new Coord2D(city.columns() - 1, city.rows() - 1));
            lowestElapsedCost = shortestPath.elapsedCost();
            // city.printGridEdgeCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Lowest elapsed cost is: %d%n", lowestElapsedCost);
    }
}
