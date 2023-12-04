package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.ToIntFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver021 {
    static Pattern gameIDPattern = Pattern.compile("(?:(Game ))([0-9]+)");
    static Pattern revealPattern = Pattern.compile("(([0-9]+) (red|blue|green), )*(([0-9]+) (red|blue|green);?)");
    static Pattern subRevealPattern = Pattern.compile("([0-9]+) (red|blue|green)");

    static enum cubes {
        red(12),
        green(13),
        blue(14);

        private final int max;

        cubes(int max) {
            this.max = max;
        }

        public int max() {
            return max;
        }
    }

    public static void main(String[] args) {
        ToIntFunction<String> gameIdFinderFunction = new ToIntFunction<>() {
            @Override
            public int applyAsInt(String inputLine) {
                Matcher filterMatcher = gameIDPattern.matcher(inputLine);
                filterMatcher.find();
                int gameNumber = Integer.parseInt(filterMatcher.group(2));
                return gameNumber;
            }

        };

        Predicate<String> filterValidGamesFunction = new Predicate<>() {
            @Override
            public boolean test(String toTest) {
                Matcher filterMatcher = revealPattern.matcher(toTest);
                while (filterMatcher.find()) {
                    String reveal = filterMatcher.group();
                    Matcher subRevealMatcher = subRevealPattern.matcher(reveal);
                    while (subRevealMatcher.find()){
                        int quantity = Integer.parseInt(subRevealMatcher.group(1));
                        String colour = subRevealMatcher.group(2);
                        if (quantity > cubes.valueOf(colour).max()){
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        int total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_02").toPath())) {
            total = inputLines.filter(filterValidGamesFunction).mapToInt(gameIdFinderFunction).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }

}
