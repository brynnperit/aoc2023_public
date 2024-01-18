package com.brynnperit.aoc2023.week4.solver231;

import java.util.*;

public class PathExplorer {
    private Coord2D position;
    private Coord2D goal;
    private List<BitSet> visitedTiles;
    private long pathLength;

    public Coord2D getPosition() {
        return new Coord2D(position);
    }

    public <T> PathExplorer(Coord2D startPosition, Coord2D endPosition, List<List<T>> grid) {
        this.position = new Coord2D(startPosition);
        this.goal = new Coord2D(endPosition);
        this.visitedTiles = grid.stream().map(row->new BitSet(row.size())).toList();
        this.visitedTiles.get(startPosition.y()).set(startPosition.x());
        this.pathLength = 0L;
    }

    public void go(Direction toGo){
        position.go(toGo);
        visitedTiles.get(position.y()).set(position.x());
        pathLength++;
    }

    public boolean hasVisited(int x, int y){
        return visitedTiles.get(y).get(x);
    }

    public long pathLength() {
        return pathLength;
    }

    public int positionX() {
        return position.x();
    }

    public int positionY() {
        return position.y();
    }

    public boolean reachedDestination() {
        return position.equals(goal);
    }
}
