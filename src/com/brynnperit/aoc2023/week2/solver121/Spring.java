package com.brynnperit.aoc2023.week2.solver121;

import java.util.*;
import java.util.stream.*;

public enum Spring {
    full('#'),
    empty('.'),
    unknown('?');

    final private char symbol;
    private static final Map<Character, Spring> charToEnum = Stream.of(values())
            .collect(Collectors.toMap(c -> c.symbol(), c -> c));

    public static Optional<Spring> fromSymbol(char symbol) {
        return Optional.ofNullable(charToEnum.get(symbol));
    }

    Spring(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }
}