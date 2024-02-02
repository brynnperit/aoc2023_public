package com.brynnperit.aoc2023.week3.solver152;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class solver152 {
    private static final Pattern stepPattern = Pattern.compile("([^,\\n]+)[,\\n]?");

    private static List<SequenceStep> getSteps(String inputLine) {
        List<SequenceStep> allSteps = new ArrayList<>();
        Matcher stepMatcher = stepPattern.matcher(inputLine);
        while (stepMatcher.find()) {
            allSteps.add(new SequenceStep(stepMatcher.group(1)));
        }
        return allSteps;
    }

    private static long getLensPower(int boxNumber, List<Lens> box) {
        long boxLensPower = 0;
        ListIterator<Lens> lensIterator = box.listIterator();
        while (lensIterator.hasNext()) {
            int lensIndex = lensIterator.nextIndex();
            Lens lens = lensIterator.next();
            long lensPower = 1 + boxNumber;
            lensPower *= lensIndex + 1;
            lensPower *= lens.focalLength();
            boxLensPower += lensPower;
        }
        // The focusing power of a single lens is the result of multiplying together:

        // One plus the box number of the lens in question.
        // The slot number of the lens within the box: 1 for the first lens, 2 for the
        // second lens, and so on.
        // The focal length of the lens.
        return boxLensPower;
    }

    public static void main(String[] args) {
        long lensPower = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week3/input_15").toPath())) {
            List<SequenceStep> allSteps = inputLines.map(line -> getSteps(line)).collect(Collectors.toList()).get(0);
            Map<Integer, List<Lens>> lensBoxes = new TreeMap<>();
            allSteps.stream().forEachOrdered(step -> step.actOnBox(lensBoxes));
            lensPower = lensBoxes.entrySet().stream().mapToLong(entry -> getLensPower(entry.getKey(), entry.getValue()))
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The total lens power is %d%n", lensPower);
    }

}
