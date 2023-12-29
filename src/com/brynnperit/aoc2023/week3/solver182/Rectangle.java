package com.brynnperit.aoc2023.week3.solver182;

import java.util.*;

public record Rectangle(LineSegment top, LineSegment bottom) implements Comparable<Rectangle> {
    public long getArea() {
        long height = 1 + bottom.lowerCoord().y() - top.higherCoord().y();
        long width = 1 + top.higherCoord().x() - top.lowerCoord().x();
        return Math.abs(height * width);
    }

    public List<Coord2D> getAllCornerCoords() {
        List<Coord2D> corners = new ArrayList<>();
        corners.add(getCornerCoords(Direction.UP, Direction.LEFT));
        corners.add(getCornerCoords(Direction.UP, Direction.RIGHT));
        corners.add(getCornerCoords(Direction.DOWN, Direction.RIGHT));
        corners.add(getCornerCoords(Direction.DOWN, Direction.LEFT));
        return corners;
    }

    public Coord2D getCornerCoords(Direction vertical, Direction horizontal) {
        long x;
        long y;
        if (horizontal == Direction.LEFT) {
            x = top.lowerCoord().x();
        } else {
            x = top.higherCoord().x();
        }
        if (vertical == Direction.UP) {
            y = top.lowerCoord().y();
        } else {
            y = bottom.higherCoord().y();
        }
        return new Coord2D(x, y);
    }

    public LineSegment getSide(Direction direction) {
        switch (direction) {
            case UP:
                return top;
            case DOWN:
                return bottom;
            case LEFT:
                return new LineSegment(new Coord2D(top.lowerCoord()), new Coord2D(bottom().lowerCoord()),
                        Direction.RIGHT);
            case RIGHT:
                return new LineSegment(new Coord2D(top.higherCoord()), new Coord2D(bottom.higherCoord()),
                        Direction.LEFT);
            default:
                return null;
        }
    }

    @Override
    public int compareTo(Rectangle other) {
        int value = getCornerCoords(Direction.UP, Direction.LEFT)
                .compareTo(other.getCornerCoords(Direction.UP, Direction.LEFT));
        if (value == 0) {
            value = getCornerCoords(Direction.DOWN, Direction.RIGHT)
                    .compareTo(other.getCornerCoords(Direction.DOWN, Direction.RIGHT));
        }
        return value;
    }
}