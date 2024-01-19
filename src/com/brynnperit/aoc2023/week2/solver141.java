package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class solver141 {
    private static class Coord2D {
        private int x;
        private int y;

        public Coord2D(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coord2D(Coord2D other) {
            this.x = other.x;
            this.y = other.y;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public void go(Direction toGo) {
            x = toGo.goX(x);
            y = toGo.goY(y);
        }

        public void copyCoord(Coord2D nextCoord) {
            this.x = nextCoord.x;
            this.y = nextCoord.y;
        }
    }

    private static class RockGrid {
        private List<List<Rock>> grid = new ArrayList<>();

        private void addRow(String rowString) {
            grid.add(new ArrayList<>());
            List<Rock> currentRow = grid.get(rowCount() - 1);
            for (char c : rowString.toCharArray()) {
                currentRow.add(Rock.fromSymbol(c).get());
            }
        }

        private int rollDirection(Direction toRollIn, Coord2D rockCoord) {
            int distanceRolled = 0;
            Coord2D destinationCoord = new Coord2D(rockCoord);
            Coord2D nextCoord = new Coord2D(rockCoord);
            nextCoord.go(toRollIn);
            while (coordsInBounds(nextCoord) && getRock(nextCoord) == Rock.space) {
                destinationCoord.copyCoord(nextCoord);
                nextCoord.go(toRollIn);
            }
            distanceRolled = orthogonalDistance(destinationCoord, rockCoord);
            swap(rockCoord, destinationCoord);
            return distanceRolled;
        }

        private void swap(Coord2D rockCoord, Coord2D destinationCoord) {
            Rock swap = getRock(rockCoord);
            setRock(rockCoord, getRock(destinationCoord));
            setRock(destinationCoord, swap);
        }

        private int orthogonalDistance(Coord2D firstCoord, Coord2D secondCoord) {
            return Math.abs(firstCoord.x() - secondCoord.x()) + Math.abs(firstCoord.y() - secondCoord.y());
        }

        private boolean coordsInBounds(Coord2D coords) {
            return coords.x() < grid.get(0).size() && coords.x() >= 0 && coords.y() < grid.size() && coords.y() >= 0;
        }

        private Rock getRock(Coord2D coord) {
            return grid.get(coord.y()).get(coord.x);
        }

        private void setRock(Coord2D coord, Rock toSet) {
            grid.get(coord.y).set(coord.x, toSet);
        }

        private void rollAllRocks(Direction toRollIn) {
            Coord2D currentCoord = toRollIn.getStartCoordForSweep(this);
            Direction progressDirection = toRollIn.opposite();
            Direction secondaryProgressDirection;
            if (toRollIn == Direction.north || toRollIn == Direction.south) {
                secondaryProgressDirection = Direction.east;
            } else {
                secondaryProgressDirection = Direction.south;
            }
            while (coordsInBounds(currentCoord)) {
                while (coordsInBounds(currentCoord)) {
                    if (getRock(currentCoord) == Rock.round) {
                        rollDirection(toRollIn, currentCoord);
                    }
                    currentCoord.go(secondaryProgressDirection);
                }
                currentCoord.go(progressDirection);
                secondaryProgressDirection = secondaryProgressDirection.opposite();
                currentCoord.go(secondaryProgressDirection);
            }
        }

        public int columnCount() {
            return grid.get(0).size();
        }

        public int rowCount() {
            return grid.size();
        }

        public long calculateLoad(Direction toCalculateFrom) {
            long totalLoad = 0;
            int multiplier;
            Coord2D currentCoord = toCalculateFrom.getStartCoordForSweep(this);
            Direction progressDirection = toCalculateFrom.opposite();
            Direction secondaryProgressDirection;
            if (toCalculateFrom == Direction.north || toCalculateFrom == Direction.south) {
                secondaryProgressDirection = Direction.east;
                multiplier = rowCount();
            } else {
                secondaryProgressDirection = Direction.south;
                multiplier = columnCount();
            }
            while (coordsInBounds(currentCoord)) {
                while (coordsInBounds(currentCoord)) {
                    if (getRock(currentCoord) == Rock.round) {
                        totalLoad += multiplier;
                    }
                    currentCoord.go(secondaryProgressDirection);
                }
                currentCoord.go(progressDirection);
                multiplier -= 1;
                secondaryProgressDirection = secondaryProgressDirection.opposite();
                currentCoord.go(secondaryProgressDirection);
            }
            return totalLoad;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (List<Rock> row : grid) {
                for (Rock rock : row) {
                    sb.append(rock.symbol());
                }
                sb.append(String.format("%n"));
            }
            return sb.toString();
        }
    }

    public enum Direction {
        north(0, -1),
        east(1, 0),
        south(0, 1),
        west(-1, 0);

        final private int x;
        final private int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int goX(int x) {
            return x + this.x;
        }

        public int goY(int y) {
            return y + this.y;
        }

        public Coord2D getStartCoordForSweep(RockGrid toSweep) {
            return new Coord2D(x == 1 ? toSweep.columnCount() - 1 : 0, y == 1 ? toSweep.rowCount() - 1 : 0);
        }

        public Coord2D asCoord() {
            return new Coord2D(x, y);
        }

        public Direction opposite() {
            return Direction.values()[(this.ordinal() + 2) % 4];
        }
    }

    public enum Rock {
        round('O'),
        cube('#'),
        space('.');

        final private char symbol;
        private static final Map<Character, Rock> charToEnum = Stream.of(values())
                .collect(Collectors.toMap(c -> c.symbol(), c -> c));

        public static Optional<Rock> fromSymbol(char symbol) {
            return Optional.ofNullable(charToEnum.get(symbol));
        }

        Rock(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }
    }

    public static void main(String[] args) {
        long totalLoad = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week2/input_14").toPath())) {
            RockGrid grid = new RockGrid();
            inputLines.forEachOrdered(grid::addRow);
            grid.rollAllRocks(Direction.north);
            totalLoad = grid.calculateLoad(Direction.north);
            //System.out.println(grid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("The total load is %d%n", totalLoad);

    }
}
