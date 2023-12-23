package com.brynnperit.aoc2023.week3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

public class solver162 {

    private record Contraption(EnumMap<Direction, Contraption> orthogonalConnectedContraptions, Coord2D position,
            ContraptionType type) implements Comparable<Contraption> {
        public Contraption(Coord2D position, ContraptionType type) {
            this(new EnumMap<>(Direction.class), position, type);
        }

        public Optional<Contraption> get(Direction direction) {
            return Optional.ofNullable(orthogonalConnectedContraptions.get(direction));
        }

        public List<Ray> getRays(Direction encounterDirection) {
            Map<Contraption, EnumSet<Direction>> completedContraptions = new TreeMap<>();
            Deque<Direction> allDirections = new ArrayDeque<>();
            Deque<Contraption> allContraptions = new ArrayDeque<>();
            List<Ray> rays = new ArrayList<>();

            allDirections.push(encounterDirection);
            allContraptions.push(this);
            completedContraptions.computeIfAbsent(this, i -> EnumSet.noneOf(Direction.class))
                    .add(encounterDirection);

            while (!allDirections.isEmpty()) {
                // TODO: The iterator doesn't work as advertised, just do int iteration.
                Direction currentDirection = allDirections.pop();
                Contraption currentContraption = allContraptions.pop();

                EnumSet<Direction> resultDirections = currentContraption.type.GetDirections(currentDirection);
                for (Direction resultDirection : resultDirections) {
                    Optional<Contraption> resultContraption = currentContraption.get(resultDirection);
                    if (resultContraption.isPresent()) {
                        if (!completedContraptions
                                .computeIfAbsent(resultContraption.get(), i -> EnumSet.noneOf(Direction.class))
                                .contains(resultDirection)) {
                            Contraption nextContraption = resultContraption.get();
                            // System.out.printf("Current contraption: %d %d %s %s, next contraption: %d %d
                            // %s %s%n",
                            // currentContraption.position.x, currentContraption.position.y,
                            // currentDirection.toString(), currentContraption.type.symbol,
                            // nextContraption.position.x, nextContraption.position.y,
                            // resultDirection.toString(),
                            // nextContraption.type.symbol);
                            completedContraptions.get(nextContraption).add(resultDirection);
                            allDirections.push(resultDirection);
                            allContraptions.push(nextContraption);
                        }
                    }
                }
            }

            for (Contraption completeContraption : completedContraptions.keySet()) {
                for (Direction completedDirection : completedContraptions.get(completeContraption)) {
                    Optional<Contraption> optionalContraption = completeContraption.get(completedDirection.opposite());
                    if (optionalContraption.isPresent()) {
                        rays.add(new Ray(completeContraption.position(),
                                optionalContraption.get().position()));
                    }
                }
            }
            return rays;
        }

        @Override
        public int compareTo(Contraption other) {
            int result = position.compareTo(other.position);
            if (result == 0) {
                result = type.compareTo(other.type);
            }
            return result;
        }
    }

    private record Ray(Coord2D start, Coord2D end, Direction direction) {
        public Ray(Coord2D start, Coord2D end) {
            this(start, end, Direction.getDirection(start, end));
        }

        private Ray(Coord2D start, Coord2D end, Direction direction) {
            this.start = start;
            this.end = end;
            this.direction = direction;
        }
    }

    private record LightGrid(List<BitSet> rows, int width) {
        public LightGrid(ContraptionGridBuilder.ContraptionGrid grid) {
            this(new ArrayList<>(), grid.colCount());
            IntStream.range(0, grid.rowCount()).forEach(i -> rows.add(new BitSet(grid.colCount())));
        }

        private LightGrid(List<BitSet> rows, int width) {
            this.rows = rows;
            this.width = width;
        }

        public void applyRay(Ray toApply) {
            if (toApply.direction == Direction.NORTH || toApply.direction == Direction.SOUTH) {
                Coord2D raySquare = new Coord2D(toApply.start());
                while (!raySquare.equals(toApply.end())) {
                    rows.get(raySquare.y()).set(raySquare.x());
                    raySquare.go(toApply.direction());
                }
            } else {
                int fromX = Math.min(toApply.start().x(), toApply.end().x());
                int toX = Math.max(toApply.start().x(), toApply.end().x()) + 1;
                rows.get(toApply.start().y()).set(fromX, toX);
            }
        }

        public long litSquareCount() {
            return rows.stream().mapToLong(bits -> bits.cardinality()).sum();
        }

        @Override
        public String toString() {
            StringBuilder sbMain = new StringBuilder();
            for (BitSet bitSet : rows) {
                StringBuilder sb = new StringBuilder();
                IntStream.range(0, width).forEach(i -> sb.append('.'));
                bitSet.stream().forEach(i -> sb.setCharAt(i, '#'));
                sb.append(String.format("%n"));
                sbMain.append(sb);
            }
            return sbMain.toString();
        }
    }

    private enum ContraptionType {
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

    private static class Coord2D implements Comparable<Coord2D> {
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

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Coord2D))
                return false;
            Coord2D other = (Coord2D) o;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(x);
            result = 31 * result + Integer.hashCode(y);
            return result;
        }

        public Coord2D getEdgeOfGrid(Direction resultDirection, int colCount, int rowCount) {
            int resultX = x + resultDirection.x() * colCount;
            resultX = Math.min(Math.max(0, resultX), colCount - 1);
            int resultY = y + resultDirection.y() * rowCount;
            resultY = Math.min(Math.max(0, resultY), rowCount - 1);
            return new Coord2D(resultX, resultY);
        }

        @Override
        public int compareTo(Coord2D other) {
            int result = Integer.compare(x, other.x);
            if (result == 0) {
                result = Integer.compare(y, other.y);
            }
            return result;
        }
    }

    public enum Direction {
        NORTH(0, -1, (coord, rows, cols) -> cols.get(coord.x).lowerEntry(coord.y)),
        EAST(1, 0, (coord, rows, cols) -> rows.get(coord.y).higherEntry(coord.x)),
        SOUTH(0, 1, (coord, rows, cols) -> cols.get(coord.x).higherEntry(coord.y)),
        WEST(-1, 0, (coord, rows, cols) -> rows.get(coord.y).lowerEntry(coord.x));

        final private int x;
        final private int y;
        final private TriFunction<Coord2D, List<NavigableMap<Integer, Contraption>>, List<NavigableMap<Integer, Contraption>>, Entry<Integer, Contraption>> connectFunction;

        @FunctionalInterface
        private interface TriFunction<T, U, V, R> {
            R apply(T t, U u, V v);
        }

        Direction(int x, int y,
                TriFunction<Coord2D, List<NavigableMap<Integer, Contraption>>, List<NavigableMap<Integer, Contraption>>, Entry<Integer, Contraption>> connectFunction) {
            this.x = x;
            this.y = y;
            this.connectFunction = connectFunction;
        }

        public static Direction getDirection(Coord2D start, Coord2D end) {
            int xDifference = end.x() - start.x();
            int yDifference = end.y() - start.y();
            if (xDifference > 0) {
                return Direction.EAST;
            } else if (xDifference < 0) {
                return Direction.WEST;
            } else if (yDifference < 0) {
                return Direction.NORTH;
            }
            return Direction.SOUTH;
        }

        public Direction next() {
            return Direction.values()[(this.ordinal() + 1) % 4];
        }

        public Direction previous() {
            return Direction.values()[(this.ordinal() + 3) % 4];
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int goX(int x) {
            return x + this.x;
        }

        public int goY(int y) {
            return y + this.y;
        }

        public Coord2D asCoord() {
            return new Coord2D(x, y);
        }

        public Direction opposite() {
            return Direction.values()[(this.ordinal() + 2) % 4];
        }

        public Entry<Integer, Contraption> getConnect(Contraption contraption,
                List<NavigableMap<Integer, Contraption>> condensedRows,
                List<NavigableMap<Integer, Contraption>> condensedColumns) {
            return this.connectFunction.apply(contraption.position(), condensedRows, condensedColumns);
        }

        public Entry<Integer, Contraption> getConnect(Coord2D position,
                List<NavigableMap<Integer, Contraption>> condensedRows,
                List<NavigableMap<Integer, Contraption>> condensedColumns) {
            return this.connectFunction.apply(position, condensedRows, condensedColumns);
        }
    }

    private static class ContraptionGridBuilder {
        private List<List<Contraption>> grid = new ArrayList<>();

        public static class ContraptionGrid {
            private List<List<Contraption>> grid = new ArrayList<>();
            private List<NavigableMap<Integer, Contraption>> condensedRows = new ArrayList<>();
            private List<NavigableMap<Integer, Contraption>> condensedColumns = new ArrayList<>();
            private Set<Contraption> allContraptions = new HashSet<>();
            final int colCount;
            final int rowCount;

            private ContraptionGrid(List<List<Contraption>> grid) {
                this.grid = grid;
                ListIterator<List<Contraption>> rowIterator = grid.listIterator();
                int columnCount = grid.get(0).size();
                IntStream.range(0, columnCount).forEach(i -> condensedColumns.add(new TreeMap<>()));
                colCount = grid.get(0).size();
                rowCount = grid.size();
                // Fill in condensed rows and columns
                while (rowIterator.hasNext()) {
                    int rowNumber = rowIterator.nextIndex();
                    List<Contraption> currentRow = rowIterator.next();
                    NavigableMap<Integer, Contraption> condensedRow = new TreeMap<>();
                    ListIterator<Contraption> columnIterator = currentRow.listIterator();
                    while (columnIterator.hasNext()) {
                        int columnNumber = columnIterator.nextIndex();
                        Contraption currentContraption = columnIterator.next();
                        if (currentContraption.type != ContraptionType.SPACE) {
                            NavigableMap<Integer, Contraption> condensedColumn = condensedColumns.get(columnNumber);
                            condensedRow.put(columnNumber, currentContraption);
                            condensedColumn.put(rowNumber, currentContraption);
                            allContraptions.add(currentContraption);
                        }
                    }
                    condensedRows.add(condensedRow);
                }
                // Link all the contraptions to each other
                for (Contraption contraption : allContraptions) {
                    for (Direction toConnect : Direction.values()) {
                        Entry<Integer, Contraption> connectingEntry = toConnect.getConnect(contraption, condensedRows,
                                condensedColumns);
                        if (connectingEntry != null) {
                            contraption.orthogonalConnectedContraptions.put(toConnect, connectingEntry.getValue());
                        } else {
                            Contraption contraptionAtEdge = getContraptionAtEdge(contraption, toConnect);
                            if (contraptionAtEdge != contraption) {
                                contraption.orthogonalConnectedContraptions.put(toConnect, contraptionAtEdge);
                            } else {
                                contraption.orthogonalConnectedContraptions.put(toConnect, null);
                            }
                        }
                    }
                }
            }

            public Contraption getContraptionAtEdge(Contraption contraption, Direction resultDirection) {
                Contraption toGet = null;
                Coord2D startPosition = contraption.position();
                Coord2D endPosition = startPosition.getEdgeOfGrid(resultDirection, colCount, rowCount);
                toGet = grid.get(endPosition.y()).get(endPosition.x());
                if (!startPosition.equals(endPosition)) {
                    toGet.orthogonalConnectedContraptions.put(resultDirection.opposite(), contraption);
                }
                return toGet;
            }

            public int colCount() {
                return colCount;
            }

            public int rowCount() {
                return rowCount;
            }

            public List<LightGrid> allPossibleLightGrids(){
                List<Coord2D> startCoords = new ArrayList<>();
                List<Direction> startDirections = new ArrayList<>();
                for (int x = 0; x < colCount; x++){
                    startCoords.add(new Coord2D(x,0));
                    startDirections.add(Direction.SOUTH);
                    startCoords.add(new Coord2D(x,rowCount-1));
                    startDirections.add(Direction.NORTH);
                }
                for (int y = 0; y < rowCount; y++){
                    startCoords.add(new Coord2D(0,y));
                    startDirections.add(Direction.EAST);
                    startCoords.add(new Coord2D(colCount-1,y));
                    startDirections.add(Direction.WEST);
                }
                return IntStream.range(0, startCoords.size()).parallel().mapToObj(i->fillLightGrid(startCoords.get(i), startDirections.get(i))).toList();
            }

            public LightGrid fillLightGrid(Coord2D startCoord, Direction startDirection) {
                LightGrid lightGrid = new LightGrid(this);
                Contraption firstContraption = condensedRows.get(startCoord.y()).get(startCoord.x());
                if (firstContraption == null) {
                    Entry<Integer, Contraption> possibleContraption = startDirection.getConnect(startCoord,
                            condensedRows, condensedColumns);
                    if (possibleContraption != null) {
                        firstContraption = possibleContraption.getValue();
                    }
                }
                if (firstContraption != null) {
                    if (condensedRows.get(startCoord.y()).containsKey(startCoord.x())) {
                        firstContraption = condensedRows.get(0).get(0);
                    }
                    List<Ray> raysInGrid = firstContraption.getRays(startDirection);
                    raysInGrid.add(new Ray(startCoord, firstContraption.position()));
                    raysInGrid.forEach(ray -> lightGrid.applyRay(ray));
                }else{
                    lightGrid.applyRay(new Ray(startCoord, startCoord.getEdgeOfGrid(startDirection, colCount, rowCount)));
                }
                // lightGrid.applyRay(new Ray(startCoord, firstContraption.position()));

                return lightGrid;
            }
        }

        private void addRow(String rowString) {
            List<Contraption> newRow = new ArrayList<>();
            int rowNumber = grid.size();
            int columnNumber = 0;
            for (char c : rowString.toCharArray()) {
                newRow.add(new Contraption(new Coord2D(columnNumber, rowNumber), ContraptionType.fromSymbol(c).get()));
                columnNumber++;
            }
            grid.add(newRow);
        }

        private ContraptionGrid finalizeGrid() {
            return new ContraptionGrid(grid);
        }
    }

    public static void main(String[] args) {
        long energizedCount = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_16").toPath())) {
            ContraptionGridBuilder builder = new ContraptionGridBuilder();
            inputLines.forEachOrdered(line -> builder.addRow(line));
            ContraptionGridBuilder.ContraptionGrid grid = builder.finalizeGrid();
            energizedCount = grid.allPossibleLightGrids().stream().parallel().mapToLong(light->light.litSquareCount()).max().orElse(-1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("%d tiles are energized%n", energizedCount);
    }
}