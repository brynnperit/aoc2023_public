package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class solver131 {
    private static Pattern ashRockPattern = Pattern.compile("[#.]");
    private static List<AshRockField> ashRockFields = new ArrayList<>();

    private enum AshRock {
        ash('.'),
        rock('#');

        final private char symbol;
        private static final Map<Character, AshRock> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<AshRock> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        AshRock(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }
    }

    private static class AshRockField {
        private List<List<AshRock>> rows;
        private List<List<AshRock>> columns = new ArrayList<>();

        public AshRockField(List<AshRock> row) {
            int rowLength = row.size();
            this.rows = new ArrayList<>();
            this.rows.add(row);
            IntStream.range(0, rowLength).forEach(i -> columns.add(new ArrayList<>()));
            IntStream.range(0, rowLength).forEach(i -> columns.get(i).add(row.get(i)));
        }

        public void addRow(List<AshRock> row) {
            int rowLength = rows.get(0).size();
            this.rows.add(row);
            IntStream.range(0, rowLength).forEach(i -> columns.get(i).add(row.get(i)));
        }

        public int getFirstMirrorColumn() {
            return findFirstMirrorNumber(columns);
        }

        public int getFirstMirrorRow() {
            return findFirstMirrorNumber(rows);
        }

        private record RowPair(int firstRow, int secondRow) implements Comparable<RowPair> {
            @Override
            public int compareTo(RowPair otherPair) {
                int result = Integer.compare(firstRow + secondRow, otherPair.firstRow + otherPair.secondRow);
                if (result == 0) {
                    result = Integer.compare(firstRow, otherPair.firstRow);
                    if (result == 0) {
                        result = Integer.compare(secondRow, otherPair.secondRow);
                    }
                }
                return result;
            }

            @Override
            public String toString() {
                return String.format("[%d,%d]", firstRow, secondRow);
            }
        }

        private int findFirstMirrorNumber(List<List<AshRock>> rows) {
            // Put each row into a HashMap<List<AshRock>,List<Integer>> where if there's
            // already an entry then we add 1 to the list,
            // otherwise make a new entry. Get the difference between the entries in the
            // 2-element integer lists; if they form a complete group
            // from 1->whatever and at least one of the entries is the maximum or minimum of
            // the outer list then there's a mirror there.
            int firstNumber = -1;
            Map<List<AshRock>, List<Integer>> rowMap = new HashMap<>();
            ListIterator<List<AshRock>> rowIterator = rows.listIterator();
            while (rowIterator.hasNext()) {
                int currentIndex = rowIterator.nextIndex();
                List<AshRock> currentRow = rowIterator.next();
                rowMap.computeIfAbsent(currentRow, row -> new ArrayList<>()).add(currentIndex);
            }
            List<List<Integer>> rowMultiples = rowMap.values().stream().filter(list -> list.size() >= 2)
                    .collect(Collectors.toList());
            List<RowPair> rowPairs = new ArrayList<>();
            rowMultiples.stream().forEach(list -> getPairsFromList(list, rowPairs));
            Collections.sort(rowPairs);

            List<Integer> rowDifferences = rowPairs.stream().map(pair -> pair.secondRow - pair.firstRow)
                    .collect(Collectors.toList());

            List<List<RowPair>> rowPairSequences = getRowPairSequences(rowPairs, rowDifferences);

            Predicate<List<RowPair>> containsZero = pairList -> pairList.stream()
                    .flatMapToInt(pair -> IntStream.of(pair.firstRow(), pair.secondRow())).anyMatch(i -> i == 0);
            List<List<RowPair>> leftSideSequences = rowPairSequences.stream().filter(containsZero)
                    .collect(Collectors.toList());
            if (leftSideSequences.size() == 1) {
                List<RowPair> leftSideSequence = leftSideSequences.get(0);
                firstNumber = leftSideSequence.get(leftSideSequence.size() - 1).firstRow();
            } else {
                Predicate<List<RowPair>> containsMax = pairList -> pairList.stream()
                        .flatMapToInt(pair -> IntStream.of(pair.firstRow(), pair.secondRow()))
                        .anyMatch(i -> i == rows.size() - 1);
                List<List<RowPair>> rightSideSequences = rowPairSequences.stream().filter(containsMax)
                        .collect(Collectors.toList());
                if (rightSideSequences.size() == 1) {
                    List<RowPair> rightSideSequence = rightSideSequences.get(0);
                    firstNumber = rightSideSequence.get(rightSideSequence.size() - 1).firstRow();
                }
            }
            return firstNumber;
        }

        private List<List<RowPair>> getRowPairSequences(List<RowPair> rowPairs, List<Integer> rowDifferences) {
            List<List<RowPair>> sequences = new ArrayList<>();
            List<Integer> finalDifferences = new ArrayList<>();
            Iterator<RowPair> pairIterator = rowPairs.iterator();
            Iterator<Integer> differenceIterator = rowDifferences.iterator();
            List<RowPair> currentSequence = new ArrayList<>();
            sequences.add(currentSequence);
            int previousDifference = Integer.MAX_VALUE;
            while (pairIterator.hasNext()) {
                RowPair currentPair = pairIterator.next();
                int currentDifference = differenceIterator.next();
                if (previousDifference != Integer.MAX_VALUE) {
                    if (currentDifference != previousDifference - 2) {
                        finalDifferences.add(previousDifference);
                        currentSequence = new ArrayList<>();
                        sequences.add(currentSequence);
                    }
                }
                previousDifference = currentDifference;
                currentSequence.add(currentPair);
            }
            finalDifferences.add(previousDifference);
            for (int sequenceIndex = sequences.size() - 1; sequenceIndex >= 0; sequenceIndex--) {
                if (finalDifferences.get(sequenceIndex) != 1) {
                    sequences.remove(sequenceIndex);
                }
            }
            return sequences;
        }

        private void getPairsFromList(List<Integer> rowIndexList, List<RowPair> rowPairs) {
            ListIterator<Integer> outerRowIterator = rowIndexList.listIterator();
            while (outerRowIterator.hasNext()) {
                int outerRowListIndex = outerRowIterator.nextIndex();
                int outerRowIndex = outerRowIterator.next();
                ListIterator<Integer> innerRowIterator = rowIndexList.listIterator(outerRowListIndex + 1);
                while (innerRowIterator.hasNext()) {
                    rowPairs.add(new RowPair(outerRowIndex, innerRowIterator.next()));
                }
            }
        }
    }

    private static AshRockField fieldInProgress = null;

    private static void processLine(String line) {
        Matcher ashRockMatcher = ashRockPattern.matcher(line);
        boolean hasInput = false;
        List<AshRock> rowInProgress = null;
        while (ashRockMatcher.find()) {
            hasInput = true;
            if (rowInProgress == null) {
                rowInProgress = new ArrayList<>();
            }
            rowInProgress.add(AshRock.fromSymbol(ashRockMatcher.group().charAt(0)).get());
        }
        if (hasInput) {
            if (fieldInProgress == null) {
                fieldInProgress = new AshRockField(rowInProgress);
                ashRockFields.add(fieldInProgress);
            } else {
                fieldInProgress.addRow(rowInProgress);
            }
        } else {
            fieldInProgress = null;
        }
    }

    public static void main(String[] args) {
        long summaryNumber = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_13").toPath())) {
            inputLines.forEachOrdered(line -> processLine(line));
            List<Integer> rowResults = ashRockFields.stream().map(field -> field.getFirstMirrorRow()).toList();
            List<Integer> columnResults = ashRockFields.stream().map(field -> field.getFirstMirrorColumn()).toList();
            List<Integer> rowAmounts = rowResults.stream().map(i -> (i + 1) * 100).toList();
            List<Integer> columnAmounts = columnResults.stream().map(i -> (i + 1)).toList();
            IntStream.range(0, rowResults.size()).forEach(i -> System.out.printf("%d: [%d,%d]=[%d,%d]%n", i + 1,
                    rowResults.get(i), columnResults.get(i), rowAmounts.get(i), columnAmounts.get(i)));
            summaryNumber = rowAmounts.stream().mapToInt(i -> i).sum() + columnAmounts.stream().mapToInt(i -> i).sum();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The summary number is %d%n", summaryNumber);
    }
}
