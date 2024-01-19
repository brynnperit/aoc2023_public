package com.brynnperit.aoc2023.week1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver032 {
    private static Pattern numberPattern = Pattern.compile("([0-9]+)");
    private static Pattern gearPattern = Pattern.compile("\\*");

    private static String[] lines = new String[3];
    private static List<Gear> validGears = new ArrayList<>();

    private record Gear(int num1, int num2){};

    private static void checkLine(String nextLine){
        lines[0]=lines[1];
        lines[1]=lines[2];
        lines[2]=nextLine;
        if (lines[1] != null){
            Matcher gearMatcher = gearPattern.matcher(lines[1]);
            while (gearMatcher.find()){
                int startCoord = Integer.max(gearMatcher.start() - 1, 0);
                int endCoord = Integer.min(gearMatcher.end(), lines[1].length() - 1);
                boolean foundFirstNumber = false;
                int firstNumber = -1;
                boolean foundSecondNumber = false;
                int secondNumber = -1;
                boolean foundMoreThanTwoNumbers = false;
                for (String line : lines){
                    if (line != null && !foundMoreThanTwoNumbers){
                        Matcher numberMatcher = numberPattern.matcher(line);
                        while(numberMatcher.find() && !foundMoreThanTwoNumbers){
                            //Valid numbers:
                            //number start before start && number end after start
                            //number starts before end && number ends after end
                            //Invalid numbers:
                            //number ends before start
                            //number starts after adjacency end
                            //Going to just use the inverse of invalid conditions to determine adjacency
                            //adjacency is starting to look like a fake word
                            boolean numberStopsBeforeStart = (numberMatcher.end()-1) < startCoord;
                            boolean numberStartsAfterEnd = numberMatcher.start() > endCoord;
                            boolean numberAdjacent = !(numberStopsBeforeStart || numberStartsAfterEnd);
                            if (numberAdjacent){
                                if (!foundFirstNumber){
                                    foundFirstNumber = true;
                                    firstNumber = Integer.parseInt(numberMatcher.group());
                                }else if (!foundSecondNumber){
                                    foundSecondNumber = true;
                                    secondNumber = Integer.parseInt(numberMatcher.group());
                                }else{
                                    foundMoreThanTwoNumbers = true;
                                }
                            }
                        }
                    }
                }
                if (foundFirstNumber && foundSecondNumber && !foundMoreThanTwoNumbers){
                    validGears.add(new Gear(firstNumber, secondNumber));
                }
            }
        }
    }
    public static void main(String[] args) {
        int total = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week1/input_03").toPath())) {
            inputLines.forEachOrdered(solver032::checkLine);
            checkLine(null);
            total = validGears.stream().mapToInt(s -> s.num1()*s.num2()).sum();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total is: " + total);
    }

}
