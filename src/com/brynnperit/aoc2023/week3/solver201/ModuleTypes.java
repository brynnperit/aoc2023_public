package com.brynnperit.aoc2023.week3.solver201;

import java.util.*;
import java.util.regex.*;
import java.util.function.*;

public enum ModuleTypes {
    FLIP_FLOP("^%([a-z]+)", (l,s)->new FlipFlopModule(l,s)), 
    CONJUNCTION("^&([a-z]+)", (l,s)->new ConjunctionModule(l,s)), 
    BROADCAST("(broadcaster)", (l,s)->new BroadcastModule(l,s));

    final private Pattern matchPattern;
    final private BiFunction<List<String>,String,Module> constructor;
    final private static Pattern DESTINATION_PATTERN = Pattern.compile("([a-z]+)(?:[,\\n]|$)");

    private ModuleTypes(String matchPatternString, BiFunction<List<String>,String,Module> constructor) {
        this.constructor = constructor;
        this.matchPattern = Pattern.compile(matchPatternString);
    }

    public static Module getModuleType(String moduleString){
        for (ModuleTypes module : ModuleTypes.values()){
            Matcher moduleMatcher = module.matchPattern.matcher(moduleString);
            if (moduleMatcher.find()){
                Matcher destinationMatcher = DESTINATION_PATTERN.matcher(moduleString);
                String name = moduleMatcher.group(1);
                List<String> destinations = new ArrayList<>();
                while (destinationMatcher.find()){
                    destinations.add(destinationMatcher.group(1));
                }
                return module.constructor.apply(destinations, name);
            }
        }
        return null;
    }
}
