package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;
import java.util.stream.*;

public class solver122 {
    private static Pattern recordPattern = Pattern.compile("([0-9]+)");
    private static Pattern springPattern = Pattern.compile("[?.#]");

    private static class SpringRecord {
        private List<Spring> springList = new ArrayList<>();
        private List<Integer> groupSizes = new ArrayList<>();

        public class MutableLockedGroup {
            private int size;
            private int startIndex;

            public MutableLockedGroup(int size) {
                this.size = size;
            }

            public int size() {
                return size;
            }

            public int startIndex() {
                return startIndex;
            }

            public void setStartIndex(int startIndex) {
                this.startIndex = startIndex;
            }

            @Override
            public String toString() {
                return String.format("%d at %d", size, startIndex);
            }
        }

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
                IntStream.range(0, 4).forEach(i -> {
                    springList.add(Spring.unknown);
                    springList.addAll(springListCopy);
                });
                List<Integer> groupSizesCopy = List.copyOf(groupSizes);
                IntStream.range(0, 4).forEach(i -> groupSizes.addAll(groupSizesCopy));
            }
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

        private long resolveUnknowns() {
            List<MutableLockedGroup> lockedGroups = new ArrayList<>(groupSizes.size());
            IntStream.range(0, groupSizes.size())
                    .forEach(i -> lockedGroups.add(new MutableLockedGroup(groupSizes.get(i))));
            return resolveUnknowns(lockedGroups, 0);
        }

        private long resolveUnknowns(List<MutableLockedGroup> lockedGroups, int currentGroup) {
            long toReturn = 0;
            long subsectionSize = 0;
            long subsectionResult = 0;
            int existingNextLowerWindow = -1;

            int lowerWindow = getLowerWindow(0, lockedGroups, currentGroup);

            int higherWindow = getHigherWindow(currentGroup);
            while (lowerWindow <= higherWindow) {
                if (lowerWindow <= higherWindow) {
                    lockedGroups.get(currentGroup).setStartIndex(lowerWindow);
                    if (currentGroup < groupSizes.size() - 1) {
                        int resultingNextLowerWindow = getLowerWindow(lowerWindow, lockedGroups, currentGroup + 1);
                        if (resultingNextLowerWindow != existingNextLowerWindow) {
                            toReturn += subsectionSize * subsectionResult;
                            subsectionSize = 1;
                            subsectionResult = resolveUnknowns(lockedGroups, currentGroup + 1);
                            existingNextLowerWindow = resultingNextLowerWindow;
                        } else {
                            subsectionSize++;
                        }
                    } else {
                        toReturn += 1;
                        // System.out.println(springListToString(writeAppliedSpringList(lockedGroups)));
                    }
                    lowerWindow++;
                }
                lowerWindow = getLowerWindow(lowerWindow, lockedGroups, currentGroup);
            }
            return toReturn + (subsectionSize * subsectionResult);
        }

        private int getLowerWindow(int minLowerWindow, List<MutableLockedGroup> lockedGroups, int currentGroup) {
            int lowerWindow = -1;
            if (currentGroup == 0) {
                lowerWindow = 0;
            } else {
                MutableLockedGroup lastLockedGroup = lockedGroups.get(currentGroup - 1);
                // ###., size is 3, adding 4 puts the window at ###.|
                lowerWindow = lastLockedGroup.startIndex() + lastLockedGroup.size() + 1;
            }
            int normalLowerWindow = lowerWindow;
            lowerWindow = Math.max(lowerWindow, minLowerWindow);
            int windowResult;
            do {
                windowResult = canPlaceGroupAtWindow(lowerWindow, groupSizes.get(currentGroup), currentGroup,
                        normalLowerWindow);
                if (windowResult < 0) {
                    lowerWindow++;
                } else if (windowResult > 0) {
                    lowerWindow += windowResult + 1;
                }
            } while (windowResult != 0 && lowerWindow < springList.size());
            return lowerWindow;
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
                // Group size 3, checking ?.?., so checkposition starts at 2 at ?.|?.
                // We will encounter space when checkposition is windowstart+1 = 1, ?|.?.
                // We want to advance by one so that the next ++ will put us after the space
                // this would return 1
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

        @SuppressWarnings("unused")
        public List<Spring> writeAppliedSpringList(List<MutableLockedGroup> lockedRecords) {
            Iterator<MutableLockedGroup> lockedIterator = lockedRecords.iterator();
            MutableLockedGroup currentLockedGroup = null;
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
            return outputList;
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

    private static AtomicInteger counter = new AtomicInteger(0);

    private static long mapper(int index, List<SpringRecord> allSpringRecords){
        System.out.printf("S%d%n",index+1);
        long startTime = System.nanoTime();
        long result = allSpringRecords.get(index).resolveUnknowns();
        long endTime = System.nanoTime();
        double msTime = (endTime-startTime)/1000000.0;
        int count = counter.incrementAndGet();
        String outputLine = String.format("F%d(%d)(%.3f ms)%n%s%n%d/%d%n",index+1,result,msTime,allSpringRecords.get(index).toString(),count,allSpringRecords.size());
        System.out.print(outputLine);
        try (java.io.BufferedWriter bWriter = Files.newBufferedWriter(new
            File(String.format("outputs/output_122/finished_%03d.txt",index+1)).toPath(),
            java.nio.charset.StandardCharsets.UTF_8)) {
            bWriter.write(outputLine);
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }
    public static void main(String[] args) {
        long possibleCombinations = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_12").toPath())) {
             List<SpringRecord> allSpringRecords = inputLines.map(line -> new SpringRecord(line, true)).collect(Collectors.toList());
            // possibleCombinations = IntStream.range(1, allSpringRecords.size() + 1).mapToLong(i -> {
            //     System.out.print(String.format("%d ", i));
            //     return allSpringRecords.get(i - 1).resolveUnknowns();
            // }).map(l -> {
            //     System.out.println(Long.toString(l));
            //     return l;
            // }).sum();
            possibleCombinations = IntStream.range(0, allSpringRecords.size()).parallel().mapToLong(i->mapper(i,allSpringRecords)).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("There are " + possibleCombinations + " possible configurations");
    }
}