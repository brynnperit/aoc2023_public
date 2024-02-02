package com.brynnperit.aoc2023.week3.solver152;

import java.util.function.*;
import java.util.*;
import java.util.stream.*;

public enum StepType {
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
            if (lens.label().equals(step.stepString)) {
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