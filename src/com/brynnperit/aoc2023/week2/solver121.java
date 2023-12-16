package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class solver121 {
    private static Pattern recordPattern = Pattern.compile("([0-9]+)");
    private static Pattern springPattern = Pattern.compile("[?.#]");

    private static class SpringRecord {
        private List<Spring> springList = new ArrayList<>();
        private List<Integer> groupSizes = new ArrayList<>();

        private record LockedGroup(int size, int startIndex) {
            @Override
            public String toString() {
                return String.format("%d at %d", size, startIndex);
            }
        }

        public SpringRecord(String inputLine) {
            Matcher springMatcher = springPattern.matcher(inputLine);
            while (springMatcher.find()) {
                springList.add(Spring.fromSymbol(springMatcher.group().charAt(0)).get());
            }
            Matcher recordMatcher = recordPattern.matcher(inputLine);
            while (recordMatcher.find()) {
                groupSizes.add(Integer.parseInt(recordMatcher.group()));
            }
        }

        public static int sortSpringLists(List<Spring> first, List<Spring> second) {
            int compareValue = 0;
            compareValue = Integer.compare(first.size(), second.size());
            if (compareValue == 0) {
                Iterator<Spring> firstIterator = first.iterator();
                Iterator<Spring> secondIterator = second.iterator();
                while (compareValue == 0 && firstIterator.hasNext()) {
                    compareValue = firstIterator.next().compareTo(secondIterator.next());
                }
            }
            return compareValue;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            addSpringListToStringBuilder(sb, springList);
            sb.append(groupSizes);
            return sb.toString();
        }

        private static void addSpringListToStringBuilder(StringBuilder sb, List<Spring> springList) {
            // sb.append('(');
            for (Spring spring : springList) {
                sb.append(spring.symbol());
            }
            // sb.append(')');
        }

        @SuppressWarnings("unused")
        public static String springListToString(List<Spring> springList) {
            StringBuilder sb = new StringBuilder();
            addSpringListToStringBuilder(sb, springList);
            return sb.toString();
        }

        private Set<List<Spring>> resolveUnknowns(Set<List<Spring>> validPlacements) {
            resolveUnknownsWithLockedGroups(validPlacements, new ArrayList<>(5));
            return validPlacements;
        }

        private void resolveUnknownsWithLockedGroups(Set<List<Spring>> validPlacements,
                List<LockedGroup> lockedRecords) {
            int currentGroup = lockedRecords.size();
            if (currentGroup == groupSizes.size()) {
                writeAppliedSpringListToSet(validPlacements, lockedRecords);
            } else {
                int lowerWindow = getLowerWindow(lockedRecords);
                int higherWindow = getHigherWindow(currentGroup);
                int currentGroupSize = groupSizes.get(currentGroup);
                for (int currentWindow = lowerWindow; currentWindow <= higherWindow; currentWindow++) {
                    int windowMustAdvance = canPlaceGroupAtWindow(currentWindow, currentGroupSize, currentGroup,
                            lowerWindow);
                    if (windowMustAdvance == 0) {
                        lockedRecords.add(new LockedGroup(currentGroupSize, currentWindow));
                        resolveUnknownsWithLockedGroups(validPlacements, lockedRecords);
                        lockedRecords.remove(lockedRecords.size() - 1);
                    } else {
                        if (windowMustAdvance > 0) {
                            currentWindow += windowMustAdvance;
                        }
                    }
                }
            }
        }

        // Must be a space or a ? one after the edge of the group, group must contain
        // only springs or ?, must be a space or ? before the
        // edge of the group
        /**
         * 
         * @param currentWindowStart
         * @param currentGroupSize
         * @param currentGroup
         * @return Anything other than a 0 means invalid position; positive numbers mean
         *         advance that many positions forward.
         */
        private int canPlaceGroupAtWindow(int currentWindowStart, int currentGroupSize, int currentGroup,
                int lowerWindow) {
            int checkPosition;
            if (currentGroup != groupSizes.size() - 1) {
                // Imagine that we're checking, for a group of size 3, ???#. first check is at
                // ???|#.
                // Returning -1 will put the window at |???#., then the loop's ++ will put us
                // one after which is potentially valid
                checkPosition = currentWindowStart + currentGroupSize;
                if (checkPosition < springList.size() && springList.get(checkPosition) == Spring.full) {
                    return -1;
                }
            } else {
                // This is the last group, no # are allowed after it.
                for (checkPosition = springList.size() - 1; checkPosition >= currentWindowStart
                        + currentGroupSize; checkPosition--) {
                    if (springList.get(checkPosition) == Spring.full) {
                        int returnOffset = (checkPosition - (currentGroupSize + currentWindowStart + 1));
                        if (returnOffset == 0) {
                            returnOffset = -1;
                        }
                        return returnOffset;
                    }
                }
            }
            checkPosition = currentWindowStart + currentGroupSize - 1;
            if (checkPosition < springList.size()) {
                // Group size 3, checking ?.?., so checkposition starts at ?.|?.
                // We will encounter space when checkposition is windowstart+1, ?|.?.
                // We want to advance by one so that the next ++ will put us after the space
                for (; checkPosition >= currentWindowStart; checkPosition--) {
                    if (springList.get(checkPosition) == Spring.empty) {
                        int returnOffset = checkPosition - currentWindowStart;
                        if (returnOffset == 0) {
                            returnOffset = -1;
                        }
                        return returnOffset;
                    }
                }
            } else {
                return springList.size();
            }
            for (checkPosition = currentWindowStart - 1; checkPosition >= lowerWindow; checkPosition--) {
                if (springList.get(checkPosition) == Spring.full) {
                    return springList.size();
                }
            }
            return 0;
        }

        private int getHigherWindow(int currentGroup) {
            int higherWindow = springList.size() - 1;
            int groupsAfterCurrent = (groupSizes.size() - 1) - currentGroup;
            // Account for minimum one space for each group after ours
            higherWindow -= groupsAfterCurrent;
            // Account for the size of each group after ours
            for (int groupIndex = currentGroup + 1; groupIndex < groupSizes.size(); groupIndex++) {
                higherWindow -= groupSizes.get(groupIndex);
            }
            return higherWindow;
        }

        private void writeAppliedSpringListToSet(Set<List<Spring>> validPlacements, List<LockedGroup> lockedRecords) {
            Iterator<LockedGroup> lockedIterator = lockedRecords.iterator();
            LockedGroup currentLockedGroup = null;
            boolean writingCurrentLockedGroup = true;
            int lockedLeft = 0;
            List<Spring> outputList = new ArrayList<>();
            for (int springIndex = 0; springIndex < springList.size(); springIndex++) {
                while (lockedLeft == 0) {
                    if (writingCurrentLockedGroup) {
                        // lockedLeft hit 0 because it just finished writing the current group (or
                        // because we just started);
                        if (lockedIterator.hasNext()) {
                            currentLockedGroup = lockedIterator.next();
                            lockedLeft = -(currentLockedGroup.startIndex - springIndex);
                        } else {
                            lockedLeft = -springList.size();
                        }
                        writingCurrentLockedGroup = false;
                    } else {
                        // lockedLeft hit 0 because it is now ready to start writing the current group
                        lockedLeft = currentLockedGroup.size();
                        writingCurrentLockedGroup = true;
                    }
                }
                if (lockedLeft < 0) {
                    Spring currentSpring = springList.get(springIndex);
                    if (currentSpring == Spring.full) {
                        StringBuilder outputListSB = new StringBuilder(springListToString(outputList));
                        outputListSB.insert(springIndex, '|');
                        StringBuilder thisSB = new StringBuilder(this.toString());
                        thisSB.insert(springIndex, '|');
                        System.err.println(String.format(
                                "Unaccounted for # in applied spring at index %d: Locked Groups:%n%s%nOutput List:%s%nThis:%n%s",
                                springIndex, lockedRecords.toString(), outputListSB.toString(),
                                thisSB.toString()));
                    }
                    lockedLeft++;
                    outputList.add(currentSpring);
                } else {
                    lockedLeft--;
                    outputList.add(Spring.full);
                }
            }
            validPlacements.add(outputList);
        }

        private int getLowerWindow(List<LockedGroup> lockedGroups) {
            if (lockedGroups.size() == 0) {
                return 0;
            } else {
                LockedGroup lastLockedGroup = lockedGroups.get(lockedGroups.size() - 1);
                // ###., size is 3, adding 4 puts the window at ###.|
                return lastLockedGroup.startIndex + lastLockedGroup.size + 1;
            }
        }
    }

    private enum Spring {
        full('#'),
        empty('.'),
        unknown('?');

        final private char symbol;
        private static final Map<Character, Spring> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Spring> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        Spring(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }
    }

    @SuppressWarnings("unused")
    private static void fillOutputSB(int index, SpringRecord record, StringBuffer outputSB) {
        outputSB.append(String.format("%d %s %n", index, record.toString()));
        Set<List<Spring>> possibilities = record.resolveUnknowns(new TreeSet<>(SpringRecord::sortSpringLists));
        int possibilityNumber = 1;
        for (List<Spring> possibility : possibilities) {
            outputSB.append(String.format("%s %d%n", SpringRecord.springListToString(possibility), possibilityNumber));
            possibilityNumber++;
        }
        outputSB.append(String.format("Permutations: %d%n", possibilities.size()));
    }

    public static void main(String[] args) {
        int possibleCombinations = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_12").toPath())) {
            // // These lines send output to a file for debugging
            // StringBuffer outputSB = new StringBuffer();
            // List<SpringRecord> allRecords = inputLines.map(line -> new
            // SpringRecord(line)).collect(Collectors.toList());
            // IntStream.range(1, allRecords.size()).forEach(i -> fillOutputSB(i,
            // allRecords.get(i - 1), outputSB));
            

            // These lines just give the answer directly
            Stream<SpringRecord> allSpringRecords = inputLines.map(line -> new SpringRecord(line));
            Stream<Set<List<Spring>>> allPossibilitiesForSpringRecords = allSpringRecords
                    .map(sr -> sr.resolveUnknowns(new HashSet<>()));
            possibleCombinations = allPossibilitiesForSpringRecords.mapToInt(set -> set.size()).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("There are " + possibleCombinations + " possible configurations");
    }
}