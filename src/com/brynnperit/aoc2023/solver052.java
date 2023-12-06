package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver052 {
    private static Pattern twoNumberPattern = Pattern.compile("([0-9]+) ([0-9]+)");
    private static Pattern threeNumberPattern = Pattern.compile("([0-9]+) ([0-9]+) ([0-9]+)");
    private static Pattern seedsStartLine = Pattern.compile("seeds:");
    private static Pattern mapLine = Pattern.compile("([a-z]+)-to-([a-z]+) map:");

    private static String currentMapFrom;
    private static String currentMapTo;

    private record destinationRangeAndSize(long destinationRange, long size) {
    };

    private record mapRange(long rangeStart, long size) implements Comparable<mapRange> {

        @Override
        public int compareTo(mapRange arg0) {
            int result = Long.compare(this.rangeStart, arg0.rangeStart);
            if (result == 0) {
                result = Long.compare(this.size, arg0.size);
            }
            return result;
        }
    };

    private static Map<String, String> fromToNames = new HashMap<>();
    private static Map<String, NavigableMap<Long, destinationRangeAndSize>> fromToRangesByName = new HashMap<>();
    private static Set<mapRange> seeds = new TreeSet<>();

    private static void processInputLine(String line) {
        Matcher seedsStartLineMatcher = seedsStartLine.matcher(line);
        Matcher mapLineMatcher = mapLine.matcher(line);
        Matcher threeNumberPatternMatcher = threeNumberPattern.matcher(line);
        if (seedsStartLineMatcher.find()) {
            processSeedsLine(line);
        } else if (mapLineMatcher.find()) {
            currentMapFrom = mapLineMatcher.group(1);
            currentMapTo = mapLineMatcher.group(2);
            fromToNames.put(currentMapFrom, currentMapTo);
        } else if (threeNumberPatternMatcher.find()) {
            long destinationRangeStart = Long.parseLong(threeNumberPatternMatcher.group(1));
            long sourceRangeStart = Long.parseLong(threeNumberPatternMatcher.group(2));
            long rangeSize = Long.parseLong(threeNumberPatternMatcher.group(3));
            if (!fromToRangesByName.containsKey(currentMapFrom)) {
                fromToRangesByName.put(currentMapFrom, new TreeMap<>());
            }
            fromToRangesByName.get(currentMapFrom).put(sourceRangeStart,
                    new destinationRangeAndSize(destinationRangeStart, rangeSize));
        }
    }

    private static void processSeedsLine(String line) {
        Matcher twoNumberPatternMatcher = twoNumberPattern.matcher(line);
        while (twoNumberPatternMatcher.find()) {
            long startSeedRange = Long.parseLong(twoNumberPatternMatcher.group(1));
            long seedRangeLength = Long.parseLong(twoNumberPatternMatcher.group(2));
            seeds.add(new mapRange(startSeedRange, seedRangeLength));
        }
    }

    private static Set<mapRange> progressSeedRangesToLocationRanges() {
        String mapFrom = "seed";
        Set<mapRange> fromRangeSet = seeds;
        while (!mapFrom.equals("location")) {
            NavigableMap<Long, destinationRangeAndSize> fromToRanges = fromToRangesByName.get(mapFrom);
            boolean hasFromToRanges = fromToRanges != null;
            Set<mapRange> toRangeSet = new TreeSet<>();

            if (hasFromToRanges) {
                for (mapRange fromMapRange : fromRangeSet) {
                    long fromRangeStart = fromMapRange.rangeStart();
                    long fromRangeSize = fromMapRange.size();
                    Map.Entry<Long, destinationRangeAndSize> closestFromToRange = fromToRanges
                            .floorEntry(fromRangeStart);

                    // Find overlap with the closest preceding map, if there is one
                    if (closestFromToRange != null) {
                        long fromMapStart = closestFromToRange.getKey();
                        long toMapStart = closestFromToRange.getValue().destinationRange();
                        long mapSize = closestFromToRange.getValue().size();

                        long fromOverlapStart = fromRangeStart - fromMapStart;
                        if (fromOverlapStart < mapSize) {
                            // There is overlap; map the overlap to a new range and add it to the toRangeSet
                            long toRangeStart = toMapStart + fromOverlapStart;
                            // Imagine that our range starts at 20 and has size 33, and the map starts at 10
                            // has size 21; if a map at 10 with size 2 has 10,11 then a map at 10 with size
                            // 30
                            // has 10,11,12,13,14,15,16,17,18,19,20,21,...; 10 numbers before the start of
                            // our range so 11 after the start of our range.
                            // In this case our fromdelta is 10 and our to range size is mapSize - fromdelta
                            // = 11.
                            long toRangeSize = Math.min(fromRangeSize, mapSize - fromOverlapStart);
                            toRangeSet.add(new mapRange(toRangeStart, toRangeSize));
                            // Now we remove that range we just added to the to range set from the current
                            // range by increasing the fromRangeStart and decreasing rangeSize;
                            fromRangeStart += toRangeSize;
                            fromRangeSize -= toRangeSize;
                            // Now the leftover range starts at 31 and has size 32
                        }
                    }
                    Map.Entry<Long, destinationRangeAndSize> nextFromToRange = fromToRanges
                            .ceilingEntry(fromRangeStart);
                    boolean hasNextFromToRange = nextFromToRange != null;
                    while (hasNextFromToRange && fromRangeSize > 0) {
                        long fromMapStart = nextFromToRange.getKey();
                        long toMapStart = nextFromToRange.getValue().destinationRange();
                        long mapSize = nextFromToRange.getValue().size();
                        // Assume we overlap; if not then we'll find out in this if statement and update
                        // the boolean
                        boolean overlapsWithNextRange = true;
                        if (fromMapStart > fromRangeStart) {
                            // We've got some range before the next map starts; find out how big that
                            // section of range is and put it in the toRangeSet as-is
                            long distanceBeforeNextMap = fromMapStart - fromRangeStart;
                            if (distanceBeforeNextMap >= fromRangeSize) {
                                overlapsWithNextRange = false;
                                toRangeSet.add(new mapRange(fromRangeStart, fromRangeSize));
                                fromRangeStart += fromRangeSize;
                                fromRangeSize = 0;
                            } else {
                                toRangeSet.add(new mapRange(fromRangeStart, distanceBeforeNextMap));
                                fromRangeStart += distanceBeforeNextMap;
                                fromRangeSize -= distanceBeforeNextMap;
                            }
                        }
                        // Now if we have any range left we map the overlap through to the toRangeSet
                        if (overlapsWithNextRange) {
                            if (fromRangeSize > mapSize) {
                                toRangeSet.add(new mapRange(toMapStart, mapSize));
                                fromRangeStart += mapSize;
                                fromRangeSize -= mapSize;
                            } else {
                                toRangeSet.add(new mapRange(toMapStart, fromRangeSize));
                                fromRangeStart += fromRangeSize;
                                fromRangeSize = 0;
                            }
                        }
                        nextFromToRange = fromToRanges.ceilingEntry(fromRangeStart);
                        hasNextFromToRange = nextFromToRange != null;
                    }
                    // If we have range left then we've run out of maps; add the rest of the range
                    // as-is to the toRangeSet
                    if (fromRangeSize > 0) {
                        toRangeSet.add(new mapRange(fromRangeStart, fromRangeSize));
                        fromRangeStart += fromRangeSize;
                        fromRangeSize = 0;
                    }
                }
                if (!hasFromToRanges) {
                    toRangeSet = fromRangeSet;
                }
                fromRangeSet = toRangeSet;
            }
            mapFrom = fromToNames.get(mapFrom);
        }
        return fromRangeSet;
    }

    public static void main(String[] args) {
        long lowestLocationNumber = Long.MAX_VALUE;
        // Need data structure that returns the next lowest number to the one being
        // searched for
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_05").toPath())) {
            inputLines.forEachOrdered(solver052::processInputLine);
            Set<mapRange> locationRanges = progressSeedRangesToLocationRanges();
            // lowestLocationNumber =
            // seeds.stream().mapToLong(solver052::findLocationNumberForSeed).reduce(Long.MAX_VALUE,
            // (first,second)->first < second ? first : second);
            lowestLocationNumber = locationRanges.stream().mapToLong(range -> range.rangeStart()).reduce(Long.MAX_VALUE,
                    (first, second) -> (first < second ? first : second));

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Lowest location number is: " + lowestLocationNumber);
    }
}
