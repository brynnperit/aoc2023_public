package com.brynnperit.aoc2023.week4.solver221;

import java.util.*;

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

    public <T> boolean isInBounds(List<List<T>> grid) {
        return x >= 0 && x < grid.get(0).size() && y >= 0 && y < grid.size();
    }

    public <T> boolean isAtEdge(List<List<T>> grid, Direction direction){
        return (direction.x()==-1 && x==0)||(direction.x()==1&&x==grid.get(0).size()-1)||(direction.y()==-1&&y==0)||(direction.y()==1&&y==grid.size()-1);
    }

    public <T> Coord2D putAtEdge(List<List<T>> grid, Direction direction){
        if(direction.x()==-1){
            return new Coord2D(0,y);
        }
        if(direction.x()==1){
            return new Coord2D(grid.get(0).size()-1,y);
        }
        if(direction.y()==-1){
            return new Coord2D(x,0);
        }
        if(direction.y()==1){
            return new Coord2D(x,grid.size()-1);
        }
        return null;
    }

    public static <T> Coord2D getCornerAtCounterClockwiseFromDirection(Direction direction, List<List<T>> grid) {
        int row = grid.size() - 1;
        int col = grid.get(0).size() - 1;
        switch (direction) {
        case NORTH:
            return new Coord2D(0, 0);
        case EAST:
            return new Coord2D(col, 0);
        case SOUTH:
            return new Coord2D(col, row);
        case WEST:
            return new Coord2D(0, row);
        }
        return null;
    }
}
