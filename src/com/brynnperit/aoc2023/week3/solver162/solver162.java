package com.brynnperit.aoc2023.week3.solver162;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.*;

public class solver162 {
    public static void main(String[] args) {
        long topLeftEnergizedCount = -1;
        long maximumEnergizedCount = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week3/input_16").toPath())) {
            ContraptionGrid.ContraptionGridBuilder builder = new ContraptionGrid.ContraptionGridBuilder();
            inputLines.forEachOrdered(line -> builder.addRow(line));
            ContraptionGrid grid = builder.finalizeGrid();
            LightGrid lightGrid = grid.fillLightGrid(new Coord2D(0, 0), Direction.EAST);
            topLeftEnergizedCount = lightGrid.litSquareCount();
            maximumEnergizedCount = grid.allPossibleLightGrids().stream().parallel().mapToLong(light->light.litSquareCount()).max().orElse(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("%d tiles are energized when entering from the top left %n", topLeftEnergizedCount);
        System.out.printf("%d tiles are energized in the maximum configuration %n", maximumEnergizedCount);
    }
}