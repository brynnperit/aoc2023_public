package com.brynnperit.aoc2023.week3.solver201;

import java.util.function.*;
import java.util.*;

public interface Module extends BiConsumer<Module,PulseType>{
    public String getName();
    public List<String> getDestinationStrings();
    public List<Module> getDestinations();
    public Set<Module> getInputModules();
    public void setDestinationStringsToModules(Map<String, Module> destinationMap);
    public void setQueue(MessageQueue queue);
    public void addInputModule(Module input);
}
