package com.brynnperit.aoc2023.week2.solver121;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;
import java.util.stream.*;

public class SpringRecord {
    final private static Pattern recordPattern = Pattern.compile("([0-9]+)");
    final private static Pattern springPattern = Pattern.compile("[?.#]");
    private List<Spring> springList = new ArrayList<>();
    private List<Integer> groupSizes = new ArrayList<>();
    // Group sizes -> positions -> permutations
    private Map<Integer, NavigableMap<Integer, Long>> permutationCounts = new TreeMap<>();
    private Map<Integer, BitSet> possibleGroupLocations = new TreeMap<>();
    private BitSet allRequiredSpringPositions;

    public SpringRecord(String inputLine, boolean expandToFiveTimes) {
        Matcher springMatcher = springPattern.matcher(inputLine);
        while (springMatcher.find()) {
            springList.add(Spring.fromSymbol(springMatcher.group().charAt(0)).get());
        }

        Matcher recordMatcher = recordPattern.matcher(inputLine);
        while (recordMatcher.find()) {
            groupSizes.add(Integer.parseInt(recordMatcher.group()));
        }

        if (expandToFiveTimes) {
            List<Spring> springListCopy = List.copyOf(springList);
            IntStream.range(0, 4).forEachOrdered(i -> {
                springList.add(Spring.unknown);
                springList.addAll(springListCopy);
            });
            List<Integer> groupSizesCopy = List.copyOf(groupSizes);
            IntStream.range(0, 4).forEach(i -> groupSizes.addAll(groupSizesCopy));
        }
        generateGroupPositionCaches();
    }

    private void generateRequiredSpringPositions() {
        allRequiredSpringPositions = new BitSet(springList.size());
        ListIterator<Spring> springIterator = springList.listIterator();
        while (springIterator.hasNext()) {
            int index = springIterator.nextIndex();
            Spring currentSpring = springIterator.next();
            if (currentSpring == Spring.full) {
                allRequiredSpringPositions.set(index);
            }
        }
    }

    private void generateGroupPositionCaches() {
        generateRequiredSpringPositions();
        for (int groupSize : groupSizes) {
            possibleGroupLocations.computeIfAbsent(groupSize, i->populateGroupPositionCaches(groupSize));
        }
    }

    private BitSet populateGroupPositionCaches(int groupSize) {
        BitSet cache = new BitSet(springList.size());
        ListIterator<Spring> springIterator = springList.listIterator();
        int maximumGroupSize = 0;
        int startOfGroupDecrementor = groupSize - 1;
        while (springIterator.hasNext()) {
            int index = springIterator.nextIndex();
            Spring currentSpring = springIterator.next();
            if (currentSpring == Spring.unknown || currentSpring == Spring.full) {
                maximumGroupSize++;
            } else {
                maximumGroupSize = 0;
            }
            if (maximumGroupSize >= groupSize) {
                cache.set(index - startOfGroupDecrementor, canPlaceGroupAtPosition(index - startOfGroupDecrementor, groupSize));
            }
        }
        return cache;
    }

    private boolean canPlaceGroupAtPosition(int position, int groupSize) {
        if (position > 0 && springList.get(position - 1) == Spring.full) {
            return false;
        }
        ListIterator<Spring> springIterator = springList.listIterator(position);
        int continuousPossibleLocations = 0;
        while (springIterator.hasNext() && continuousPossibleLocations < groupSize) {
            Spring currentSpring = springIterator.next();
            if (currentSpring == Spring.empty) {
                return false;
            }
            continuousPossibleLocations++;
        }
        if (continuousPossibleLocations < groupSize) {
            return false;
        }
        if (springIterator.hasNext() && springIterator.next() == Spring.full) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Spring spring : springList) {
            sb.append(spring.symbol());
        }
        sb.append(groupSizes);
        return sb.toString();
    }

    public long getPermutations() {
        return getPermutations(0, 0);
    }

    private Optional<Long> getPermutationsFromCache(int groupIndex, int positionToCheckFrom) {
        NavigableMap<Integer, Long> cache = permutationCounts.computeIfAbsent(groupIndex, i -> new TreeMap<>());
        Entry<Integer, Long> count = cache.ceilingEntry(positionToCheckFrom);
        if (count != null) {
            return Optional.of(count.getValue());
        }
        return Optional.empty();
    }

    private void setPermutationsInCache(int groupIndex, int position, long count) {
        permutationCounts.computeIfAbsent(groupIndex, i -> new TreeMap<>()).put(position, count);
    }

    private long getPermutations(int currentGroupIndex, int positionToCheckFrom) {
        if (positionToCheckFrom >= springList.size()) {
            return 0;
        }
        Optional<Long> possiblePermutations = getPermutationsFromCache(currentGroupIndex, positionToCheckFrom);
        if (possiblePermutations.isPresent()) {
            return possiblePermutations.get();
        }
        int groupSize = groupSizes.get(currentGroupIndex);
        BitSet possibleLocations = possibleGroupLocations.get(groupSize);
        int lastPossibleLocation = allRequiredSpringPositions.nextSetBit(positionToCheckFrom);
        if (lastPossibleLocation == -1) {
            lastPossibleLocation = springList.size();
        }
        long permutationSum = 0;
        if (currentGroupIndex < groupSizes.size() - 1) {
            for (int index = possibleLocations.nextSetBit(positionToCheckFrom); index >= 0 && index <= lastPossibleLocation; index = possibleLocations.nextSetBit(index + 1)) {
                permutationSum += getPermutations(currentGroupIndex + 1, index + groupSize + 1);
            }
        } else {
            for (int index = possibleLocations.nextSetBit(positionToCheckFrom); index >= 0 && index <= lastPossibleLocation; index = possibleLocations.nextSetBit(index + 1)) {
                // This is the last group; if there are springs denoted as being present in the
                // pattern that aren't covered by this group then there are no possible permutations.
                if (allRequiredSpringPositions.nextSetBit(index + groupSize) == -1) {
                    permutationSum += 1;
                }
            }
        }
        setPermutationsInCache(currentGroupIndex, positionToCheckFrom, permutationSum);
        return permutationSum;
    }
}