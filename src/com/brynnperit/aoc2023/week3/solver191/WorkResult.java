package com.brynnperit.aoc2023.week3.solver191;

import java.util.Optional;

public interface WorkResult<T> {
    WorkResultType resultType();
    Optional<T> result();
}