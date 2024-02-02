package com.brynnperit.aoc2023.week3.solver162;

import java.util.*;
import java.util.Map.Entry;

public enum Direction {
    NORTH(0, -1, (coord, rows, cols) -> cols.get(coord.x()).lowerEntry(coord.y())),
    EAST(1, 0, (coord, rows, cols) -> rows.get(coord.y()).higherEntry(coord.x())),
    SOUTH(0, 1, (coord, rows, cols) -> cols.get(coord.x()).higherEntry(coord.y())),
    WEST(-1, 0, (coord, rows, cols) -> rows.get(coord.y()).lowerEntry(coord.x()));

    final private int x;
    final private int y;
    final private TriFunction<Coord2D, List<NavigableMap<Integer, Contraption>>, List<NavigableMap<Integer, Contraption>>, Entry<Integer, Contraption>> connectFunction;

    @FunctionalInterface
    private interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    Direction(int x, int y,
            TriFunction<Coord2D, List<NavigableMap<Integer, Contraption>>, List<NavigableMap<Integer, Contraption>>, Entry<Integer, Contraption>> connectFunction) {
        this.x = x;
        this.y = y;
        this.connectFunction = connectFunction;
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

    public Entry<Integer, Contraption> getConnect(Contraption contraption,
            List<NavigableMap<Integer, Contraption>> condensedRows,
            List<NavigableMap<Integer, Contraption>> condensedColumns) {
        return this.connectFunction.apply(contraption.position(), condensedRows, condensedColumns);
    }

    public Entry<Integer, Contraption> getConnect(Coord2D position,
            List<NavigableMap<Integer, Contraption>> condensedRows,
            List<NavigableMap<Integer, Contraption>> condensedColumns) {
        return this.connectFunction.apply(position, condensedRows, condensedColumns);
    }
}