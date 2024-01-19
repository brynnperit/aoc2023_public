package com.brynnperit.aoc2023.week1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver042 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)");
    private static Pattern startPossessedRangePattern = Pattern.compile(":");
    private static Pattern startWinningRangePattern = Pattern.compile("\\|");
    private static List<Integer> cardCopies = new ArrayList<>(Arrays.asList(0));

    private static void calculateCardCopies(String line) {
        int winningNumbersCount = 0;

        Set<Integer> winningNumbers = new TreeSet<>();

        Matcher startPossessedRangeMatcher = startPossessedRangePattern.matcher(line);
        startPossessedRangeMatcher.find();
        int startOfPossessedRange = startPossessedRangeMatcher.end();

        Matcher startWinningRangeMatcher = startWinningRangePattern.matcher(line);
        startWinningRangeMatcher.find();
        int startOfWinningRange = startWinningRangeMatcher.end();

        Matcher cardNumberMatcher = numberPattern.matcher(line).region(0, startOfPossessedRange);
        cardNumberMatcher.find();
        int cardNumber = Integer.parseInt(cardNumberMatcher.group());
        
        //Adds the current card to the list of copies
        //We get one copy of the current card by default
        if (cardCopies.size() < cardNumber+1){
            cardCopies.add(1);
        }
        int currentCardCopies = cardCopies.get(cardNumber);

        Matcher winningNumberMatcher = numberPattern.matcher(line).region(startOfWinningRange, line.length());
        while (winningNumberMatcher.find()) {
            winningNumbers.add(Integer.parseInt(winningNumberMatcher.group()));
        }

        Matcher possessedNumberMatcher = numberPattern.matcher(line).region(startOfPossessedRange, startOfWinningRange);
        while (possessedNumberMatcher.find()) {
            int possessedNumber = Integer.parseInt(possessedNumberMatcher.group());
            if (winningNumbers.contains(possessedNumber)) {
                winningNumbersCount++;
            }
        }

        for (int additionalCardIndex = cardNumber+1; additionalCardIndex < cardNumber+1+winningNumbersCount; additionalCardIndex++){
            if (cardCopies.size() < additionalCardIndex+1){
                //Adds the upcoming card to the list of copies
                //We get one copy of every card by default, and having matching numbers gives additional copies of cards
                cardCopies.add(currentCardCopies+1);
            }else{
                cardCopies.set(additionalCardIndex, cardCopies.get(additionalCardIndex)+currentCardCopies);
            }
        }
    }

    public static void main(String[] args) {
        int total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week1/input_04").toPath())) {
            inputLines.forEachOrdered(solver042::calculateCardCopies);
            total = cardCopies.stream().mapToInt(i->i).sum();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }
}
