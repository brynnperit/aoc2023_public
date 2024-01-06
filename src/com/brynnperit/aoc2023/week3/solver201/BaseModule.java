package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;

public abstract class BaseModule implements Module {
    final protected String name;
    protected final List<String> destinationStrings;
    protected final List<Module> destinations = new ArrayList<>();
    protected Set<Module> inputModules = new HashSet<>();
    protected MessageQueue queue = null;
    protected int stateBitsLength = 0;
    protected int defaultLength = 0;

    public BaseModule(List<String> destinationStrings, String name) {
        this.name = name;
        this.destinationStrings = destinationStrings;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDestinationStrings() {
        return Collections.unmodifiableList(destinationStrings);
    }

    @Override
    public List<Module> getDestinations() {
        return Collections.unmodifiableList(destinations);
    }

    @Override
    public Set<Module> getInputModules() {
        return Collections.unmodifiableSet(inputModules);
    }

    @Override
    public void setDestinationStringsToModules(Map<String, Module> destinationMap) {
        for (String destinationString : destinationStrings) {
            Module destinationModule = destinationMap.get(destinationString);
            destinations.add(destinationModule);
            destinationModule.addInputModule(this);
        }
        destinationStrings.clear();
    }

    @Override
    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    @Override
    public void addInputModule(Module input) {
        inputModules.add(input);
    }
}
