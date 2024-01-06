package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public class ConjunctionModule extends BaseModule{
    private Map<Module, Integer> modulesToBitset = new HashMap<>();
    private BitSet mostRecentReceived = new BitSet();

    public ConjunctionModule(List<String> l, String s) {
        super(l, s);
    }

    @Override
    public void accept(Module origin, PulseType pulse) {
        mostRecentReceived.set(modulesToBitset.get(origin),pulse==PulseType.HIGH);
        PulseType toSend;
        if (mostRecentReceived.cardinality()==modulesToBitset.size()){
            toSend = PulseType.LOW;
        }else{
            toSend = PulseType.HIGH;
        }
        for(Module destination: destinations){
            queue.add(this, toSend, destination);
        }
    }

    @Override
    public void addInputModule(Module input){
        super.addInputModule(input);
        modulesToBitset.put(input, modulesToBitset.size());
    }
}
