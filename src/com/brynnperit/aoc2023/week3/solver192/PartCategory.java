package com.brynnperit.aoc2023.week3.solver192;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum PartCategory {
    EXTREMELY_COOL_LOOKING('x'), MUSICAL('m'), AERODYNAMIC('a'), SHINY('s');

    final char symbol;

    public char symbol() {
        return symbol;
    }

    private PartCategory(char symbol) {
        this.symbol = symbol;
    }

    private static final Map<PartCategory, PartCategory> nextCategory = IntStream.range(0, values().length-1).mapToObj(i->values()[i]).collect(Collectors.toMap(c->c, c->values()[c.ordinal()+1]));

    private static final Map<Character, PartCategory> symbolToEnum = Stream.of(values()).collect(Collectors.toMap(c -> c.symbol(), c -> c));

    public Optional<PartCategory> nextCategory(){
        return Optional.ofNullable(nextCategory.get(this));
    }

    public static Optional<PartCategory> fromSymbol(char symbol) {
        return Optional.ofNullable(symbolToEnum.get(symbol));
    }
}
