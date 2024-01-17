package com.brynnperit.aoc2023.week4.solver221;

public record Coord3D(int x, int y, int z) {
    public Coord3D(String x, String y, String z){
        this(Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(z));
    }
}
