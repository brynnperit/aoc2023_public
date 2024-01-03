package com.brynnperit.aoc2023.week3.solver191;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;
public class solver191 {
    public static void main(String[] args) {
        long totalRatings = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/input_19").toPath())) {
            WorkflowCollection flowCollection = new WorkflowCollection();
            List<String> allLines = lines.toList();
            List<Part> partList = allLines.stream().map(line->Part.GetPart(line)).filter(optional->optional.isPresent()).map(optional->optional.get()).toList();
            allLines.forEach(line->flowCollection.addWorkflow(line));
            List<Part> validPartList = partList.stream().filter(part->flowCollection.process(part)).toList();
            totalRatings = validPartList.stream().flatMapToLong(part->part.getAllRatings().stream().mapToLong(i->i)).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The total rating of accepted parts is %d%n", totalRatings);
    }
}
