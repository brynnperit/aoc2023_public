package com.brynnperit.aoc2023.week3.solver162;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

public class ContraptionGrid {
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
                if (currentContraption.type() != ContraptionType.SPACE) {
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
                    contraption.orthogonalConnectedContraptions().put(toConnect, connectingEntry.getValue());
                } else {
                    Contraption contraptionAtEdge = getContraptionAtEdge(contraption, toConnect);
                    if (contraptionAtEdge != contraption) {
                        contraption.orthogonalConnectedContraptions().put(toConnect, contraptionAtEdge);
                    } else {
                        contraption.orthogonalConnectedContraptions().put(toConnect, null);
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
            toGet.orthogonalConnectedContraptions().put(resultDirection.opposite(), contraption);
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
    public static class ContraptionGridBuilder {
        private List<List<Contraption>> grid = new ArrayList<>();
    
        public void addRow(String rowString) {
            List<Contraption> newRow = new ArrayList<>();
            int rowNumber = grid.size();
            int columnNumber = 0;
            for (char c : rowString.toCharArray()) {
                newRow.add(new Contraption(new Coord2D(columnNumber, rowNumber), ContraptionType.fromSymbol(c).get()));
                columnNumber++;
            }
            grid.add(newRow);
        }
    
        public ContraptionGrid finalizeGrid() {
            return new ContraptionGrid(grid);
        }
    }
}