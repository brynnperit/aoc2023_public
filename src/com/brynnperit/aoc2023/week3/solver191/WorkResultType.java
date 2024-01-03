package com.brynnperit.aoc2023.week3.solver191;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WorkResultType implements Function<String, WorkResult<String>> {
    FAILED_TEST(false, 'F'), BRANCH(false, 'B'), ACCEPTED(true, 'A'), REJECTED(true, 'R');

    final char symbol;

    public char symbol() {
        return symbol;
    }

    private static final Map<Character, WorkResultType> symbolToEnum = Stream.of(values())
            .collect(Collectors.toMap(c -> c.symbol(), c -> c));

    public static Optional<WorkResultType> fromSymbol(char symbol) {
        return Optional.ofNullable(symbolToEnum.get(symbol));
    }

    final private boolean isFinalResult;

    public boolean isFinalResult() {
        return isFinalResult;
    }

    private WorkResultType(boolean isFinalResult, char symbol) {
        this.isFinalResult = isFinalResult;
        this.symbol = symbol;
    }

    @Override
    public WorkResult<String> apply(String resultString) {
        return new WorkResultPrivate<String>(this, resultString);
    }

    private class WorkResultPrivate<T> implements WorkResult<T> {
        private final WorkResultType resultType;
        private final T result;

        public WorkResultPrivate(WorkResultType resultType, T result) {
            this.resultType = resultType;
            this.result = result;
        }

        @Override
        public WorkResultType resultType() {
            return resultType;
        }

        @Override
        public Optional<T> result() {
            return Optional.ofNullable(result);
        }
    }
}
