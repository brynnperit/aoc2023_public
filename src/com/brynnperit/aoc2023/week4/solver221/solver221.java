package com.brynnperit.aoc2023.week4.solver221;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver221 {
    public static void main(String[] args) {
        long bricksRemovable = -1;
        long bricksThatWouldFall = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/week4/input_22").toPath())) {
            BrickPile pile = new BrickPile();
            lines.forEachOrdered(s->pile.addBrick(s));
            Set<RectangularPrism> removableBricks = pile.getRemovableBricks();
            bricksRemovable = removableBricks.size();
            bricksThatWouldFall = pile.getNumberOfBricksThatWouldFall();
            // System.out.println(removableBricks);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.printf("There are %d removable bricks%n", bricksRemovable);
        System.out.printf("The cascading falling bricks sum is %d%n", bricksThatWouldFall);
    }
}
