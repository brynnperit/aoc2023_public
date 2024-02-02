package com.brynnperit.aoc2023.week3.solver162;

import java.util.*;
import java.util.stream.*;

public record LightGrid(List<BitSet> rows, int width) {
    public LightGrid(ContraptionGrid grid) {
        this(new ArrayList<>(), grid.colCount());
        IntStream.range(0, grid.rowCount()).forEach(i -> rows.add(new BitSet(grid.colCount())));
    }

    public LightGrid(List<BitSet> rows, int width) {
        this.rows = rows;
        this.width = width;
    }

    public void applyRay(Ray toApply) {
        if (toApply.direction() == Direction.NORTH || toApply.direction() == Direction.SOUTH) {
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