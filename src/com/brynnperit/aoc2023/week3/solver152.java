package com.brynnperit.aoc2023.week3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class solver152 {
    private static Pattern stepPattern = Pattern.compile("([^,\\n]+)[,\\n]?");
    private static Pattern stepTypePattern = Pattern.compile("[-=]");
    private static Pattern stepAddNumberPattern = Pattern.compile("=([0-9]+)");

    private static record Lens(int focalLength, String label) {
    }

    private static class SequenceStep {
        final String stepString;
        final StepType type;
        final int lensNumber;

        private enum StepType {
            add('=', StepType::addToBoxes),
            remove('-', StepType::removeFromBoxes);

            final private char symbol;
            final private BiConsumer<Map<Integer, List<Lens>>, SequenceStep> boxFunction;
            private static final Map<Character, StepType> charToEnum = Stream.of(values())
                    .collect(Collectors.toMap(c -> c.symbol(), c -> c));

            public static Optional<StepType> fromSymbol(char symbol) {
                return Optional.ofNullable(charToEnum.get(symbol));
            }

            private static void addToBoxes(Map<Integer, List<Lens>> boxes, SequenceStep step) {
                List<Lens> box = boxes.computeIfAbsent(step.hashCode(), i -> new ArrayList<>());
                int replacementIndex = removeFromBox(box, step);
                if (replacementIndex == -1) {
                    box.add(new Lens(step.lensNumber, step.stepString));
                } else {
                    box.add(replacementIndex, new Lens(step.lensNumber, step.stepString));
                }
            }

            private static void removeFromBoxes(Map<Integer, List<Lens>> boxes, SequenceStep step) {
                List<Lens> box = boxes.computeIfAbsent(step.hashCode(), i -> new ArrayList<>());
                removeFromBox(box, step);
            }

            private static int removeFromBox(List<Lens> box, SequenceStep step) {
                ListIterator<Lens> boxIterator = box.listIterator();
                while (boxIterator.hasNext()) {
                    int lensIndex = boxIterator.nextIndex();
                    Lens lens = boxIterator.next();
                    if (lens.label.equals(step.stepString)) {
                        boxIterator.remove();
                        return lensIndex;
                    }
                }
                return -1;
            }

            public void actOnBox(Map<Integer, List<Lens>> box, SequenceStep step) {
                this.boxFunction.accept(box, step);
            }

            StepType(char symbol, BiConsumer<Map<Integer, List<Lens>>, SequenceStep> boxFunction) {
                this.symbol = symbol;
                this.boxFunction = boxFunction;
            }

            public char symbol() {
                return symbol;
            }
        }

        public SequenceStep(String step) {
            Matcher stepTypeMatch = stepTypePattern.matcher(step);
            stepTypeMatch.find();
            this.type = StepType.fromSymbol(stepTypeMatch.group().charAt(0)).get();
            this.stepString = step.substring(0, stepTypeMatch.start());
            if (this.type == StepType.add) {
                Matcher stepAddNumberMatcher = stepAddNumberPattern.matcher(step);
                stepAddNumberMatcher.find();
                lensNumber = Integer.parseInt(stepAddNumberMatcher.group(1));
            } else {
                lensNumber = -1;
            }
        }

        private void actOnBox(Map<Integer, List<Lens>> boxes) {
            type.actOnBox(boxes, this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof SequenceStep))
                return false;
            SequenceStep other = (SequenceStep) o;
            return stepString.equals(other.stepString);
        }

        @Override
        public int hashCode() {
            int currentValue = 0;
            for (char c : stepString.toCharArray()) {
                currentValue += (int) c;
                currentValue *= 17;
                currentValue %= 256;
            }
            return currentValue;
        }
    }

    private static List<SequenceStep> getSteps(String inputLine) {
        List<SequenceStep> allSteps = new ArrayList<>();
        Matcher stepMatcher = stepPattern.matcher(inputLine);
        while (stepMatcher.find()) {
            allSteps.add(new SequenceStep(stepMatcher.group(1)));
        }
        return allSteps;
    }

    private static long getLensPower(int boxNumber, List<Lens> box) {
        long boxLensPower = 0;
        ListIterator<Lens> lensIterator = box.listIterator();
        while (lensIterator.hasNext()) {
            int lensIndex = lensIterator.nextIndex();
            Lens lens = lensIterator.next();
            long lensPower = 1 + boxNumber;
            lensPower *= lensIndex + 1;
            lensPower *= lens.focalLength();
            boxLensPower += lensPower;
        }
        // The focusing power of a single lens is the result of multiplying together:

        // One plus the box number of the lens in question.
        // The slot number of the lens within the box: 1 for the first lens, 2 for the
        // second lens, and so on.
        // The focal length of the lens.
        return boxLensPower;
    }

    public static void main(String[] args) {
        long lensPower = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week3/input_15").toPath())) {
            List<SequenceStep> allSteps = inputLines.map(line -> getSteps(line)).collect(Collectors.toList()).get(0);
            Map<Integer, List<Lens>> lensBoxes = new TreeMap<>();
            allSteps.stream().forEachOrdered(step -> step.actOnBox(lensBoxes));
            lensPower = lensBoxes.entrySet().stream().mapToLong(entry -> getLensPower(entry.getKey(), entry.getValue()))
                    .sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The total lens power is %d%n", lensPower);
    }

}
