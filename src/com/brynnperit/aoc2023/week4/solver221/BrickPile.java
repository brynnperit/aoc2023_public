package com.brynnperit.aoc2023.week4.solver221;

import java.util.*;
import java.util.regex.*;

public class BrickPile {
    private static final Pattern coordPattern = Pattern.compile("([0-9]+),([0-9]+),([0-9]+)");
    private final PileNode baseBrick = new PileNode();
    private final PriorityQueue<RectangularPrism> unprocessedPrisms = new PriorityQueue<>((p1,p2)->Integer.compare(p1.getMinAltitude(), p2.getMinAltitude()));

    public void addBrick(String brickString){
        Matcher coordMatcher = coordPattern.matcher(brickString);
        coordMatcher.find();
        Coord3D firstCoord = new Coord3D(coordMatcher.group(1), coordMatcher.group(2), coordMatcher.group(3));
        coordMatcher.find();
        Coord3D secondCoord = new Coord3D(coordMatcher.group(1), coordMatcher.group(2), coordMatcher.group(3));
        unprocessedPrisms.add(new RectangularPrism(firstCoord, secondCoord));
    }

    public Set<RectangularPrism> getRemovableBricks(){
        addUnprocessedPrisms();
        return baseBrick.getRemovableBricks();
    }

    public long getNumberOfBricksThatWouldFall(){
        addUnprocessedPrisms();
        return baseBrick.getNumberOfBricksThatWouldFall();
    }

    private void addUnprocessedPrisms() {
        while(!unprocessedPrisms.isEmpty()){
            baseBrick.add(unprocessedPrisms.poll());
        }
    }
}
