package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WorkResultType implements BiFunction<String, PartRanges, WorkResult<String,PartRanges>> {
    BRANCH(false, 'B'), ACCEPTED(true, 'A'), REJECTED(true, 'R');

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
    public WorkResult<String,PartRanges> apply(String resultString, PartRanges ranges) {
        return new WorkResultPrivate<String,PartRanges>(this, resultString, ranges);
    }

    private class WorkResultPrivate<T,R> implements WorkResult<T,R> {
        private final WorkResultType resultType;
        private final T result;
        private final R rangeResult;

        public WorkResultPrivate(WorkResultType resultType, T result, R rangeResult) {
            this.resultType = resultType;
            this.result = result;
            this.rangeResult = rangeResult;
        }

        @Override
        public WorkResultType resultType() {
            return resultType;
        }

        @Override
        public Optional<T> result() {
            return Optional.ofNullable(result);
        }

        @Override
        public Optional<R> rangeResult() {
            return Optional.ofNullable(rangeResult);
        }
    }
}
