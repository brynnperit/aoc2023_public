package com.brynnperit.aoc2023.week3.solver192;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver192 {
    public static void main(String[] args) {
        long totalDistinctParts = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/input_19").toPath())) {
            WorkflowCollection flowCollection = new WorkflowCollection();
            lines.forEach(line -> flowCollection.addWorkflow(line));
            List<PartRanges> partRangeList = flowCollection.getRanges();
            //partRangeList.sort(PartRanges::compareTo);
            // System.out.printf("Part range list is %d items long%n", partRangeList.size());
            // for (PartRanges ranges : partRangeList) {
            //     System.out.printf("%s:%d distinct items%n", ranges.toString(), ranges.countDistinct());
            // }
            CombinedRangesBranch tree = new CombinedRangesBranch(partRangeList.get(0));
            IntStream.range(1, partRangeList.size()).forEach(i->tree.addRanges(partRangeList.get(i)));
            //System.out.println(tree.toString());
            totalDistinctParts = tree.getDistinctCombinations();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The total number of possible distinct accepted parts is %d%n", totalDistinctParts);
    }
}
