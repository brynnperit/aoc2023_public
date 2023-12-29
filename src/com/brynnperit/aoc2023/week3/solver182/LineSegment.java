package com.brynnperit.aoc2023.week3.solver182;

import java.util.*;

public record LineSegment(Coord2D lowerCoord, Coord2D higherCoord, Direction normal)
        implements Comparable<LineSegment> {

    public LineSegment(Edge e) {
        this(new Coord2D(e.lowerCoord()), new Coord2D(e.higherCoord()), e.interiorSide());
    }

    public long length() {
        return lowerCoord.distanceTo(higherCoord)+1;
    }

    public boolean fitsWithin(LineSegment other) {
        if (normal.isVertical()) {
            return lowerCoord.x() >= other.lowerCoord().x() && higherCoord.x() <= other.higherCoord.x();
        } else {
            return lowerCoord.y() >= other.lowerCoord().y() && higherCoord.y() <= other.higherCoord.y();
        }
    }

    public List<LineSegment> generateOppositeLines(LineSegment opposite) {
        if (normal.isVertical()){
            return generateHorizontalOppositeLines(opposite);
        }else{
            return generateVerticalOppositeLines(opposite);
        }
    }
    private List<LineSegment> generateVerticalOppositeLines(LineSegment subsection){
        List<LineSegment> newLines = new ArrayList<>();
        LineSegment mainSection = this;
        long xVal = mainSection.lowerCoord.x();
        if (Long.compare(mainSection.lowerCoord.y(), subsection.lowerCoord.y()) < 0) {
            Coord2D newLowerCoord = new Coord2D(xVal, mainSection.lowerCoord.y());
            Coord2D newHigherCoord = new Coord2D(xVal, subsection.lowerCoord.y()-1);
            Coord2D newMainLowerCoord = new Coord2D(xVal, subsection.lowerCoord.y());
            newLines.add(new LineSegment(newLowerCoord, newHigherCoord, mainSection.normal));
            mainSection = new LineSegment(newMainLowerCoord, mainSection.higherCoord, normal);
        }
        if (Long.compare(mainSection.higherCoord.y(), subsection.higherCoord.y()) > 0) {
            Coord2D newMainHigherCoord = new Coord2D(xVal, subsection.higherCoord.y());
            Coord2D newLowerCoord = new Coord2D(xVal, subsection.higherCoord.y()+1);
            Coord2D newHigherCoord = new Coord2D(xVal, mainSection.higherCoord.y());
            newLines.add(new LineSegment(newLowerCoord, newHigherCoord, mainSection.normal));
            mainSection = new LineSegment(mainSection.lowerCoord, newMainHigherCoord, normal);
        }
        newLines.add(mainSection);
        return newLines;
    }

    private List<LineSegment> generateHorizontalOppositeLines(LineSegment subsection){
        List<LineSegment> newLines = new ArrayList<>();
        LineSegment mainSection = this;
        long yVal = mainSection.lowerCoord.y();
        if (Long.compare(mainSection.lowerCoord.x(), subsection.lowerCoord.x()) < 0) {
            Coord2D newLowerCoord = new Coord2D(mainSection.lowerCoord.x(), yVal);
            Coord2D newHigherCoord = new Coord2D(subsection.lowerCoord.x()-1,yVal);
            Coord2D newMainLowerCoord = new Coord2D(subsection.lowerCoord.x(), yVal);
            newLines.add(new LineSegment(newLowerCoord, newHigherCoord, mainSection.normal));
            mainSection = new LineSegment(newMainLowerCoord, mainSection.higherCoord, normal);
        }
        if (Long.compare(mainSection.higherCoord.x(), subsection.higherCoord.x()) > 0) {
            Coord2D newMainHigherCoord = new Coord2D(subsection.higherCoord.x(), yVal);
            Coord2D newLowerCoord = new Coord2D(subsection.higherCoord.x()+1,yVal);
            Coord2D newHigherCoord = new Coord2D(mainSection.higherCoord.x(),yVal);
            newLines.add(new LineSegment(newLowerCoord, newHigherCoord, mainSection.normal));
            mainSection = new LineSegment(mainSection.lowerCoord, newMainHigherCoord, normal);
        }
        newLines.add(mainSection);
        return newLines;
    }

    public boolean isSubsetOf(LineSegment other){
        return lowerCoord.compareTo(other.lowerCoord) > -1 && higherCoord.compareTo(other.higherCoord) < 1;
    }

    public boolean overlapsX(LineSegment other) {
        boolean otherCoordEndsBeforeStart = other.higherCoord.x() < lowerCoord.x();
        boolean otherCoordStartsAfterEnd = other.lowerCoord.x() > higherCoord.x();
        return !(otherCoordEndsBeforeStart || otherCoordStartsAfterEnd);
    }

    public boolean overlapsY(LineSegment other) {
        boolean otherCoordEndsBeforeStart = other.higherCoord.y() < lowerCoord.y();
        boolean otherCoordStartsAfterEnd = other.lowerCoord.y() > higherCoord.y();
        return !(otherCoordEndsBeforeStart || otherCoordStartsAfterEnd);
    }

    @Override
    public int compareTo(LineSegment other) {
        int value = lowerCoord.compareTo(other.lowerCoord);
        if (value == 0) {
            value = higherCoord.compareTo(other.higherCoord);
        }
        return value;
    }

    public int compareToY(LineSegment other) {
        int value = lowerCoord.compareToY(other.lowerCoord);
        if (value == 0) {
            value = higherCoord.compareToY(other.higherCoord);
        }
        return value;
    }
}