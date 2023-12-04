package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver031 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)");
    private static Pattern symbolPattern = Pattern.compile("[^0-9.]");

    private static String[] lines = new String[3];
    private static List<String> validNumberStrings = new ArrayList<>();

    private static void checkLine(String nextLine){
        lines[0]=lines[1];
        lines[1]=lines[2];
        lines[2]=nextLine;
        if (lines[1] != null){
            Matcher numberMatcher = numberPattern.matcher(lines[1]);
            while (numberMatcher.find()){
                String foundNumber = numberMatcher.group();
                int startCoord = Integer.max(numberMatcher.start() - 1, 0);
                int endCoord = Integer.min(numberMatcher.end(), lines[1].length() - 1);
                boolean foundSurroundingSymbol = false;
                for (String line : lines){
                    if (line != null && !foundSurroundingSymbol){
                        Matcher symbolMatcher = symbolPattern.matcher(line);
                        if(symbolMatcher.find(startCoord)){
                            foundSurroundingSymbol = symbolMatcher.start() <= endCoord;
                        }
                    }
                }
                if (foundSurroundingSymbol){
                    validNumberStrings.add(foundNumber);
                }
            }
        }
    }
    public static void main(String[] args) {
        int total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_03").toPath())) {
            inputLines.forEachOrdered(solver031::checkLine);
            checkLine(null);
            total = validNumberStrings.stream().mapToInt(s -> Integer.parseInt(s)).sum();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }

}
