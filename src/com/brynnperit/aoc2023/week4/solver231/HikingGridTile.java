package com.brynnperit.aoc2023.week4.solver231;

import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

public enum HikingGridTile {
    PATH('.', EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),null), 
    FOREST('#',EnumSet.noneOf(Direction.class),null),
    SLOPE_NORTH('^',EnumSet.of(Direction.NORTH),Direction.NORTH.opposite()),
    SLOPE_EAST('>',EnumSet.of(Direction.EAST),Direction.EAST.opposite()),
    SLOPE_SOUTH('v',EnumSet.of(Direction.SOUTH),Direction.SOUTH.opposite()),
    SLOPE_WEST('<',EnumSet.of(Direction.WEST),Direction.WEST.opposite());

    private final char symbol;
    private final Set<Direction> validDirections;
    private final Direction invalidEntryDirection;

    private static final Map<Character, HikingGridTile> charToEnum = Stream.of(values()).collect(toMap(c -> c.symbol(), c -> c));

    public static Optional<HikingGridTile> fromSymbol(char symbol) {
        return Optional.ofNullable(charToEnum.get(symbol));
    }

    public char symbol() {
        return symbol;
    }

    public Set<Direction> validDirections(){
        return validDirections;
    }

    public Direction invalidEntryDirection() {
        return invalidEntryDirection;
    }

    private HikingGridTile(final char symbol, final Set<Direction> validDirections, final Direction invalidEntryDirection) {
        this.symbol = symbol;
        this.validDirections = Collections.unmodifiableSet(validDirections);
        this.invalidEntryDirection = invalidEntryDirection;
    }
}
