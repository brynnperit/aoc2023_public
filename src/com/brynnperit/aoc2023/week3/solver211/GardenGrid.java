package com.brynnperit.aoc2023.week3.solver211;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GardenGrid {
    private List<List<GardenTileType>> grid = new ArrayList<>();
    private Coord2D startPosition;
    private BitSet nonEmptyRows = new BitSet();
    private BitSet nonEmptyColumns = new BitSet();

    public int rows() {
        return grid.size();
    }

    public int columns() {
        return grid.get(0).size();
    }

    public Coord2D getStartPosition() {
        return new Coord2D(startPosition);
    }

    public NavigableMap<Long, Long> getGardenCountsFromPoints(List<SearchVertice> points) {
        List<SearchVertice> vertices = getGardensFromPoints(points);
        Map<Long, Long> verticesByCostCount = vertices.stream()
                .collect(Collectors.groupingBy(SearchVertice::cost, Collectors.counting()));
        // Sum up the number of garden counts so at any given cost number it maps to the
        // total number of gardens reachable at that cost
        for (long cost = 2; cost < verticesByCostCount.keySet().size(); cost++) {
            verticesByCostCount.put(cost, verticesByCostCount.get(cost) + verticesByCostCount.get(cost - 2));
        }
        return new TreeMap<>(verticesByCostCount);
    }

    public List<SearchVertice> getGardensExactStepsFromStart(long steps) {
        List<SearchVertice> verticeList = getGardensFromPoint(startPosition, steps);
        return getGardensExactStepsFromStart(verticeList, steps);
    }

    public List<SearchVertice> getGardensExactStepsFromStart(List<SearchVertice> gardensFromStart, long steps) {
        long isOdd = steps % 2;
        return gardensFromStart.stream().filter(v -> v.cost() % 2 == isOdd).toList();
    }

    public List<SearchVertice> getGardensFromPoint(Coord2D point) {
        return getGardensFromPoint(point, Long.MAX_VALUE);
    }

    public List<SearchVertice> getGardensFromPoint(Coord2D point, long steps) {
        return getGardensFromPoints(List.of(new SearchVertice(point, 0)), steps);
    }

    public List<SearchVertice> getGardensFromPoints(List<SearchVertice> points) {
        return getGardensFromPoints(points, Long.MAX_VALUE);
    }

    public List<SearchVertice> getGardensFromPoints(List<SearchVertice> points, long maxSteps) {
        List<SearchVertice> verticeList = new ArrayList<>();
        List<BitSet> visitedTiles = new ArrayList<>();

        IntStream.range(0, grid.size()).forEach(i -> {
            visitedTiles.add(new BitSet());
        });

        Queue<SearchVertice> visitQueue = new PriorityQueue<>((v1, v2) -> Long.compare(v1.cost(), v2.cost()));
        visitQueue.addAll(points);
        while (!visitQueue.isEmpty()) {
            SearchVertice currentSearch = visitQueue.poll();
            if (currentSearch.cost() <= maxSteps) {
                int row = currentSearch.position().y();
                int col = currentSearch.position().x();
                if (!visitedTiles.get(row).get(col)) {
                    visitedTiles.get(row).set(col);
                    verticeList.add(currentSearch);
                    if (currentSearch.cost() < maxSteps) {
                        for (Direction direction : Direction.values()) {
                            Coord2D newCoord = new Coord2D(currentSearch.position());
                            newCoord.go(direction);
                            if (newCoord.isInBounds(grid)) {
                                if (!visitedTiles.get(newCoord.y()).get(newCoord.x())) {
                                    if (grid.get(newCoord.y()).get(newCoord.x()) != GardenTileType.rock) {
                                        visitQueue.add(new SearchVertice(newCoord, currentSearch.cost() + 1));
                                    } else {
                                        visitedTiles.get(newCoord.y()).set(newCoord.x());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return verticeList;
    }

    public void addRow(String rowString) {
        boolean rocksInRow = false;
        List<GardenTileType> row = new ArrayList<>();
        int columnIndex = 0;
        for (char tileChar : rowString.toCharArray()) {
            GardenTileType type = GardenTileType.fromSymbol(tileChar);
            if (type == GardenTileType.start) {
                startPosition = new Coord2D(row.size(), grid.size());
            } else if (type == GardenTileType.rock) {
                rocksInRow = true;
                nonEmptyColumns.set(columnIndex);
            }
            row.add(type);
            columnIndex++;
        }
        grid.add(row);
        if (rocksInRow) {
            nonEmptyRows.set(grid.size() - 1);
        }
    }

    public long getCountOfGardensExactBigStepsFromStart(long steps) {
        long gardenCount = 0;
        int rows = rows();
        int cols = columns();
        Map<Direction, Coord2D> corners = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            corners.put(direction, Coord2D.getCornerAtCounterClockwiseFromDirection(direction, grid));
        }
        List<SearchVertice> verticeList = getGardensFromPoint(startPosition);
        gardenCount += getGardensExactStepsFromStart(verticeList, steps).size();
        List<SearchVertice> cornerVerticeList = verticeList.stream().filter(v -> corners.containsValue(v.position()))
                .toList();
        List<SearchVertice> allEdgeVerticeList = verticeList.stream().filter(v -> v.position().x() == 0
                || v.position().x() == cols - 1 || v.position().y() == 0 || v.position().y() == rows - 1).toList();
        Map<Direction, List<SearchVertice>> edgeVertices = new EnumMap<>(Direction.class);
        Map<Direction, SearchVertice> cornerVertices = new EnumMap<>(Direction.class);
        // Hacky way to get the search vertices for the corners and edges
        for (Direction direction : Direction.values()) {
            Stream<SearchVertice> allEdgeVerticeStream = allEdgeVerticeList.stream()
                    .filter(v -> v.position().isAtEdge(grid, direction));
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                edgeVertices.put(direction,
                        allEdgeVerticeStream.filter(v -> !nonEmptyColumns.get(v.position().x())).toList());
            } else {
                edgeVertices.put(direction,
                        allEdgeVerticeStream.filter(v -> !nonEmptyRows.get(v.position().y())).toList());
            }
            cornerVertices.put(direction, cornerVerticeList.stream()
                    .filter(v -> v.position().equals(corners.get(direction))).findFirst().get());
        }

        for (Direction direction : Direction.values()) {
            gardenCount += getOrthogonalGardens(steps, corners, edgeVertices, direction);
            gardenCount += getDiagonalGardens(steps, corners, cornerVertices, direction);
        }
        return gardenCount;
    }

    private long getOrthogonalGardens(long steps, Map<Direction, Coord2D> corners,
            Map<Direction, List<SearchVertice>> edgeVerticesMap, Direction direction) {
        long orthogonalGardens = 0;
        int rows = rows();
        int cols = columns();
        List<SearchVertice> edgeVertices = edgeVerticesMap.get(direction);
        // For north the left entry point into orthogonal grids north of start will be
        // southwest, right is southeast.
        long offset = Long.MAX_VALUE;
        for (SearchVertice vertice : edgeVertices) {
            offset = Math.min(offset, vertice.cost());
        }
        List<SearchVertice> entryVertices = new ArrayList<>();
        for (SearchVertice vertice : edgeVertices) {
            entryVertices.add(new SearchVertice(vertice.position().putAtEdge(grid, direction.opposite()),
                    vertice.cost() - offset));
        }
        NavigableMap<Long, Long> cachedOrthogonalResults = getGardenCountsFromPoints(entryVertices);

        int stepsPerGrid;
        switch (direction) {
        case SOUTH, NORTH:
            stepsPerGrid = rows;
            break;
        default:
            stepsPerGrid = cols;
        }
        boolean gridsAlternateEvensAndOdds = stepsPerGrid % 2 == 1;

        // The +1 is due to needing one additional step to get into the second grid from
        // the first
        long stepsAtSecondGrid = offset + 1;

        boolean usesOddForTiebreakers = offset%2==steps%2;
        orthogonalGardens += calculateColumn(steps, stepsPerGrid, usesOddForTiebreakers, gridsAlternateEvensAndOdds,cachedOrthogonalResults, stepsAtSecondGrid);
        return orthogonalGardens;
    }

    private long getDiagonalGardens(long steps, Map<Direction, Coord2D> corners,
            Map<Direction, SearchVertice> cornerVertices, Direction direction) {
        long diagonalGardens = 0;
        int rows = rows();
        int cols = columns();
        
        boolean columnsAlternateEvensAndOdds = cols % 2 == 1;
        boolean rowsAlternateEvensAndOdds = rows % 2 == 1;
        // For north right will be northeast
        SearchVertice rightCorner = cornerVertices.get(direction.next());
        // For north the entry point into diagonal grids northeast of start will be
        // southwest
        Coord2D rightEntryPoint = corners.get(direction.next().opposite());
        SearchVertice cornerVertice = new SearchVertice(rightEntryPoint, 0);
        NavigableMap<Long, Long> cachedDiagonalResults = getGardenCountsFromPoints(List.of(cornerVertice));

        // +2 because it takes two steps from the corner of origin to reach the entry
        // corner of the diagonally adjacent grid
        long stepsAtSecondGrid = (rightCorner.cost() + 2);

        long stepsAtColumnEntry = stepsAtSecondGrid;
        boolean firstGridUsesOddForTiebreakers = cornerVertices.get(direction.next()).cost()%2==steps%2;
        long columnNumber = 1;
        while (stepsAtColumnEntry <= steps) {
            boolean columnUsesOddsForTiebreakers;
            if (firstGridUsesOddForTiebreakers) {
                if (columnsAlternateEvensAndOdds) {
                    columnUsesOddsForTiebreakers = columnNumber % 2 == 0;
                } else {
                    columnUsesOddsForTiebreakers = true;
                }
            } else {
                if (columnsAlternateEvensAndOdds) {
                    columnUsesOddsForTiebreakers = columnNumber % 2 == 1;
                } else {
                    columnUsesOddsForTiebreakers = false;
                }
            }
            if (stepsAtColumnEntry >= (steps-20)){
                System.out.println("break");
            }
            diagonalGardens += calculateColumn(steps, rows, columnUsesOddsForTiebreakers, rowsAlternateEvensAndOdds,cachedDiagonalResults, stepsAtColumnEntry);

            stepsAtColumnEntry += cols;
            columnNumber++;
        }
        return diagonalGardens;
    }

    private long calculateColumn(long steps, int stepsPerGrid, boolean columnUsesOddsForTiebreakers,
            boolean rowsAlternateEvensAndOdds, NavigableMap<Long, Long> cachedResults, long stepsAtColumnEntry) {
        long gardenCount = 0;

        Entry<Long, Long> lastResults = cachedResults.lastEntry();
        Entry<Long, Long> secondLastResults = cachedResults.lowerEntry(lastResults.getKey());
        long stepsNeededForFullCoverage = lastResults.getKey();
        long stepsLeftInColumn = steps - stepsAtColumnEntry;
        long GridsInColumnWithOrthogonalFull = stepsLeftInColumn / stepsPerGrid;
        long stepsLeft = stepsLeftInColumn % stepsPerGrid;
        long gridIndex = GridsInColumnWithOrthogonalFull;
        // Grid index increases by 1 as there is a partial grid left over
        gridIndex++;

        while (stepsLeft < stepsNeededForFullCoverage && gridIndex > 0) {
            gardenCount += cachedResults.get(stepsLeft);
            gridIndex -= 1;
            stepsLeft += stepsPerGrid;
        }

        // Calculate the total number of gardens between the start of the column at index 0 (which
        // is already included in the orthogonal results or start grid)
        // and the start of the incomplete set
        long oddPlots = 0, evenPlots = 0;
        if (rowsAlternateEvensAndOdds) {
            // Grids alternate between entering with an even number and odd number of steps
            evenPlots = gridIndex / 2;
            oddPlots = gridIndex / 2;
            if (gridIndex % 2 == 1) {
                if (columnUsesOddsForTiebreakers) {
                    // Will get odd, then even,..., then even, finally odd
                    oddPlots++;
                } else {
                    // Will get even,then odd,..., then odd, finally even
                    evenPlots++;
                }
            }
        } else {
            if (columnUsesOddsForTiebreakers) {
                oddPlots = gridIndex;
            } else {
                evenPlots = gridIndex;
            }
        }

        long oddGardens, evenGardens;
        if (lastResults.getKey() % 2 == 1) {
            oddGardens = oddPlots * lastResults.getValue();
            evenGardens = evenPlots * secondLastResults.getValue();
        } else {
            evenGardens = evenPlots * lastResults.getValue();
            oddGardens = oddPlots * secondLastResults.getValue();
        }
        gardenCount += oddGardens;
        gardenCount += evenGardens;
        return gardenCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<StringBuilder> rowStringBuilders = getRowStringBuilders();
        rowStringBuilders.stream().forEachOrdered(rsb -> sb.append(rsb));
        return sb.toString();
    }

    public String toString(List<SearchVertice> plots) {
        StringBuilder sb = new StringBuilder();
        List<StringBuilder> rowStringBuilders = getRowStringBuilders();
        for (SearchVertice vert : plots) {
            Coord2D coord = vert.position();
            rowStringBuilders.get(coord.y()).setCharAt(coord.x(), (char) ('0' + ((vert.cost()) % 10)));
        }
        rowStringBuilders.stream().forEachOrdered(rsb -> sb.append(rsb));
        return sb.toString();
    }

    private List<StringBuilder> getRowStringBuilders() {
        List<StringBuilder> rowStringBuilders = new ArrayList<>();
        for (List<GardenTileType> row : grid) {
            StringBuilder rowBuilder = new StringBuilder();
            for (GardenTileType tile : row) {
                rowBuilder.append(tile.symbol());
            }
            rowBuilder.append(String.format("%n"));
            rowStringBuilders.add(rowBuilder);
        }
        return rowStringBuilders;
    }

}
