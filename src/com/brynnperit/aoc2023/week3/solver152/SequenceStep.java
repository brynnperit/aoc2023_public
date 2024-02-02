package com.brynnperit.aoc2023.week3.solver152;

import java.util.*;
import java.util.regex.*;

public class SequenceStep {
    
    private static final Pattern stepTypePattern = Pattern.compile("[-=]");
    private static final Pattern stepAddNumberPattern = Pattern.compile("=([0-9]+)");

    final String stepString;
    final StepType type;
    final int lensNumber;

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

    public void actOnBox(Map<Integer, List<Lens>> boxes) {
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
