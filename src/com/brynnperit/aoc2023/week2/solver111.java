package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

public class solver111 {
    private static class GalaxyMap {
        private GalaxyRowColumnList galaxyRows = new GalaxyRowColumnList(GalaxyRowColumnList.Mode.row);
        private GalaxyRowColumnList galaxyColumns = new GalaxyRowColumnList(GalaxyRowColumnList.Mode.column);
        private List<GalaxyCoordinate> galaxyPositions = new ArrayList<>();

        public List<GalaxyCoordinate> addGalaxyRowFromTextLine(String line) {
            List<GalaxyCoordinate> galaxyCoordinates = new ArrayList<>();
            int rowCoordinate = galaxyRows.addNew();
            int columnCoordinate = 0;
            for (char spaceChar : line.toCharArray()) {
                Space space = Space.fromSymbol(spaceChar).get();
                if (space == Space.galaxy) {
                    GalaxyCoordinate newGalaxyCoordinate = new GalaxyCoordinate(rowCoordinate, columnCoordinate);
                    addGalaxy(newGalaxyCoordinate);
                    galaxyCoordinates.add(newGalaxyCoordinate);
                }
                columnCoordinate++;
            }
            return galaxyCoordinates;
        }

        public void addGalaxy(GalaxyCoordinate toAddAt) {
            galaxyRows.addGalaxyAt(toAddAt);
            galaxyColumns.addGalaxyAt(toAddAt);
            galaxyPositions.add(toAddAt);
        }

        private int getDistanceBetweenGalaxies(int firstGalaxyIndex, int secondGalaxyIndex) {
            int distance = 0;
            GalaxyCoordinate firstGalaxyCoordinate = galaxyPositions.get(firstGalaxyIndex);
            GalaxyCoordinate secondGalaxyCoordinate = galaxyPositions.get(secondGalaxyIndex);
            int rowDistance = galaxyRows.getDistanceBetweenGalaxies(firstGalaxyCoordinate, secondGalaxyCoordinate);
            int columnDistance = galaxyColumns.getDistanceBetweenGalaxies(firstGalaxyCoordinate,
                    secondGalaxyCoordinate);
            distance = rowDistance + columnDistance;
            return distance;
        }

        private static class GalaxyRowColumnList {
            private List<GalaxyRowColumn> galaxyRowColumnList = new ArrayList<>();
            private CachedExtraDistanceMap cachedExtraDistanceMap = new CachedExtraDistanceMap();

            private Mode mode;

            private class CachedExtraDistanceMap {
                private NavigableMap<Integer, NavigableMap<Integer, Integer>> distanceMap = new TreeMap<>();
                private boolean hasCachedDistances = false;

                private boolean hasCachedData(int key) {
                    return !distanceMap.get(key).isEmpty();
                }

                private int higherEntry(int key) {
                    int currentKey = key;
                    Entry<Integer, NavigableMap<Integer, Integer>> higher = distanceMap.higherEntry(currentKey);
                    if (higher != null) {
                        return higher.getKey();
                    }
                    return Integer.MAX_VALUE;
                }

                private void addGalaxyAt(int index) {
                    if (!distanceMap.containsKey(index)) {
                        distanceMap.put(index, new TreeMap<Integer, Integer>());
                    }
                }

                private void addDistance(int start, int end, int totalExtraDistance) {
                    hasCachedDistances = true;
                    NavigableMap<Integer, Integer> mapFromStart = distanceMap.get(start);
                    mapFromStart.put(end, totalExtraDistance);

                    // Adding the distance between 0->6, for example, with additional galaxies at 2
                    // and 4 in the middle;
                    // we will already have the 0->2 and 0->4 and 2->4 distance so we need to also
                    // add the 2->6 and 4>-6 distance
                    // 2->6 distance is 0->6 distance - 0->2 distance, 4->6 distance is 2->6
                    // distance - 2->4.
                    // 2 is the higher key below, 0->2 distance is previousBetweenDistance,
                    int distanceLeft = totalExtraDistance;
                    int previousKey = start;
                    int higherKey;
                    Entry<Integer, NavigableMap<Integer, Integer>> higherEntry = distanceMap
                                .higherEntry(previousKey);
                        higherKey = higherEntry.getKey();
                    while (higherKey < end) {
                        if (higherKey <= end) {
                            int previousBetweenDistance = distanceMap.get(previousKey).get(higherKey);
                            distanceLeft -= previousBetweenDistance;
                            higherEntry.getValue().put(end, distanceLeft);
                        }
                        previousKey = higherKey;
                        higherEntry = distanceMap
                                .higherEntry(previousKey);
                        higherKey = higherEntry.getKey();
                    }
                }

                public void clear() {
                    if (hasCachedDistances) {
                        for (int key : distanceMap.keySet()) {
                            distanceMap.get(key).clear();
                        }
                    }
                    hasCachedDistances = false;
                }

                public int floorSecondaryIndex(int startIndex, int endIndex) {
                    Integer floorKey = distanceMap.get(startIndex).floorKey(endIndex);
                    return floorKey == null ? startIndex : floorKey;
                }

                public int cachedDistance(int startIndex, int endIndex) {
                    if (startIndex == endIndex) {
                        return 0;
                    }
                    return distanceMap.get(startIndex).get(endIndex);
                }
            }

            public GalaxyRowColumnList(Mode mode) {
                this.mode = mode;
            }

            public void addGalaxyAt(GalaxyCoordinate toAddAt) {
                cachedExtraDistanceMap.clear();
                int primaryIndex = mode.getPrimaryIndex(toAddAt);
                int secondaryIndex = mode.getSecondaryIndex(toAddAt);
                if (primaryIndex >= galaxyRowColumnList.size()) {
                    while (primaryIndex >= galaxyRowColumnList.size()) {
                        galaxyRowColumnList.add(new GalaxyRowColumn());
                    }
                }
                galaxyRowColumnList.get(primaryIndex).addGalaxyAt(secondaryIndex);
                cachedExtraDistanceMap.addGalaxyAt(primaryIndex);
            }

            public int addNew() {
                galaxyRowColumnList.add(new GalaxyRowColumn());
                return galaxyRowColumnList.size() - 1;
            }

            public int getDistanceBetweenGalaxies(GalaxyCoordinate first, GalaxyCoordinate second) {
                int firstIndex = mode.getPrimaryIndex(first);
                int secondIndex = mode.getPrimaryIndex(second);
                if (secondIndex < firstIndex) {
                    int swap = firstIndex;
                    firstIndex = secondIndex;
                    secondIndex = swap;
                }
                int distance = secondIndex - firstIndex;
                int extraDistance = 0;
                int previousIndex = firstIndex;
                int nextCachedIndex;
                if (cachedExtraDistanceMap.hasCachedData(firstIndex)){
                    nextCachedIndex = firstIndex;
                }else{
                    nextCachedIndex = Math.min(cachedExtraDistanceMap.higherEntry(firstIndex), secondIndex);
                }
                boolean fullyCached = true;
                
                while (nextCachedIndex <= secondIndex) {
                    // Iterate until the next cached jump
                    if (previousIndex < nextCachedIndex) {
                        fullyCached = false;
                        ListIterator<GalaxyRowColumn> iterator = galaxyRowColumnList.listIterator(previousIndex+1);
                        while (iterator.nextIndex() < nextCachedIndex) {
                            if (iterator.next().isEmpty()) {
                                extraDistance++;
                            }
                        }
                        cachedExtraDistanceMap.addDistance(firstIndex, nextCachedIndex, extraDistance);
                    }
                    // Jump as far as we can towards our target index;
                    int farthestJumpIndex = cachedExtraDistanceMap.floorSecondaryIndex(nextCachedIndex,
                            secondIndex);
                    int jumpDistance = farthestJumpIndex - nextCachedIndex;
                    if (jumpDistance > 0) {
                        int extraDistanceFromJump = cachedExtraDistanceMap.cachedDistance(nextCachedIndex,
                                farthestJumpIndex);
                        extraDistance += extraDistanceFromJump;
                        if (!fullyCached) {
                            cachedExtraDistanceMap.addDistance(firstIndex, farthestJumpIndex, extraDistance);
                        }
                    }
                    previousIndex = farthestJumpIndex;
                    if (farthestJumpIndex < secondIndex) {
                        fullyCached = false;
                    }
                    // If there is no cached index afterwards this will return integer.max_value;
                    // either way when we're done this will exceed second index
                    nextCachedIndex = cachedExtraDistanceMap.higherEntry(previousIndex);
                }
                return distance + extraDistance;
            }

            public enum Mode {
                row(coord -> coord.row, coord -> coord.column),
                column(coord -> coord.column, coord -> coord.row);

                private final ToIntFunction<GalaxyCoordinate> primaryIndex;
                private final ToIntFunction<GalaxyCoordinate> secondaryIndex;

                private Mode(ToIntFunction<GalaxyCoordinate> primaryIndex,
                        ToIntFunction<GalaxyCoordinate> secondaryIndex) {
                    this.primaryIndex = primaryIndex;
                    this.secondaryIndex = secondaryIndex;
                }

                public int getPrimaryIndex(GalaxyCoordinate coord) {
                    return primaryIndex.applyAsInt(coord);
                }

                public int getSecondaryIndex(GalaxyCoordinate coord) {
                    return secondaryIndex.applyAsInt(coord);
                }
            }
        }

        private static class GalaxyRowColumn {
            private Set<Integer> galaxyPositions = new TreeSet<>();

            public void addGalaxyAt(int index) {
                galaxyPositions.add(index);
            }

            public boolean isEmpty() {
                return galaxyPositions.isEmpty();
            }
        }

        private record GalaxyCoordinate(int row, int column) {
        }

        private enum Space {
            empty('.'),
            galaxy('#');

            final private char symbol;
            private static final Map<Character, Space> charToEnum = Stream.of(values())
                    .collect(Collectors.toMap(c -> c.symbol(), c -> c));

            public static Optional<Space> fromSymbol(char symbol) {
                return Optional.ofNullable(charToEnum.get(symbol));
            }

            Space(char symbol) {
                this.symbol = symbol;
            }

            public char symbol() {
                return symbol;
            }

        }
    }

    public static void main(String[] args) {
        long galaxyDistanceSum = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_11").toPath())) {
            GalaxyMap map = new GalaxyMap();
            int numberOfGalaxies = inputLines.map(map::addGalaxyRowFromTextLine).mapToInt(list -> list.size()).sum();
            IntStream startRanges = IntStream.range(0, numberOfGalaxies);
            galaxyDistanceSum = startRanges.flatMap(startIndex -> IntStream.range(startIndex + 1, numberOfGalaxies)
                    .map(endIndex -> map.getDistanceBetweenGalaxies(startIndex, endIndex))).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The sum of galaxy distances is " + galaxyDistanceSum);
    }

}
