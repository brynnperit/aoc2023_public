package com.brynnperit.aoc2023.week3.solver182;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.*;

public class solver182 {
    public static void main(String[] args) {
        long lagoonSize = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/week3/input_18").toPath())) {
            EdgeCollection edges = new EdgeCollection();
            lines.forEachOrdered(line -> edges.addEdge(line));
            // System.out.println(grid);
            lagoonSize = edges.getInteriorSize();
            System.out.println("Reverses direction: " + edges.edgesReverseDirection());
            System.out.println("Always changes direction: " + edges.edgesAlwaysChangeDirection());
            // try (java.io.BufferedWriter bWriter = Files.newBufferedWriter(
            //         new File(String.format("outputs/output_182/edges.txt")).toPath(),
            //         java.nio.charset.StandardCharsets.UTF_8)) {
            //     bWriter.write(edges.toString());
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Lagoon size is %d%n", lagoonSize);
    }
}
