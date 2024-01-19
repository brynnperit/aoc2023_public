package com.brynnperit.aoc2023.week1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class solver061 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)");
    private static Pattern timeStartLine = Pattern.compile("Time:");
    // private static Pattern distanceStartLine = Pattern.compile("Distance:");

    private record RaceStat(int time, int distance) {
    };

    private record WinningRange(long lowestHoldDuration, long highestHoldDuration) {
    };

    private static List<Integer> times = new ArrayList<>();
    private static List<Integer> distances = new ArrayList<>();

    private static void processInputLine(String line) {
        // Matcher distanceStartLineMatcher = distanceStartLine.matcher(line);
        Matcher timeStartLineMatcher = timeStartLine.matcher(line);
        Matcher numberPatternMatcher = numberPattern.matcher(line);
        boolean isTimeLine = false;
        if (timeStartLineMatcher.find()) {
            isTimeLine = true;
        }
        while (numberPatternMatcher.find()) {
            if (isTimeLine) {
                times.add(Integer.parseInt(numberPatternMatcher.group()));
            } else {
                distances.add(Integer.parseInt(numberPatternMatcher.group()));
            }
        }
    }

    private static WinningRange getWinningRangeForRace(RaceStat race) {
        WinningRange toReturn = new WinningRange(-1, -1);
        // Distance formula is (holdDuration)(raceTime-holdDuration) =
        // distanceTravelled, winning races are (holdDuration)(raceTime-holdDuration) >
        // distanceNeeded
        // solve (very slowly because slow brain day caused by lack of sleep) for
        // holdDuration for -(holdDuration)^2 + raceTime*holdDuration - distanceNeeded =
        // 0 as we win the race between those two points
        // It's just a quadratic formula (ax^2 + bx + c)where x is hold duration, b is
        // racetime, a=-1, c = -distanceNeeded; first point is (-racetime +
        // sqrt(racetime^2 - 4*-1*-distanceneeded))/-2
        // second point is is (-racetime - sqrt(racetime^2 - 4*-1*-distanceneeded))/-2
        // determinant = b^2-4ac = racetime^2 + 4distanceNeeded, if this is >= 0 then
        // can be solved.
        double aPart = -1;
        double bPart = race.time();
        //Add 1 to the race distance here because we must go further than it to win.
        double cPart = -1 * (race.distance()+1);
        double determinant = Math.pow(bPart, 2) - 4 * aPart * cPart;
        double lowRoot, highRoot;
        if (determinant >= 0) {
            lowRoot = (-bPart + Math.sqrt(Math.pow(bPart, 2) - 4 * aPart * cPart)) / (2 * aPart);
            highRoot = (-bPart - Math.sqrt(Math.pow(bPart, 2) - 4 * aPart * cPart)) / (2 * aPart);
            long lowRootRanged = Math.round(Math.ceil(lowRoot));
            long highRootRanged = Math.round(Math.floor(highRoot));

            if (determinant > 0) {
                toReturn = new WinningRange(lowRootRanged, highRootRanged);
            } else {
                toReturn = new WinningRange(lowRootRanged, lowRootRanged);
            }
        }
        return toReturn;
    }

    public static void main(String[] args) {
        long raceWinningPermutations = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week1/input_061_test").toPath())) {
            inputLines.forEach(solver061::processInputLine);
            List<RaceStat> races = IntStream.range(0, Math.min(times.size(), distances.size()))
                    .mapToObj(i -> new RaceStat(times.get(i), distances.get(i)))
                    .collect(Collectors.toCollection(ArrayList::new));
            raceWinningPermutations = races.stream().map(race -> getWinningRangeForRace(race)).map(range -> {
                System.out.println("Range:" + range.lowestHoldDuration() + " to " + range.highestHoldDuration());
                return range;
            }).mapToLong(range -> (range.highestHoldDuration - range.lowestHoldDuration) + 1).reduce(1,
                    (first, second) -> first * second);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("There are " + raceWinningPermutations + " permutations to win all races");
    }
}
