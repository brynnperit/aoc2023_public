package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;
import java.util.stream.*;

public class ModuleCollection {
    private final Set<String> unseenDestinations = new HashSet<>();
    private final Map<String, Module> seenDestinations = new TreeMap<>();
    final private Deque<Module> pulseOrigins = new ArrayDeque<>();
    final private Deque<PulseType> pulses = new ArrayDeque<>();
    final private Deque<Module> pulseTargets = new ArrayDeque<>();
    final private MessageQueue messageQueue = new MessageQueue(pulseOrigins, pulses, pulseTargets);
    final private static ButtonModule buttonModule = new ButtonModule(null, "button");
    private boolean print;
    private BroadcastModule broadcastModule;

    public Module getModule(String name){
        return seenDestinations.get(name);
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public void addModule(String moduleString) {
        Module newModule = ModuleTypes.getModuleType(moduleString);
        newModule.setQueue(messageQueue);
        String moduleName = newModule.getName();
        if (moduleName.equals("broadcaster")) {
            broadcastModule = (BroadcastModule) newModule;
        }
        addModule(newModule);
    }

    private void addModule(Module newModule) {
        unseenDestinations.remove(newModule.getName());
        seenDestinations.put(newModule.getName(), newModule);
        for (String destinationString : newModule.getDestinationStrings()) {
            if (!seenDestinations.containsKey(destinationString)) {
                unseenDestinations.add(destinationString);
            }
        }
        if (unseenDestinations.isEmpty()) {
            seenDestinations.values().stream().forEach(m -> m.setDestinationStringsToModules(seenDestinations));
        }
    }

    public Map<PulseType, Long> pushButton(long times, MessageHistory history, String targetName) {
        finishModules();
        long[] counts = new long[PulseType.values().length];
        Module historyModule = null;
        if (targetName != null){
            historyModule = seenDestinations.get(targetName);
        }
        for (long buttonPresses = 0; buttonPresses < times; buttonPresses++) {
            pulseOrigins.add(buttonModule);
            pulses.add(PulseType.LOW);
            pulseTargets.add(broadcastModule);
            while (!pulses.isEmpty()) {
                Module origin = pulseOrigins.remove();
                PulseType pulse = pulses.remove();
                counts[pulse.ordinal()]++;
                Module target = pulseTargets.remove();
                if (history != null && historyModule != null && target == historyModule){
                    history.add(buttonPresses, origin, pulse, target);
                }
                if (print) {
                    System.out.printf("%5d:%s -%s-> %s%n", buttonPresses, origin.getName(), pulse.toString(), target.getName());
                }
                target.accept(origin, pulse);
            }
        }
        return Stream.of(PulseType.values()).collect(Collectors.toMap(p -> p, p -> counts[p.ordinal()]));
    }

    private void finishModules() {
        while (!unseenDestinations.isEmpty()) {
            addModule(new OutputModule(unseenDestinations.iterator().next()));
        }
    }

    private static class ButtonModule extends BaseModule {
        public ButtonModule(List<String> destinationStrings, String name) {
            super(destinationStrings, name);
        }

        @Override
        public void accept(Module arg0, PulseType arg1) {
            throw new UnsupportedOperationException("Unimplemented method 'accept'");
        }
    }

    private static class OutputModule extends BaseModule {
        public OutputModule(String name) {
            super(new ArrayList<>(0), name);
        }

        @Override
        public void accept(Module arg0, PulseType arg1) {
        }
    }
}
