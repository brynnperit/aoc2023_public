package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public class MessageHistory {
    Map<Long, List<Module>> pulseOriginsHistory = new TreeMap<>();
    Map<Long, List<PulseType>> pulsesHistory = new TreeMap<>();
    Map<Long, List<Module>> pulseTargetsHistory = new TreeMap<>();

    public void add(long pressCount, Module origin, PulseType pulse, Module target) {
        pulseOriginsHistory.computeIfAbsent(pressCount, i -> new ArrayList<>()).add(origin);
        pulsesHistory.computeIfAbsent(pressCount, i -> new ArrayList<>()).add(pulse);
        pulseTargetsHistory.computeIfAbsent(pressCount, i -> new ArrayList<>()).add(target);
    }

    public long getFirstAllHighCount(){
        Map<Module, Long> previous = new HashMap<>();
        Map<Module, Long> differences = new HashMap<>();
        Set<Module> origins = new HashSet<>();
        for (Long index : pulseOriginsHistory.keySet()) {
            for (int indexWithinPress = 0; indexWithinPress < pulseOriginsHistory.get(index)
                    .size(); indexWithinPress++) {
                Module origin = pulseOriginsHistory.get(index).get(indexWithinPress);
                origins.add(origin);
                PulseType pulse = pulsesHistory.get(index).get(indexWithinPress);
                if (pulse == PulseType.HIGH) {
                    if (previous.containsKey(origin)){
                        differences.put(origin, index - previous.get(origin));
                        if (differences.size()==origins.size()){
                            return differences.values().stream().mapToLong(l->l).reduce(1, (l1,l2)->l1*l2);
                        }
                    }
                    previous.put(origin, index);
                }
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Map<Module, Long> previous = new HashMap<>();
        for (Long index : pulseOriginsHistory.keySet()) {
            for (int indexWithinPress = 0; indexWithinPress < pulseOriginsHistory.get(index)
                    .size(); indexWithinPress++) {
                Module origin = pulseOriginsHistory.get(index).get(indexWithinPress);
                PulseType pulse = pulsesHistory.get(index).get(indexWithinPress);
                Module target = pulseTargetsHistory.get(index).get(indexWithinPress);
                if (pulse == PulseType.HIGH) {
                    long difference = 0;
                    if (previous.containsKey(origin)){
                        difference = index - previous.get(origin);
                    }
                    previous.put(origin, index);
                    sb.append(String.format("%d: %s sent %s to %s, %d after last%n", index, origin.getName(), pulse, target.getName(), difference));
                }
            }
        }
        return sb.toString();
    }

    public Map<Long, List<Module>> getPulseOriginsHistory() {
        return this.pulseOriginsHistory;
    }

    public Map<Long, List<PulseType>> getPulsesHistory() {
        return this.pulsesHistory;
    }

    public Map<Long, List<Module>> getPulseTargetsHistory() {
        return this.pulseTargetsHistory;
    }
}