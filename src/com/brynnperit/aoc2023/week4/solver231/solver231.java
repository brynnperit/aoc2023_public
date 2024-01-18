package com.brynnperit.aoc2023.week4.solver231;

import java.util.stream.*;
import java.io.*;
import java.nio.file.Files;

public class solver231 {
    public static void main(String[] args) {
        long longestPathLength =-1;
        long longestPathIgnoringSteep = -1;
        try(Stream<String> lines = Files.lines(new File("inputs/week4/input_23").toPath())){
            HikingGrid grid = new HikingGrid();
            lines.forEachOrdered(l->grid.addLine(l));
            longestPathLength = grid.getLongestPathLength(true);
            // long before = System.nanoTime();
            longestPathIgnoringSteep = grid.getLongestPathLength(false);
            // long after = System.nanoTime();
            // System.out.printf("%d,%d,%.3f seconds%n",before,after,(after-before)/1000000000.0);
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.printf("The longest path avoiding steep slopes is %d long%n",longestPathLength);
        System.out.printf("The longest path not avoiding steep slopes is %d long%n",longestPathIgnoringSteep);
    }    
}
