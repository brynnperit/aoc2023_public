package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import java.util.function.*;

public enum WorkStepType {
    CONDITIONAL_BRANCH("^([a-z])(.)([0-9]+):([a-z]+)$", m -> new ConditionalWorkStep(m,m.group(4),s -> WorkResultType.BRANCH)),
    CONDITIONAL_RESULT("^([a-z])(.)([0-9]+):([A-Z])$", m -> new ConditionalWorkStep(m,null,s -> WorkResultType.fromSymbol(s.charAt(0)).get())),
    UNCONDITIONAL_BRANCH("^([a-z]+)$", m -> new UnconditionalWorkStep(m)),
    UNCONDITIONAL_RESULT("^([A-Z])$", m -> new UnconditionalWorkStep(m));

    final private Pattern stepSpecificPattern;
    final private Function<Matcher, WorkStep<String,PartRanges>> workStepCreator;

    private WorkStepType(String patternString,
            Function<Matcher, WorkStep<String,PartRanges>> workStepCreator) {
        this.stepSpecificPattern = Pattern.compile(patternString);
        this.workStepCreator = workStepCreator;
    }

    public static Optional<WorkStep<String,PartRanges>> getWorkStep(String stepString) {
        for (WorkStepType type : WorkStepType.values()) {
            Matcher typeMatcher = type.stepSpecificPattern.matcher(stepString);
            if (typeMatcher.matches()) {
                return Optional.ofNullable(type.workStepCreator.apply(typeMatcher));
            }
        }
        return Optional.ofNullable(null);
    }

    private static class UnconditionalWorkStep implements WorkStep<String,PartRanges> {
        final private String branchName;
        final private WorkResultType resultType;

        public UnconditionalWorkStep(Matcher matcher) {
            String text = matcher.group(1);
            Optional<WorkResultType> possibleType = WorkResultType.fromSymbol(text.charAt(0));
            if (possibleType.isPresent()) {
                this.branchName = null;
                this.resultType = possibleType.get();
            } else {
                this.branchName = text;
                this.resultType = WorkResultType.BRANCH;
            }
        }

        @Override
        public WorkResult<String, PartRanges> apply(PartRanges part) {
            return resultType.apply(branchName, null);
        }
    }

    private static class ConditionalWorkStep implements WorkStep<String,PartRanges> {
        private PartCategory partCategory;
        private ConditionalType conditionalType;
        private long condition_value;
        private String branchName;
        private WorkResultType resultType;

        public ConditionalWorkStep(Matcher matcher, String branchName, Function<String, WorkResultType> resultTypeGetter) {
            this.partCategory = PartCategory.fromSymbol(matcher.group(1).charAt(0)).get();
            this.conditionalType = ConditionalType.fromSymbol(matcher.group(2).charAt(0)).get();
            this.condition_value = Long.parseLong(matcher.group(3));
            this.branchName = branchName;
            this.resultType = resultTypeGetter.apply(matcher.group(4));
        }

        @Override
        public WorkResult<String,PartRanges> apply(PartRanges partRanges) {
            Range updatedRange;
            updatedRange = conditionalType.applyRange(partRanges.getRange(partCategory), condition_value);
            PartRanges newRanges = new PartRanges(partRanges);
            newRanges.setRange(partCategory, updatedRange);
            return resultType.apply(branchName, newRanges);
        }
    }

    private enum ConditionalType {
        GREATER_THAN('>', (range,number)->new Range(Math.max(number+1, range.low()),range.high())), 
        LESS_THAN('<', (range,number)->new Range(range.low(),Math.min(number-1, range.high())));

        private final char symbol;
        private final BiFunction<Range,Long,Range> conditionFunction;

        public char symbol() {
            return symbol;
        }

        private ConditionalType(char symbol, BiFunction<Range,Long,Range> conditionFunction) {
            this.symbol = symbol;
            this.conditionFunction = conditionFunction;
        }

        public Range applyRange(Range original, long number) {
            return conditionFunction.apply(original, number);
        }

        private static final Map<Character, ConditionalType> symbolToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<ConditionalType> fromSymbol(char symbol) {
            return Optional.ofNullable(symbolToEnum.get(symbol));
        }
    }
}
