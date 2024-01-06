package com.brynnperit.aoc2023.week3.solver201;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

public class solver201 {

    public static void main(String[] args) {
        Map<PulseType, Long> counts = new HashMap<>();
        long firstRx = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/input_20").toPath())) {
            ModuleCollection modules = new ModuleCollection();
            lines.forEach(l -> modules.addModule(l));
            modules.setPrint(false);
            MessageHistory history = new MessageHistory();
            counts = modules.pushButton(10000, history, "kc");
            firstRx = history.getFirstAllHighCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        counts.entrySet().forEach(e -> System.out.printf("%s:%d, ", e.getKey(), e.getValue()));
        System.out.printf("multiplied together is %d%n", counts.values().stream().mapToLong(l -> l).reduce((x, y) -> x * y).orElse(-1));
        System.out.printf("first rx at %d%n",firstRx);

    }
}
