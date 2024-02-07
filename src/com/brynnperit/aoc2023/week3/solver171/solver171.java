package com.brynnperit.aoc2023.week3.solver171;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.*;

public class solver171 {

    public static void main(String[] args) {
        long firstLowestElapsedCost = -1;
        long secondLowestElapsedCost = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week3/input_17").toPath())) {
            City city = new City(10);
            inputLines.forEachOrdered(line -> city.addRow(line));
            firstLowestElapsedCost = city.getShortestPath(new Coord2D(0, 0),
            new Coord2D(city.columns() - 1, city.rows() - 1),1,3).elapsedCost();
            secondLowestElapsedCost = city.getShortestPath(new Coord2D(0, 0),
            new Coord2D(city.columns() - 1, city.rows() - 1),4,10).elapsedCost();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("First lowest elapsed cost is: %d%n", firstLowestElapsedCost);
        System.out.printf("Second lowest elapsed cost is: %d%n", secondLowestElapsedCost);
    }
}
