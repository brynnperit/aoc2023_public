package com.brynnperit.aoc2023.week4.solver251;

import java.io.*;
import java.util.stream.*;
import java.nio.file.*;
import java.util.*;

public class solver251 {
    public static void main(String[] args) {
        int groupSizesMultiplied = -1;
        try(Stream<String> lines = Files.lines(new File("inputs/week4/input_25").toPath())){
            Graph graph = new Graph();
            lines.forEach(l->graph.add(l));
            // long timeBefore = System.nanoTime();
            List<Graph> distinctGroups =  graph.findTwoDistinctGroupsByEdgeRemoval(3);
            // long timeAfter = System.nanoTime();
            // System.out.printf("Before: %d, After: %d, Elapsed: %dms%n",timeBefore,timeAfter,(timeAfter-timeBefore)/1000000);
            groupSizesMultiplied=distinctGroups.stream().mapToInt(g->g.nodeCount()).reduce((g1,g2)->g1*g2).orElse(-1);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.printf("The result of multiplying the sizes of the two distinct groups is %d%n",groupSizesMultiplied);
    }
}
