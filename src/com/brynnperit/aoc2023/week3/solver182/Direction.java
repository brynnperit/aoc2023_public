package com.brynnperit.aoc2023.week3.solver182;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Direction {
        UP(0, -1, 'U', '3', true),
        RIGHT(1, 0, 'R', '0', false),
        DOWN(0, 1, 'D', '1', true),
        LEFT(-1, 0, 'L', '2', false);

        final private long x;
        final private long y;
        final private char symbol;
        final private char number;
        final private boolean isVertical;

        Direction(long x, long y, char symbol, char number, boolean isVertical) {
            this.x = x;
            this.y = y;
            this.symbol = symbol;
            this.number = number;
            this.isVertical = isVertical;
        }

        private static final Map<Character, Direction> symbolToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Direction> fromSymbol(char symbol) {
            return Optional.ofNullable(symbolToEnum.get(symbol));
        }

        private static final Map<Character, Direction> numberToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.number(), c -> c));

        public static Optional<Direction> fromNumber(char number) {
            return Optional.ofNullable(numberToEnum.get(number));
        }

        public char symbol() {
            return symbol;
        }

        public char number() {
            return number;
        }

        public boolean isVertical() {
            return isVertical;
        }

        public static Direction getDirection(Coord2D start, Coord2D end) {
            long xDifference = end.x() - start.x();
            long yDifference = end.y() - start.y();
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

        public long x() {
            return x;
        }

        public long y() {
            return y;
        }

        public long goX(long x, long length) {
            return x + this.x * length;
        }

        public long goY(long y, long length) {
            return y + this.y * length;
        }

        public Coord2D asCoord() {
            return new Coord2D(x, y);
        }

        public Direction opposite() {
            return Direction.values()[(this.ordinal() + 2) % 4];
        }
    }