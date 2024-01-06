package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public class MessageQueue {
    final private Deque<Module> pulseOrigins;
    final private Deque<PulseType> pulses;
    final private Deque<Module> pulseTargets;

    public MessageQueue(Deque<Module> pulseOrigins, Deque<PulseType> pulses, Deque<Module> pulseTargets) {
        this.pulseOrigins = pulseOrigins;
        this.pulses = pulses;
        this.pulseTargets = pulseTargets;
    }
    
    public void add(Module pulseOrigin, PulseType pulse, Module pulseTarget){
        pulseOrigins.add(pulseOrigin);
        pulses.add(pulse);
        pulseTargets.add(pulseTarget);
    }
}
