package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver051 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)");
    private static Pattern threeNumberPattern = Pattern.compile("([0-9]+) ([0-9]+) ([0-9]+)");
    private static Pattern seedsStartLine = Pattern.compile("seeds:");
    private static Pattern mapLine = Pattern.compile("([a-z]+)-to-([a-z]+) map:");

    private static String currentMapFrom;
    private static String currentMapTo;
    private record destinationRangeAndSize(long destinationRange, long size){};
    private static Map<String,String> fromToNames = new HashMap<>();
    private static Map<String, NavigableMap<Long, destinationRangeAndSize>> fromToRangesByName = new HashMap<>();
    private static List<Long> seeds = new ArrayList<>();

    private static void processInputLine(String line){
        Matcher seedsStartLineMatcher = seedsStartLine.matcher(line);
        Matcher mapLineMatcher = mapLine.matcher(line);
        Matcher threeNumberPatternMatcher = threeNumberPattern.matcher(line);
        if (seedsStartLineMatcher.find()){
            processSeedsLine(line);
        }else if (mapLineMatcher.find()){
            currentMapFrom = mapLineMatcher.group(1);
            currentMapTo = mapLineMatcher.group(2);
            fromToNames.put(currentMapFrom, currentMapTo);
        }else if (threeNumberPatternMatcher.find()){
            long destinationRangeStart = Long.parseLong(threeNumberPatternMatcher.group(1));
            long sourceRangeStart = Long.parseLong(threeNumberPatternMatcher.group(2));
            long rangeSize = Long.parseLong(threeNumberPatternMatcher.group(3));
            if (!fromToRangesByName.containsKey(currentMapFrom)){
                fromToRangesByName.put(currentMapFrom, new TreeMap<>());
            }
            fromToRangesByName.get(currentMapFrom).put(sourceRangeStart, new destinationRangeAndSize(destinationRangeStart, rangeSize));
        }
    }

    private static void processSeedsLine(String line){
        Matcher numberPatternMatcher = numberPattern.matcher(line);
        while (numberPatternMatcher.find()){
            seeds.add(Long.parseLong(numberPatternMatcher.group()));
        }
    }

    private static long findLocationNumberForSeed(long seedNumber){
        long fromNumber = seedNumber;
        long toNumber = -1;
        String mapFrom = "seed";
        while (!mapFrom.equals("location")){
            NavigableMap<Long, destinationRangeAndSize> fromToRanges = fromToRangesByName.get(mapFrom);
            boolean rangeMappingFound = false;
            if (fromToRanges != null){
                Map.Entry<Long, destinationRangeAndSize> closestFromToRange = fromToRanges.floorEntry(fromNumber);
                if (closestFromToRange != null){
                    long fromRangeStart = closestFromToRange.getKey();
                    long toRangeStart = closestFromToRange.getValue().destinationRange();
                    long rangeSize = closestFromToRange.getValue().size();

                    long fromDelta = fromNumber - fromRangeStart;
                    if (fromDelta < rangeSize){
                        rangeMappingFound = true;
                        toNumber = toRangeStart + fromDelta;
                    }
                }
            }
            if (!rangeMappingFound){
                toNumber = fromNumber;
            }
            fromNumber = toNumber;
            mapFrom = fromToNames.get(mapFrom);
        }
        return fromNumber;
    }

    public static void main(String[] args) {
        long lowestLocationNumber = -2;
        //Need data structure that returns the next lowest number to the one being searched for
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_05").toPath())) {
            inputLines.forEachOrdered(solver051::processInputLine);
            lowestLocationNumber = seeds.stream().mapToLong(solver051::findLocationNumberForSeed).reduce(Long.MAX_VALUE, (first,second)->first < second ? first : second);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Lowest location number is: " + lowestLocationNumber);
    }
}
