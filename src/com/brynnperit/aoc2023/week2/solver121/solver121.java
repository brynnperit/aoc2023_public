package com.brynnperit.aoc2023.week2.solver121;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver121 {

    public static void main(String[] args) {
        long permutationsFolded = -1;
        long permutationsUnfolded = -1;
        long foldedTimeBefore = -1;
        long foldedTimeAfter = -1;
        long unfoldedTimeBefore = -1;
        long unfoldedTimeAfter = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week2/input_12").toPath())) {
            List<String> lines = inputLines.toList();
            foldedTimeBefore = System.nanoTime();
            List<SpringRecord> foldedSpringRecords = lines.stream().map(line -> new SpringRecord(line, false)).collect(Collectors.toList());
            permutationsFolded = foldedSpringRecords.stream().mapToLong(sr->sr.getPermutations()).sum();
            foldedTimeAfter = System.nanoTime();
            unfoldedTimeBefore = System.nanoTime();
            List<SpringRecord> unfoldedSpringRecords = lines.stream().map(line -> new SpringRecord(line, true)).collect(Collectors.toList());
            permutationsUnfolded = unfoldedSpringRecords.stream().mapToLong(sr->sr.getPermutations()).sum();
            unfoldedTimeAfter = System.nanoTime();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("There are %d folded configurations%n",permutationsFolded);
        System.out.printf("Elapsed time for folded: %.3f s%n", (foldedTimeAfter-foldedTimeBefore)/1e9d);
        System.out.printf("There are %d unfolded configurations%n",permutationsUnfolded);
        System.out.printf("Elapsed time for unfolded: %.3f s%n", (unfoldedTimeAfter-unfoldedTimeBefore)/1e9d);
    }
}