package com.brynnperit.aoc2023.week3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.stream.Collectors;

public class solver151 {
    private static Pattern stepPattern = Pattern.compile("([^,\\n]+)[,\\n]?");

    private static class SequenceStep{
        final char[] steps;
        public SequenceStep(String sequence){
            steps = sequence.toCharArray();
        }

        @Override
        public int hashCode(){
            int currentValue = 0;
            for (char c : steps){
                currentValue += (int) c;
                currentValue*=17;
                currentValue%=256;
            }
            return currentValue;
        }
    }
    private static List<SequenceStep> getSteps(String inputLine){
        List<SequenceStep> allSteps = new ArrayList<>();
        Matcher stepMatcher = stepPattern.matcher(inputLine);
        while(stepMatcher.find()){
            allSteps.add(new SequenceStep(stepMatcher.group(1)));
        }
        return allSteps;
    }

    public static void main(String[] args) {
        long hashTotal = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_15").toPath())) {
            List<SequenceStep> allSteps = inputLines.map(line -> getSteps(line)).collect(Collectors.toList()).get(0);
            hashTotal = allSteps.stream().mapToInt(step->step.hashCode()).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The hash total is %d%n",hashTotal);
    }
}
