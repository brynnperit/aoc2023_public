package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public class FlipFlopModule extends BaseModule {
    private boolean isOn = false;

    public FlipFlopModule(List<String> l, String s) {
        super(l, s);
        defaultLength++;
        stateBitsLength = defaultLength;
    }

    @Override
    public void accept(Module origin, PulseType pulse) {
        if (pulse == PulseType.LOW) {
            if (isOn) {
                isOn = false;
                for (Module destination : destinations) {
                    queue.add(this, PulseType.LOW, destination);
                }
            } else {
                isOn = true;
                for (Module destination : destinations) {
                    queue.add(this, PulseType.HIGH, destination);
                }
            }
        }
    }
}
