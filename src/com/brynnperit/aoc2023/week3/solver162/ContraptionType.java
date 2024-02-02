package com.brynnperit.aoc2023.week3.solver162;

import java.util.function.*;
import java.util.*;
import java.util.stream.*;

public enum ContraptionType {
    MIRROR_FRONT('/',
            dir -> (dir == Direction.EAST || dir == Direction.WEST) ? EnumSet.of(dir.previous())
                    : EnumSet.of(dir.next())),
    MIRROR_BACK('\\',
            dir -> (dir == Direction.EAST || dir == Direction.WEST) ? EnumSet.of(dir.next())
                    : EnumSet.of(dir.previous())),
    SPLITTER_VERT('|',
            dir -> (dir == Direction.EAST || dir == Direction.WEST) ? EnumSet.of(Direction.NORTH, Direction.SOUTH)
                    : EnumSet.of(dir)),
    SPLITTER_HORI('-',
            dir -> (dir == Direction.NORTH || dir == Direction.SOUTH) ? EnumSet.of(Direction.EAST, Direction.WEST)
                    : EnumSet.of(dir)),
    SPACE('.', dir -> EnumSet.noneOf(Direction.class));

    final private char symbol;
    final private Function<Direction, EnumSet<Direction>> resultingFunction;
    private static final Map<Character, ContraptionType> charToEnum = Stream.of(values())
            .collect(Collectors.toMap(c -> c.symbol(), c -> c));

    public static Optional<ContraptionType> fromSymbol(char symbol) {
        return Optional.ofNullable(charToEnum.get(symbol));
    }

    public EnumSet<Direction> GetDirections(Direction initial) {
        return this.resultingFunction.apply(initial);
    }

    ContraptionType(char symbol, Function<Direction, EnumSet<Direction>> resultingFunction) {
        this.symbol = symbol;
        this.resultingFunction = resultingFunction;
    }

    public char symbol() {
        return symbol;
    }
}