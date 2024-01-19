package com.brynnperit.aoc2023.week3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;
import java.util.regex.*;

public class solver181 {
    private static Pattern edgeDirectionPattern = Pattern.compile("[UDLR]");
    private static Pattern edgeLengthPattern = Pattern.compile("[ULDR] ([0-9]+)");
    private static Pattern colourPattern = Pattern.compile("\\(#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})");

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

        public void addX(int x) {
            this.x += x;
        }

        public void addY(int y) {
            this.y += y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isInBounds(int maxX, int maxY) {
            return x >= 0 && x < maxX && y >= 0 && y < maxY;
        }
    }

    public enum Direction {
        UP(0, -1, 'U'),
        RIGHT(1, 0, 'R'),
        DOWN(0, 1, 'D'),
        LEFT(-1, 0, 'L');

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
                return Direction.RIGHT;
            } else if (xDifference < 0) {
                return Direction.LEFT;
            } else if (yDifference < 0) {
                return Direction.UP;
            }
            return Direction.DOWN;
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

    public static class EdgeCollection {
        private List<Edge> edges = new ArrayList<>();
        private Coord2D endCoord = new Coord2D(0, 0);
        private Coord2D lowestCoord = new Coord2D(0, 0);
        private Coord2D highestCoord = new Coord2D(0, 0);

        public List<Edge> edges() {
            return edges;
        }

        public int deltaY() {
            return highestCoord.y() - lowestCoord.y();
        }

        public int deltaX() {
            return highestCoord.x() - lowestCoord.x();
        }

        public int offsetX() {
            return lowestCoord.x();
        }

        public int offsetY() {
            return lowestCoord.y();
        }

        public void addEdge(String line) {
            Matcher edgeDirectionMatcher = edgeDirectionPattern.matcher(line);
            Matcher edgeLengthMatcher = edgeLengthPattern.matcher(line);
            Matcher colourMatcher = colourPattern.matcher(line);
            edgeDirectionMatcher.find();
            Direction direction = Direction.fromSymbol(edgeDirectionMatcher.group().charAt(0)).get();
            edgeLengthMatcher.find();
            int length = Integer.parseInt(edgeLengthMatcher.group(1));
            colourMatcher.find();
            Colour colour = new Colour(Integer.parseInt(colourMatcher.group(1), 16),
                    Integer.parseInt(colourMatcher.group(2), 16), Integer.parseInt(colourMatcher.group(3), 16));
            Edge newEdge = new Edge(colour, direction, length);
            endCoord.addX(direction.x() * length);
            endCoord.addY(direction.y() * length);
            lowestCoord.setX(Math.min(lowestCoord.x(), endCoord.x()));
            lowestCoord.setY(Math.min(lowestCoord.y(), endCoord.y()));
            highestCoord.setX(Math.max(highestCoord.x(), endCoord.x()));
            highestCoord.setY(Math.max(highestCoord.y(), endCoord.y()));
            edges.add(newEdge);
        }

    }

    public static class Edge {
        private Colour colour;
        private Direction direction;
        private final int length;

        public Edge(Colour colour, Direction direction, int length) {
            this.colour = colour;
            this.direction = direction;
            this.length = length;
        }

        public Colour colour() {
            return colour;
        }

        public Direction direction() {
            return direction;
        }

        public int length() {
            return length;
        }
    }

    public static class LagoonGrid {
        private List<List<LagoonTile>> grid = new ArrayList<>();
        private EdgeCollection edges;
        int rowCount;
        int colCount;
        int dugCount = 0;

        public LagoonGrid(EdgeCollection edges) {
            this.edges = edges;
            for (int rowIndex = -1; rowIndex <= edges.deltaY() + 1; rowIndex++) {
                List<LagoonTile> row = new ArrayList<>();
                grid.add(row);
                rowCount++;
                for (int colIndex = -1; colIndex <= edges.deltaX() + 1; colIndex++) {
                    row.add(null);
                }
            }
            colCount = grid.get(0).size();
            Coord2D drawPosition = new Coord2D(-edges.offsetX() + 1, -edges.offsetY() + 1);
            grid.get(drawPosition.y()).set(drawPosition.x(),
                    new LagoonTile(LagoonTileType.edge, null));
            // dugCount++;
            for (Edge edge : edges.edges()) {
                for (int tileNum = 0; tileNum < edge.length; tileNum++) {
                    drawPosition.go(edge.direction());
                    grid.get(drawPosition.y()).set(drawPosition.x(),
                            new LagoonTile(LagoonTileType.edge, edge.colour()));
                    dugCount++;
                }
            }

            fillGrid();
        }

        private void fillGrid() {
            Deque<Coord2D> coordStack = new ArrayDeque<Coord2D>();
            coordStack.add(new Coord2D(0, 0));
            while (!coordStack.isEmpty()) {
                Coord2D current = coordStack.pop();
                LagoonTile currentTile = grid.get(current.y()).get(current.x());
                if (currentTile == null) {
                    grid.get(current.y()).set(current.x(), new LagoonTile(LagoonTileType.ground, null));
                    for (Direction direction : Direction.values()) {
                        Coord2D newCoord = new Coord2D(current);
                        newCoord.go(direction);
                        if (newCoord.isInBounds(colCount, rowCount)) {
                            coordStack.add(newCoord);
                        }
                    }
                }
            }
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                List<LagoonTile> row = grid.get(rowIndex);
                for (int colIndex = 0; colIndex < colCount; colIndex++) {
                    LagoonTile currentTile = row.get(colIndex);
                    if (currentTile == null) {
                        row.set(colIndex, new LagoonTile(LagoonTileType.interior, null));
                        dugCount++;
                    }
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            List<StringBuilder> rowStringBuilders = new ArrayList<>();
            for (List<LagoonTile> row : grid) {
                StringBuilder rowBuilder = new StringBuilder();
                for (LagoonTile tile : row) {
                    if (tile != null) {
                        rowBuilder.append(tile.type.symbol());
                    } else {
                        rowBuilder.append(LagoonTileType.ground.symbol());
                    }
                }
                rowBuilder.append(String.format("%n"));
                rowStringBuilders.add(rowBuilder);
            }
            Coord2D drawPosition = new Coord2D(-edges.offsetX() + 1, -edges.offsetY() + 1);
            for (Edge edge : edges.edges()) {
                for (int tileNum = 0; tileNum < edge.length; tileNum++) {
                    drawPosition.go(edge.direction());
                    rowStringBuilders.get(drawPosition.y()).setCharAt(drawPosition.x(),edge.direction().symbol());
                }
            }
            rowStringBuilders.forEach(row->sb.append(row));
            return sb.toString();
        }

        public long getInteriorSize() {
            return dugCount;
        }

    }

    public record Colour(int red, int green, int blue) {
    }

    public record LagoonTile(LagoonTileType type, Colour colour) {
    }

    public enum LagoonTileType {
        ground('.'),
        edge('#'),
        interior('#');

        private final char symbol;

        private LagoonTileType(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }
    }

    public static void main(String[] args) {
        long lagoonSize = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/week3/input_18").toPath())) {
            EdgeCollection edges = new EdgeCollection();
            lines.forEachOrdered(line -> edges.addEdge(line));
            LagoonGrid grid = new LagoonGrid(edges);
            try (java.io.BufferedWriter bWriter = Files.newBufferedWriter(
                    new File(String.format("outputs/output_181/grid.txt")).toPath(),
                    java.nio.charset.StandardCharsets.UTF_8)) {
                bWriter.write(grid.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            lagoonSize = grid.getInteriorSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Lagoon size is %d%n", lagoonSize);
    }
}
