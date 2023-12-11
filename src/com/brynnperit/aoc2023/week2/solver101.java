package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.*;

public class solver101 {

    private static Coord2DTile startPosition;

    private record Coord2D(int x, int y) {

        public Coord2D goEast() {
            return new Coord2D(x + 1, y);
        }

        public Coord2D goWest() {
            return new Coord2D(x - 1, y);
        }

        public Coord2D goNorth() {
            return new Coord2D(x, y - 1);
        }

        public Coord2D goSouth() {
            return new Coord2D(x, y + 1);
        }
    }

    private record Coord2DTile(Coord2D coord, Tile tile) {
    }

    /**
     * This class either contains the statistics for a tile path or points to a
     * larger TilePath that it's part of
     * When two TilePaths connect the smaller one is consumed by the larger and then
     * just points to the larger one
     */
    private static class TilePath {
        private class TilePathStats {
            private Coord2DTile start;
            private Coord2DTile end;
            private long length;

            public TilePathStats(Coord2DTile start, Coord2DTile end, long length) {
                this.start = start;
                this.end = end;
                this.length = length;
            }
        }

        private enum PathConnectionType {
            // Start of the first connected to the end of the second; merged path will be
            // from start of second to end of first
            StartEnd((first, second) -> Tile.tilesConnect(first.getStart(), second.getEnd()),
                    (first, second) -> doMerge(first, second, second.getStart(), first.getEnd())),
            // End of the first connected to start of the second; merged path will be from
            // start of first to end of second
            EndStart((first, second) -> Tile.tilesConnect(first.getEnd(), second.getStart()),
                    (first, second) -> doMerge(first, second, first.getStart(), second.getEnd())),
            // Start of the first connected to the start of the second; merged path will be
            // from the end of the second to the end of the first
            StartStart((first, second) -> Tile.tilesConnect(first.getStart(), second.getStart()),
                    (first, second) -> doMerge(first, second, second.getEnd(), first.getEnd())),
            // End of the first connected to the end of the second; merged path will be from
            // the start of the first to the start of the second
            EndEnd((first, second) -> Tile.tilesConnect(first.getEnd(), second.getEnd()),
                    (first, second) -> doMerge(first, second, first.getStart(), second.getStart())),
            // There is no connection between the paths!
            NotConnected((first, second) -> false, (first, second) -> {
            });

            private static void doMerge(TilePath first, TilePath second, Coord2DTile newStart, Coord2DTile newEnd) {
                first.setStart(newStart);
                first.setEnd(newEnd);
                first.finishMerging(second);
            }

            private final BiFunction<TilePath, TilePath, Boolean> connectionOperation;
            private final BiConsumer<TilePath, TilePath> fuseTileOperation;

            private PathConnectionType(BiFunction<TilePath, TilePath, Boolean> connectionOperation,
                    BiConsumer<TilePath, TilePath> fuseTileOperation) {
                this.connectionOperation = connectionOperation;
                this.fuseTileOperation = fuseTileOperation;
            }

            public static PathConnectionType getPathConnectionType(TilePath first, TilePath second) {
                for (PathConnectionType connectionType : values()) {
                    if (connectionType.connectionOperation.apply(first, second)) {
                        return connectionType;
                    }
                }
                return NotConnected;
            }

            public void fuseTilePaths(TilePath first, TilePath second) {
                fuseTileOperation.accept(first, second);
            }
        }

        private TilePathStats stats;
        private TilePath mergedInto;

        private boolean hasBeenMerged() {
            return mergedInto != null;
        }

        public long getLength() {
            return getBiggestSuperPath().stats.length;
        }

        public Coord2DTile getStart() {
            return getBiggestSuperPath().stats.start;
        }

        public Coord2DTile getEnd() {
            return getBiggestSuperPath().stats.end;
        }

        private void setStart(Coord2DTile newStart) {
            stats.start = newStart;
        }

        private void setEnd(Coord2DTile newEnd) {
            stats.end = newEnd;
        }

        private void addLength(TilePath otherPath) {
            stats.length = stats.length + otherPath.stats.length;
        }

        private void finishMerging(TilePath toConsume) {
            addLength(toConsume);
            toConsume.stats = null;
            toConsume.mergedInto = this;
        }

        private Optional<TilePath> mergeIntoPath(TilePath otherPath) {
            TilePath firstPath = getBiggestSuperPath();
            otherPath = otherPath.getBiggestSuperPath();
            TilePath toGrow = firstPath.getLength() > otherPath.getLength() ? firstPath : otherPath;
            TilePath toConsume = toGrow == firstPath ? otherPath : firstPath;
            PathConnectionType connectedAt = PathConnectionType.getPathConnectionType(toGrow, toConsume);
            if (connectedAt == PathConnectionType.NotConnected) {
                return Optional.ofNullable(null);
            }
            connectedAt.fuseTilePaths(toGrow, toConsume);
            return Optional.ofNullable(toGrow);
        }

        private static TilePath getBiggestSuperPath(TilePath toGetPathFrom) {
            return toGetPathFrom.hasBeenMerged() ? getBiggestSuperPath(toGetPathFrom.mergedInto) : toGetPathFrom;
        }

        private TilePath getBiggestSuperPath() {
            return getBiggestSuperPath(this);
        }

        public TilePath(Coord2DTile initialTile) {
            this.stats = new TilePathStats(initialTile, initialTile, 1);
        }

        public TilePath mergeIntoExistingTileGrid(List<List<TilePath>> tileGrid) {
            TilePath mergeFrom = getBiggestSuperPath();
            boolean isSingleTile = mergeFrom.stats.length == 1;
            Coord2DTile start = mergeFrom.stats.start;
            Coord2DTile end = mergeFrom.stats.end;
            mergeFrom = tryAdjacentMerges(start, mergeFrom, tileGrid);
            if (!isSingleTile) {
                mergeFrom = tryAdjacentMerges(end, mergeFrom, tileGrid);
            }
            return mergeFrom;
        }

        final static int[][] adjacentCoords = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

        private TilePath tryAdjacentMerges(Coord2DTile mergeTile, TilePath mergeFrom,
                List<List<TilePath>> tileGrid) {
            for (int[] coords : adjacentCoords) {
                int adjacentYCoord = mergeTile.coord.y + coords[1];
                if (adjacentYCoord >= 0 && adjacentYCoord < tileGrid.size()) {
                    List<TilePath> row = tileGrid.get(adjacentYCoord);
                    int adjacentXCoord = mergeTile.coord.x + coords[0];
                    if (adjacentXCoord >= 0 && adjacentXCoord < row.size()) {
                        TilePath adjacent = row.get(adjacentXCoord).getBiggestSuperPath();
                        if (adjacent != mergeFrom) {
                            Optional<TilePath> mergeResult = mergeIntoPath(adjacent);
                            if (mergeResult.isPresent()) {
                                mergeFrom = mergeResult.get();
                            }
                        }
                    }
                }
            }
            return mergeFrom;
        }
    }

    private enum Tile {
        northsouth('|', (position, other) -> connectsNorthOrSouth(position, other)),
        eastwest('-', (position, other) -> connectsEastOrWest(position, other)),
        northeast('L', (position, other) -> connectsNorth(position, other) || connectsEast(position, other)),
        northwest('J', (position, other) -> connectsNorth(position, other) || connectsWest(position, other)),
        eastsouth('F', (position, other) -> connectsSouth(position, other) || connectsEast(position, other)),
        southwest('7', (position, other) -> connectsSouth(position, other) || connectsWest(position, other)),
        ground('.', (position, other) -> false),
        start('S', (position, other) -> false);

        private final BiFunction<Coord2D, Coord2D, Boolean> connectsFunction;
        private static final Map<Character, Tile> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Tile> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        private char symbol;

        private Tile(char symbol, BiFunction<Coord2D, Coord2D, Boolean> connectsFunction) {
            this.symbol = symbol;
            this.connectsFunction = connectsFunction;
        }

        public char symbol() {
            return this.symbol;
        }

        protected static boolean connectsEast(Coord2D position, Coord2D other) {
            return position.goEast().equals(other);
        }

        protected static boolean connectsWest(Coord2D position, Coord2D other) {
            return position.goWest().equals(other);
        }

        protected static boolean connectsNorth(Coord2D position, Coord2D other) {
            return position.goNorth().equals(other);
        }

        protected static boolean connectsSouth(Coord2D position, Coord2D other) {
            return position.goSouth().equals(other);
        }

        protected static boolean connectsNorthOrSouth(Coord2D position, Coord2D other) {
            return position.goSouth().equals(other) || position.goNorth().equals(other);
        }

        protected static boolean connectsEastOrWest(Coord2D position, Coord2D other) {
            return position.goEast().equals(other) || position.goWest().equals(other);
        }

        public static boolean tileConnects(Coord2DTile from, Coord2DTile to) {
            boolean fromTileConnectsToToTile = from.tile.connectsFunction.apply(from.coord(), to.coord());
            return fromTileConnectsToToTile;
        }

        public static boolean tilesConnect(Coord2DTile first, Coord2DTile second) {
            boolean firstTileConnectsToSecond = tileConnects(first, second);
            boolean secondTileConnectsToFirst = tileConnects(second, first);
            return firstTileConnectsToSecond && secondTileConnectsToFirst;
        }
    }

    private static void processInput(String line, List<List<TilePath>> tileGrid) {
        List<TilePath> row = new ArrayList<>();
        int rowYCoord = tileGrid.size();
        tileGrid.add(row);
        for (char c : line.toCharArray()) {
            Optional<Tile> optNewTile = Tile.fromSymbol(c);
            if (!optNewTile.isPresent()) {
                throw new IllegalArgumentException("Unreadable tile found");
            }
            Tile newTile = optNewTile.get();
            insertNewTile(row, newTile, row.size(), rowYCoord, tileGrid, true);
        }
    }

    private static void insertNewTile(List<TilePath> row, Tile newTile, int tileXCoord, int tileYCoord,
            List<List<TilePath>> tileGrid,
            boolean addTile) {
        if (newTile == Tile.start) {
            startPosition = new Coord2DTile(new Coord2D(tileXCoord, tileYCoord), newTile);
        }
        TilePath newPath = new TilePath(new Coord2DTile(new Coord2D(tileXCoord, tileYCoord), newTile));
        if (addTile) {
            row.add(newPath);
        } else {
            row.set(tileXCoord, newPath);
        }

        TilePath mergeResult = newPath.mergeIntoExistingTileGrid(tileGrid);
        // If the new TilePath gets merged into an existing TilePath immediately then we
        // just dereference it and set its grid to the larger path instead.
        row.set(tileXCoord, mergeResult);
    }

    private static void fillInStartTile(List<List<TilePath>> tileGrid) {
        Map<String, Coord2D> tileCoords = new HashMap<>();
        Map<String, Boolean> connectingTiles = new HashMap<>();
        tileCoords.put("north", startPosition.coord.goNorth());
        tileCoords.put("east", startPosition.coord.goEast());
        tileCoords.put("south", startPosition.coord.goSouth());
        tileCoords.put("west", startPosition.coord.goWest());

        for (Map.Entry<String, Coord2D> tileCoordMapEntry : tileCoords.entrySet()) {
            Coord2D tileCoord = tileCoordMapEntry.getValue();
            if (tileCoord.y >= 0 && tileCoord.y < tileGrid.size()) {
                if (tileCoord.x >= 0 && tileCoord.x < tileGrid.get(tileCoord.y).size()) {
                    TilePath adjacentTile = tileGrid.get(tileCoord.y).get(tileCoord.x);
                    if (adjacentTile.getStart().coord.equals(tileCoord)) {
                        if (Tile.tileConnects(adjacentTile.getStart(), startPosition)) {
                            connectingTiles.put(tileCoordMapEntry.getKey(), true);
                        }
                    } else if (adjacentTile.getEnd().coord.equals(tileCoord)) {
                        if (Tile.tileConnects(adjacentTile.getEnd(), startPosition)) {
                            connectingTiles.put(tileCoordMapEntry.getKey(), true);
                        }
                    }
                }
            }
        }
        Tile startTileType;
        if (connectingTiles.keySet().size() == 2) {
            StringBuilder tileName = new StringBuilder();
            if (connectingTiles.containsKey("north")) {
                tileName.append("north");
            }
            if (connectingTiles.containsKey("east")) {
                tileName.append("east");
            }
            if (connectingTiles.containsKey("south")) {
                tileName.append("south");
            }
            if (connectingTiles.containsKey("west")) {
                tileName.append("west");
            }
            startTileType = Tile.valueOf(tileName.toString());
        } else {
            startTileType = Tile.ground;
        }
        insertNewTile(tileGrid.get(startPosition.coord.y), startTileType, startPosition.coord.x, startPosition.coord.y,
                tileGrid, false);
    }

    public static void main(String[] args) {
        long distanceToFarthestPoint = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_10").toPath())) {
            List<List<TilePath>> tileGrid = new ArrayList<>();
            inputLines.forEachOrdered(line -> processInput(line, tileGrid));
            fillInStartTile(tileGrid);
            distanceToFarthestPoint = Math
                    .round(((double) tileGrid.get(startPosition.coord.y).get(startPosition.coord.x).stats.length)
                            / (double) 2.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Distance to farthest point is: " + distanceToFarthestPoint);
    }
}
