package com.brynnperit.aoc2023.week3.solver171;

public class Coord2D implements Comparable<Coord2D> {
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