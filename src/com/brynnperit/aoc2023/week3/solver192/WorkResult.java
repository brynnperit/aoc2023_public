package com.brynnperit.aoc2023.week3.solver192;

import java.util.Optional;

public interface WorkResult<T,R> {
    WorkResultType resultType();
    Optional<T> result();
    Optional<R> rangeResult();
}