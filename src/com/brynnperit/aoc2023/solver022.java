package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class solver022 {
    static Pattern subRevealPattern = Pattern.compile("([0-9]+) (red|blue|green)");

    static enum cubes {
        red,
        green,
        blue;
    }

    public static void main(String[] args) {
        ToIntFunction<String> toPowerSetFunction = new ToIntFunction<>() {
            @Override
            public int applyAsInt(String inputLine) {
                Matcher subRevealMatcher = subRevealPattern.matcher(inputLine);
                Map<cubes, Integer> cubeCounter = new HashMap<>();
                for (cubes cube : cubes.values()) {
                    cubeCounter.put(cube, 0);
                }
                while (subRevealMatcher.find()) {
                    int quantity = Integer.parseInt(subRevealMatcher.group(1));
                    cubes colour = cubes.valueOf(subRevealMatcher.group(2));
                    if (quantity > cubeCounter.get(colour)) {
                        cubeCounter.put(colour, quantity);
                    }
                }
                int powerSetValue = 1;
                for (cubes cube : cubes.values()) {
                    powerSetValue *= cubeCounter.get(cube);
                }
                return powerSetValue;
            }
        };

        int total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_02").toPath())) {
            total = inputLines.mapToInt(toPowerSetFunction).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }

}
