package com.brynnperit.aoc2023.week3.solver211;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver211 {
    public static void main(String[] args) {
        long plotsReached = -1;
        long firstDistance = 64;
        long bigDistance = 26501365;
        long bigPlotsReached = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/week3/input_21").toPath())) {
            GardenGrid grid = new GardenGrid();
            lines.forEach(l -> grid.addRow(l));

            List<SearchVertice> verts = grid.getGardensExactStepsFromStart(firstDistance);
            // List<SearchVertice> verts = grid.getGardensFromPoint(new Coord2D(grid.columns()/2,grid.rows()/2), firstDistance).stream().filter(v->v.cost()%2==firstDistance%2).sorted((v1,v2)->Long.compare(v1.cost(), v2.cost())).toList();
            // List<SearchVertice> verts = grid.getGardensFromPoint(new Coord2D(0,grid.rows()-1), 1000).stream().filter(v->v.cost()%2==1&&v.cost()<=195).sorted((v1,v2)->Long.compare(v1.cost(), v2.cost())).toList();
            // List<SearchVertice> verts = grid.getGardensFromPoint(new Coord2D(65,130), 1000).stream().filter(v->v.cost()%2==1&&v.cost()<=200).sorted((v1,v2)->Long.compare(v1.cost(), v2.cost())).toList();
            // List<SearchVertice> verts = grid.getGardensFromPoint(new Coord2D(0,grid.rows()-1), 1000).stream().filter(v->v.cost()%2==0&&v.cost()<=200).sorted((v1,v2)->Long.compare(v1.cost(), v2.cost())).toList();
            // System.out.println(grid.toString(verts));
            plotsReached = verts.size();
            bigPlotsReached=grid.getCountOfGardensExactBigStepsFromStart(bigDistance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Reached %d plots in %d%n", plotsReached,firstDistance);
        System.out.printf("Reached %d plots in %d%n", bigPlotsReached,bigDistance);
    }
}
