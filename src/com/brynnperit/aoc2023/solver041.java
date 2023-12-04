package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver041 {
    static Pattern numberPattern = Pattern.compile("([0-9]+)");
    static Pattern startPossessedRangePattern = Pattern.compile(":");
    static Pattern startWinningRangePattern = Pattern.compile("\\|");

    private static int countWinningNumbers(String line) {
        int winningNumbersCount = 0;
        Set<Integer> winningNumbers = new TreeSet<>();

        Matcher startPossessedRangeMatcher = startPossessedRangePattern.matcher(line);
        startPossessedRangeMatcher.find();
        int startOfPossessedRange = startPossessedRangeMatcher.end();

        Matcher startWinningRangeMatcher = startWinningRangePattern.matcher(line);
        startWinningRangeMatcher.find();
        int startOfWinningRange = startWinningRangeMatcher.end();

        Matcher winningNumberMatcher = numberPattern.matcher(line).region(startOfWinningRange, line.length());
        while (winningNumberMatcher.find()) {
            winningNumbers.add(Integer.parseInt(winningNumberMatcher.group()));
        }

        Matcher possessedNumberMatcher = numberPattern.matcher(line).region(startOfPossessedRange, startOfWinningRange);
        while (possessedNumberMatcher.find()) {
            int possessedNumber = Integer.parseInt(possessedNumberMatcher.group());
            if (winningNumbers.contains(possessedNumber)) {
                winningNumbersCount++;
            }
        }

        return winningNumbersCount;
    }

    public static void main(String[] args) {
        long total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_04").toPath())) {
            // The "1L << i-1" raises 2 to the i-1; if i is 1 you get 1, if i is 2 you get
            // 2, if i is 3 you get 4, if i is 4 you get 8
            total = inputLines.mapToInt(solver041::countWinningNumbers).mapToLong(i -> i > 0 ? 1L << i - 1 : 0).sum();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }
}
