package com.brynnperit.aoc2023.week4.solver221;

import java.util.*;

public class RectangularPrism {
    private final int height;
    private int altitude;
    private final Coord2D lowerCoord, higherCoord;
    private final Map<Direction, Coord2D> edges = new EnumMap<>(Direction.class);
    private int number = nextNumber++;
    private static int nextNumber = 0;

    public int getMinAltitude(){
        return altitude;
    }

    public int getMaxAltitude(){
        return altitude+height;
    }

    public RectangularPrism(){
        this(new Coord3D(Integer.MIN_VALUE/2, Integer.MIN_VALUE/2, 0), new Coord3D(Integer.MAX_VALUE/2, Integer.MAX_VALUE/2, 0));
    }

    public RectangularPrism(Coord3D firstCoord, Coord3D secondCoord) {
        this.height = Math.abs(firstCoord.z()-secondCoord.z())+1;
        this.altitude = Math.min(firstCoord.z(),secondCoord.z());
        this.lowerCoord = new Coord2D(Math.min(firstCoord.x(), secondCoord.x()),Math.min(firstCoord.y(), secondCoord.y()));
        this.higherCoord = new Coord2D(Math.max(firstCoord.x(), secondCoord.x())+1,Math.max(firstCoord.y(), secondCoord.y())+1);
        int midX = lowerCoord.x()+((higherCoord.x()-lowerCoord.x())/2);
        int midY = lowerCoord.y()+((higherCoord.y()-lowerCoord.y())/2);
        edges.put(Direction.NORTH, new Coord2D(midX, higherCoord.y()));
        edges.put(Direction.EAST, new Coord2D(higherCoord.x(), midY));
        edges.put(Direction.SOUTH, new Coord2D(midX, lowerCoord.y()));
        edges.put(Direction.WEST, new Coord2D(lowerCoord.x(), midY));
    }

    public boolean overlaps(RectangularPrism other){
        if(Direction.getDirectionNorthSouth(edges.get(Direction.NORTH), other.edges.get(Direction.SOUTH)).orElse(Direction.NORTH) == Direction.NORTH){
            return false;
        }
        if(Direction.getDirectionNorthSouth(edges.get(Direction.SOUTH), other.edges.get(Direction.NORTH)).orElse(Direction.SOUTH) == Direction.SOUTH){
            return false;
        }
        if (Direction.getDirectionEastWest(edges.get(Direction.WEST), other.edges.get(Direction.EAST)).orElse(Direction.WEST) == Direction.WEST){
            return false;
        }
        if(Direction.getDirectionEastWest(edges.get(Direction.EAST), other.edges.get(Direction.WEST)).orElse(Direction.EAST) == Direction.EAST){
            return false;
        }
        return true;
    }

    public void dropToAltitude(int altitude){
        if (altitude <= this.altitude){
            this.altitude = altitude;
        }else{
            throw new IllegalArgumentException(String.format("Prism from %s to %s cannot be dropped from %d to %d%n",lowerCoord,higherCoord,this.altitude,altitude));
        }
    }

    @Override
    public String toString(){
        return String.format("%d:%d,%d,%d~%d,%d,%d",number,lowerCoord.x(),lowerCoord.y(),altitude,higherCoord.x(),higherCoord.y(),altitude+height);
    }

    public int getNumber() {
        return number;
    }
}
