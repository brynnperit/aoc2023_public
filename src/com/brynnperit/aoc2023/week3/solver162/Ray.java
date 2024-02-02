package com.brynnperit.aoc2023.week3.solver162;

public record Ray(Coord2D start, Coord2D end, Direction direction) {
    public Ray(Coord2D start, Coord2D end) {
        this(start, end, Direction.getDirection(start, end));
    }

    public Ray(Coord2D start, Coord2D end, Direction direction) {
        this.start = start;
        this.end = end;
        this.direction = direction;
    }
}