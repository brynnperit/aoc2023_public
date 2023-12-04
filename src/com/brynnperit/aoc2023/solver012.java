package com.brynnperit.aoc2023;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver012 {
    private static int lineCount = 0;
    private static Pattern filterPattern = Pattern.compile("([0-9]|one|two|three|four|five|six|seven|eight|nine)");

    private static enum digits{
        one(1),
        two(2),
        three(3),
        four(4),
        five(5),
        six(6),
        seven(7),
        eight(8),
        nine(9);
        private final int digit;
        digits(int digit){
            this.digit = digit;
        }
        public int getDigit(){return digit;}
    }
    private static int getDigit(String getDigitFrom){
        int returnDigit = Integer.MAX_VALUE;
        if (getDigitFrom.length() > 1){
            returnDigit = digits.valueOf(getDigitFrom).getDigit();
        }else{
            returnDigit = Integer.parseInt(getDigitFrom);
        }
        return returnDigit;
    }
    private static int processorFunction(String inputLine) {
                int firstDigit = 0;
                int lastDigit = 0;
                Matcher filterMatcher = filterPattern.matcher(inputLine);
                filterMatcher.find();
                int startIndex = filterMatcher.start() + 1;
                firstDigit = getDigit(filterMatcher.group());
                lastDigit = firstDigit;
                while (filterMatcher.find(startIndex)){
                    startIndex = filterMatcher.start() + 1;
                    lastDigit = getDigit(filterMatcher.group());
                }
                int returnValue = firstDigit*10 + lastDigit;
                lineCount++;
                return returnValue;
            }
    public static void main(String[] args) {
        int total = 0;

        try (Stream<String> inputLines = Files.lines(new File("inputs/input_01").toPath())) {
            total = inputLines.mapToInt(solver012::processorFunction).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Line count is: " + lineCount);
        System.out.println("Total is: " + total);
    }

}