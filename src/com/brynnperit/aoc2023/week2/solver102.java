package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class solver102 {

    private static Coord2DTile startPosition;

    private enum Direction {
        north(0, -1),
        east(1, 0),
        south(0, 1),
        west(-1, 0);

        final private int x;
        final private int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int goX(int x) {
            return x + this.x;
        }

        public int goY(int y) {
            return y + this.y;
        }

        public Direction opposite() {
            return Direction.values()[(this.ordinal() + 2) % 4];
        }
    }

    private record Coord2D(int x, int y) {

        public Coord2D go(Direction toGo) {
            return new Coord2D(toGo.goX(x), toGo.goY(y));
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

        private TilePath tryAdjacentMerges(Coord2DTile mergeTile, TilePath mergeFrom,
                List<List<TilePath>> tileGrid) {
            for (Direction direction : Direction.values()) {
                int adjacentYCoord = direction.goY(mergeTile.coord.y);
                if (adjacentYCoord >= 0 && adjacentYCoord < tileGrid.size()) {
                    List<TilePath> row = tileGrid.get(adjacentYCoord);
                    int adjacentXCoord = direction.goX(mergeTile.coord.x);
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
        start('S', (position, other) -> false),
        outsideLoop('O', (position, other) -> false),
        insideLoop('I', (position, other) -> false),
        beingSearched('?', (position, other) -> false);

        private final BiPredicate<Coord2D, Coord2D> connectsFunction;
        private static final Map<Character, Tile> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Tile> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        private final char symbol;

        private Tile(char symbol, BiPredicate<Coord2D, Coord2D> connectsFunction) {
            this.symbol = symbol;
            this.connectsFunction = connectsFunction;
        }

        public char symbol() {
            return this.symbol;
        }

        protected static boolean connectsEast(Coord2D position, Coord2D other) {
            return position.go(Direction.east).equals(other);
        }

        protected static boolean connectsWest(Coord2D position, Coord2D other) {
            return position.go(Direction.west).equals(other);
        }

        protected static boolean connectsNorth(Coord2D position, Coord2D other) {
            return position.go(Direction.north).equals(other);
        }

        protected static boolean connectsSouth(Coord2D position, Coord2D other) {
            return position.go(Direction.south).equals(other);
        }

        protected static boolean connectsNorthOrSouth(Coord2D position, Coord2D other) {
            return position.go(Direction.south).equals(other) || position.go(Direction.north).equals(other);
        }

        protected static boolean connectsEastOrWest(Coord2D position, Coord2D other) {
            return position.go(Direction.east).equals(other) || position.go(Direction.west).equals(other);
        }

        public static boolean tileConnects(Coord2DTile from, Coord2DTile to) {
            boolean fromTileConnectsToToTile = from.tile.connectsFunction.test(from.coord(), to.coord());
            return fromTileConnectsToToTile;
        }

        public static boolean tilesConnect(Coord2DTile first, Coord2DTile second) {
            boolean firstTileConnectsToSecond = tileConnects(first, second);
            boolean secondTileConnectsToFirst = tileConnects(second, first);
            return firstTileConnectsToSecond && secondTileConnectsToFirst;
        }
    }

    private static void processInput(String line, List<List<TilePath>> tilePathGrid, List<List<Tile>> tileGrid) {
        List<TilePath> row = new ArrayList<>();
        List<Tile> tileRow = new ArrayList<>();
        int rowYCoord = tilePathGrid.size();
        tilePathGrid.add(row);
        tileGrid.add(tileRow);
        for (char c : line.toCharArray()) {
            Optional<Tile> optNewTile = Tile.fromSymbol(c);
            if (!optNewTile.isPresent()) {
                throw new IllegalArgumentException("Unreadable tile found");
            }
            Tile newTile = optNewTile.get();
            tileRow.add(newTile);
            insertNewTile(row, newTile, row.size(), rowYCoord, tilePathGrid, true);
        }
    }

    private static void insertNewTile(List<TilePath> row, Tile newTile, int tileXCoord, int tileYCoord,
            List<List<TilePath>> tilePathGrid,
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

        TilePath mergeResult = newPath.mergeIntoExistingTileGrid(tilePathGrid);
        // If the new TilePath gets merged into an existing TilePath immediately then we
        // just dereference it and set its grid to the larger path instead.
        row.set(tileXCoord, mergeResult);
    }

    private static void fillInStartTile(List<List<TilePath>> tilePathGrid, List<List<Tile>> tileGrid) {
        Map<String, Coord2D> tileCoords = new HashMap<>();
        Map<String, Boolean> connectingTiles = new HashMap<>();
        tileCoords.put("north", startPosition.coord.go(Direction.north));
        tileCoords.put("east", startPosition.coord.go(Direction.east));
        tileCoords.put("south", startPosition.coord.go(Direction.south));
        tileCoords.put("west", startPosition.coord.go(Direction.west));

        for (Map.Entry<String, Coord2D> tileCoordMapEntry : tileCoords.entrySet()) {
            Coord2D tileCoord = tileCoordMapEntry.getValue();
            if (tileCoord.y >= 0 && tileCoord.y < tilePathGrid.size()) {
                if (tileCoord.x >= 0 && tileCoord.x < tilePathGrid.get(tileCoord.y).size()) {
                    TilePath adjacentTile = tilePathGrid.get(tileCoord.y).get(tileCoord.x);
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
        tileGrid.get(startPosition.coord.y).set(startPosition.coord.x, startTileType);
        insertNewTile(tilePathGrid.get(startPosition.coord.y), startTileType, startPosition.coord.x,
                startPosition.coord.y,
                tilePathGrid, false);
    }

    private static List<Coord2D> cleanUpTileGrid(List<List<TilePath>> tilePathGrid, List<List<Tile>> tileGrid,
            TilePath startTilePath) {
        int lowestX = Integer.MAX_VALUE;
        int highestX = Integer.MIN_VALUE;
        int lowestY = Integer.MAX_VALUE;
        int highestY = Integer.MIN_VALUE;
        for (int yRow = 0; yRow < tileGrid.size(); yRow++) {
            List<TilePath> tilePathRow = tilePathGrid.get(yRow);
            List<Tile> tileRow = tileGrid.get(yRow);
            for (int xColumn = 0; xColumn < tileRow.size(); xColumn++) {
                TilePath currentTilePath = tilePathRow.get(xColumn).getBiggestSuperPath();
                if (currentTilePath != startTilePath) {
                    tileRow.set(xColumn, Tile.ground);
                } else {
                    lowestX = Math.min(lowestX, xColumn);
                    highestX = Math.max(highestX, xColumn);
                    lowestY = Math.min(lowestY, yRow);
                    highestY = Math.max(highestY, yRow);
                }
            }
        }
        List<Coord2D> boundaries = new ArrayList<>();
        boundaries.add(new Coord2D(lowestX, lowestY));
        boundaries.add(new Coord2D(highestX, highestY));
        return boundaries;
    }

    private static List<List<Tile>> zoomTileGrid(List<List<Tile>> tileGrid, List<Coord2D> pathBoundaries) {
        List<List<Tile>> zoomedTileGrid = new ArrayList<>();
        int tileGridYMax = tileGrid.size();
        // int lowX = pathBoundaries.get(0).x - 1;
        // int lowY = pathBoundaries.get(0).y - 1;
        // int highX = pathBoundaries.get(1).x + 1;
        // int highY = pathBoundaries.get(1).y + 1;
        int lowX = -1;
        int lowY = -1;
        int highX = tileGrid.get(0).size();
        int highY = tileGrid.size();
        for (int yRow = lowY; yRow <= highY; yRow++) {
            zoomedTileGrid.add(new ArrayList<>());
            zoomedTileGrid.add(new ArrayList<>());
            zoomedTileGrid.add(new ArrayList<>());
            List<Tile> originalRow = (yRow >= 0 && yRow < tileGridYMax) ? tileGrid.get(yRow) : null;
            List<Tile> firstZoomedRow = zoomedTileGrid.get(zoomedTileGrid.size() - 3);
            List<Tile> secondZoomedRow = zoomedTileGrid.get(zoomedTileGrid.size() - 2);
            List<Tile> thirdZoomedRow = zoomedTileGrid.get(zoomedTileGrid.size() - 1);
            for (int xCol = lowX; xCol <= highX; xCol++) {
                Tile originalTile;
                if ((yRow == lowY || yRow == highY) || (xCol == lowX || xCol == highX)) {
                    originalTile = Tile.outsideLoop;
                } else {
                    originalTile = originalRow.get(xCol);
                }
                switch (originalTile) {
                    case eastsouth:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.eastsouth);
                        secondZoomedRow.add(Tile.eastwest);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.northsouth);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case southwest:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.eastwest);
                        secondZoomedRow.add(Tile.southwest);
                        secondZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.northsouth);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case northwest:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.northsouth);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.eastwest);
                        secondZoomedRow.add(Tile.northwest);
                        secondZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case northeast:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.northsouth);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.northeast);
                        secondZoomedRow.add(Tile.eastwest);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case eastwest:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.eastwest);
                        secondZoomedRow.add(Tile.eastwest);
                        secondZoomedRow.add(Tile.eastwest);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case northsouth:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.northsouth);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.northsouth);
                        secondZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.northsouth);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case start:
                    case insideLoop:
                    case beingSearched:
                    case ground:
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        firstZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        secondZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        thirdZoomedRow.add(Tile.ground);
                        break;
                    case outsideLoop:
                        firstZoomedRow.add(Tile.outsideLoop);
                        firstZoomedRow.add(Tile.outsideLoop);
                        firstZoomedRow.add(Tile.outsideLoop);
                        secondZoomedRow.add(Tile.outsideLoop);
                        secondZoomedRow.add(Tile.outsideLoop);
                        secondZoomedRow.add(Tile.outsideLoop);
                        thirdZoomedRow.add(Tile.outsideLoop);
                        thirdZoomedRow.add(Tile.outsideLoop);
                        thirdZoomedRow.add(Tile.outsideLoop);
                        break;
                }
            }
        }
        return zoomedTileGrid;
    }

    private static void setGroundTilesToInsideOrOutside(List<List<Tile>> tileGrid) {
        boolean done = false;
        boolean finalLoop = false;
        while (!done) {
            done = true;
            for (int yRow = 0; yRow < tileGrid.size(); yRow++) {
                List<Tile> tileRow = tileGrid.get(yRow);
                for (int xCol = 0; xCol < tileRow.size(); xCol++) {
                    Tile currentTile = tileRow.get(xCol);
                    if (!finalLoop) {
                        if (currentTile == Tile.ground) {
                            done = false;
                            // Run our outside tile finder on the current ground square; skip west since
                            // we're coming from there
                            Tile result = findOutsideTiles(tileGrid, xCol, yRow, Direction.west, 10);
                            if (result == Tile.outsideLoop) {
                                // We've found an outside tile connected to the current tile; set all the
                                // connected "being searched" tiles to outside
                                fillSearchedTiles(tileGrid, xCol, yRow, Tile.outsideLoop, 60);
                            }
                        }else if (currentTile == Tile.beingSearched){
                            Tile result = findOutsideTiles(tileGrid, xCol, yRow, Direction.west, 2);
                            if (result == Tile.outsideLoop){
                                fillSearchedTiles(tileGrid, xCol, yRow, Tile.outsideLoop, 60);
                                done = false;
                            }
                        }
                    }else{
                        if (currentTile == Tile.beingSearched){
                            fillSearchedTiles(tileGrid, xCol, yRow, Tile.insideLoop, 60);
                        }
                    }
                }
            }
            if (done && !finalLoop) {
                finalLoop = true;
                done = false;
            }
        }
    }

    private static Tile findOutsideTiles(List<List<Tile>> tileGrid, int xCol, int yRow, Direction toSkip, int limit) {
        if (limit > 0) {
            if (yRow < 0 || yRow >= tileGrid.size() || xCol < 0 || xCol >= tileGrid.get(yRow).size()) {
                return Tile.outsideLoop;
            }
            switch (tileGrid.get(yRow).get(xCol)) {
                case beingSearched:
                case ground:
                    // Set every ground tile we encounter to a "being searched" tile so that we
                    // don't infinite-loop
                    tileGrid.get(yRow).set(xCol, Tile.beingSearched);
                    for (Direction currentDirection : Direction.values()) {
                        if (currentDirection != toSkip) {
                            // Keep recursing and setting additional ground tiles in different directions to
                            // "being searched" until we encounter an outside tile or run out of tiles
                            Tile result = findOutsideTiles(tileGrid, currentDirection.goX(xCol),
                                    currentDirection.goY(yRow),
                                    currentDirection.opposite(), limit - 1);
                            if (result == Tile.insideLoop || result == Tile.outsideLoop) {
                                return result;
                            }
                        }
                    }
                    break;
                case insideLoop:
                    return Tile.insideLoop;
                case outsideLoop:
                    return Tile.outsideLoop;
                default:
                    break;
            }
        }
        return Tile.beingSearched;
    }

    private static void fillSearchedTiles(List<List<Tile>> tileGrid, int xCol, int yRow, Tile toFill, int limit) {
        if (limit > 0 && !(yRow < 0 || yRow >= tileGrid.size() || xCol < 0 || xCol >= tileGrid.get(yRow).size())) {
            switch (tileGrid.get(yRow).get(xCol)) {
                case beingSearched:
                    tileGrid.get(yRow).set(xCol, toFill);
                    for (Direction currentDirection : Direction.values()) {
                            // Keep recursing and setting additional ground tiles in different directions to
                            // "being searched" until we encounter an outside tile or run out of tiles
                            fillSearchedTiles(tileGrid, currentDirection.goX(xCol), currentDirection.goY(yRow), toFill, limit - 1);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void printTileGrid(List<List<Tile>> tileGrid) {
        StringBuilder toPrint = new StringBuilder();
        tileGrid.forEach(list -> {
            list.forEach(tile -> toPrint.append(tile.symbol));
            toPrint.append('\n');
        });
        System.out.println(toPrint);
    }

    private static void printTileGridUnzoomed(List<List<Tile>> tileGrid) {
        StringBuilder toPrint = new StringBuilder();
        // Incrementing row and column counters by 2 gets only the cells present in the
        // un-zoomed grid
        for (int yRow = 1; yRow < tileGrid.size(); yRow += 3) {
            List<Tile> tileRow = tileGrid.get(yRow);
            for (int xCol = 1; xCol < tileRow.size(); xCol += 3) {
                Tile currentTile = tileRow.get(xCol);
                toPrint.append(currentTile.symbol);
            }
            toPrint.append('\n');
        }
        System.out.println(toPrint);
    }

    private static long getInsideTilesFromZoomedTileGrid(List<List<Tile>> tileGrid) {
        long insideTileCount = 0;
        // Incrementing row and column counters by 2 gets only the cells present in the
        // un-zoomed grid
        for (int yRow = 1; yRow < tileGrid.size(); yRow += 3) {
            List<Tile> tileRow = tileGrid.get(yRow);
            for (int xCol = 1; xCol < tileRow.size(); xCol += 3) {
                Tile currentTile = tileRow.get(xCol);
                if (currentTile == Tile.insideLoop) {
                    insideTileCount++;
                }
            }
        }
        return insideTileCount;
    }

    public static void main(String[] args) {
        long insideTileCount = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_10").toPath())) {
            List<List<TilePath>> tilePathGrid = new ArrayList<>();
            List<List<Tile>> tileGrid = new ArrayList<>();
            inputLines.forEachOrdered(line -> processInput(line, tilePathGrid, tileGrid));
            fillInStartTile(tilePathGrid, tileGrid);
            // printTileGrid(tileGrid);
            TilePath startTilePath = tilePathGrid.get(startPosition.coord.y).get(startPosition.coord.x);
            List<Coord2D> pathBoundaries = cleanUpTileGrid(tilePathGrid, tileGrid, startTilePath);
            // System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
             printTileGrid(tileGrid);
            List<List<Tile>> zoomedTileGrid = zoomTileGrid(tileGrid, pathBoundaries);
            // System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            // printTileGrid(zoomedTileGrid);
            setGroundTilesToInsideOrOutside(zoomedTileGrid);
            // System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            // printTileGrid(zoomedTileGrid);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            printTileGridUnzoomed(zoomedTileGrid);
            insideTileCount = getInsideTilesFromZoomedTileGrid(zoomedTileGrid);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("There are " + insideTileCount + " inside tiles");
    }
}
