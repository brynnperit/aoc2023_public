package com.brynnperit.aoc2023.week3.solver191;

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
    final private Function<Matcher, WorkStep<String>> workStepCreator;

    private WorkStepType(String patternString,
            Function<Matcher, WorkStep<String>> workStepCreator) {
        this.stepSpecificPattern = Pattern.compile(patternString);
        this.workStepCreator = workStepCreator;
    }

    public static Optional<WorkStep<String>> getWorkStep(String stepString) {
        for (WorkStepType type : WorkStepType.values()) {
            Matcher typeMatcher = type.stepSpecificPattern.matcher(stepString);
            if (typeMatcher.matches()) {
                return Optional.ofNullable(type.workStepCreator.apply(typeMatcher));
            }
        }
        return Optional.ofNullable(null);
    }

    private static class UnconditionalWorkStep implements WorkStep<String> {
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
        public WorkResult<String> apply(Part part) {
            return resultType.apply(branchName);
        }
    }

    private static class ConditionalWorkStep implements WorkStep<String> {
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
        public WorkResult<String> apply(Part part) {
            boolean passedTest = conditionalType.test(part.getRating(partCategory), condition_value);
            if (passedTest) {
                return resultType.apply(branchName);
            } else {
                return WorkResultType.FAILED_TEST.apply(branchName);
            }
        }
    }

    private enum ConditionalType {
        GREATER_THAN('>', (i, j) -> Long.compare(i, j)), LESS_THAN('<', (i, j) -> Long.compare(i, j) * -1);

        private final char symbol;
        private final LongBinaryOperator conditionFunction;

        public char symbol() {
            return symbol;
        }

        private ConditionalType(char symbol, LongBinaryOperator conditionFunction) {
            this.symbol = symbol;
            this.conditionFunction = conditionFunction;
        }

        public boolean test(long first, long second) {
            return conditionFunction.applyAsLong(first, second) > 0;
        }

        private static final Map<Character, ConditionalType> symbolToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<ConditionalType> fromSymbol(char symbol) {
            return Optional.ofNullable(symbolToEnum.get(symbol));
        }
    }
}
