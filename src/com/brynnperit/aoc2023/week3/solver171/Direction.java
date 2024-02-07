package com.brynnperit.aoc2023.week3.solver171;

import java.util.*;
import java.util.stream.*;

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