package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public class BroadcastModule extends BaseModule{
    public BroadcastModule(List<String> l, String s) {
        super(l, s);
        stateBitsLength = 1;
    }

    @Override
    public void accept(Module origin,PulseType pulse) {
        for (Module destination: destinations){
            queue.add(this, pulse, destination);
        }
    }
}
