package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class solver092 {
    private static Pattern signedIntegerPattern = Pattern.compile("-?[0-9]+");

    private static void extendDifferenceListsByOneBackwards(List<List<Long>> differenceListList){
        List<Long> lastList = differenceListList.get(differenceListList.size()-1);
        lastList.add(0,0L);
        List<Long> lowerList = lastList;
        for (int currentListIndex = differenceListList.size()-2;currentListIndex>=0; currentListIndex--){
            List<Long> currentList = differenceListList.get(currentListIndex);
            currentList.add(0, currentList.get(0)-lowerList.get(0));
            lowerList = currentList;
        }
    }

    private static List<List<Long>> getDifferencesUntilZeros(List<Long> numbers){
        List<List<Long>> differences = new ArrayList<>();
        List<Long> currentNumbers = numbers;
        differences.add(currentNumbers);
        while (!allNumbersZero(currentNumbers)){
            List<Long> currentNumbersUnmodifiable = Collections.unmodifiableList(currentNumbers);
            List<Long> nextNumbers = IntStream.range(0, currentNumbers.size()-1).mapToObj(index->currentNumbersUnmodifiable.get(index+1)-currentNumbersUnmodifiable.get(index)).collect(Collectors.toList());
            differences.add(nextNumbers);
            currentNumbers = nextNumbers;
        }
        return differences;
    }
    
    private static boolean allNumbersZero(List<Long> currentNumbers) {
        return currentNumbers.stream().mapToLong(i->i).filter(i->i!=0).count() == 0;
    }

    private static List<Long> getNumbersFromLine(String line){
        List<Long> numbers = new ArrayList<>();
        Matcher signedIntegerMatcher = signedIntegerPattern.matcher(line);
        while (signedIntegerMatcher.find()){
            long currentNumber = Long.parseLong(signedIntegerMatcher.group());
            numbers.add(currentNumber);
        }
        return numbers;
    }

    public static void main(String[] args) {
        long totalPredictions = -1;
        try(Stream<String> inputLines = Files.lines(new File("inputs/input_09").toPath())){
            List<List<List<Long>>> allDifferences = inputLines.map(solver092::getNumbersFromLine).map(solver092::getDifferencesUntilZeros).collect(Collectors.toList());
            allDifferences.forEach(listList->extendDifferenceListsByOneBackwards(listList));
            totalPredictions = allDifferences.stream().map(listList->listList.get(0)).mapToLong(firstList->firstList.get(0)).sum();
            //allDifferences.forEach(list->{list.forEach(System.out::println);System.out.println();;});
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Total of predicted previous values is: " + totalPredictions);
    }
    
}
