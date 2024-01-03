package com.brynnperit.aoc2023.week3.solver191;

import java.util.*;
import java.util.regex.*;

public record Part(Map<PartCategory, Long> partRatings) {
    private static final Pattern partSpecificationPattern = Pattern.compile("([a-z])=([0-9]+)");
    private static final Pattern partPattern = Pattern.compile("^\\{(([a-z])=([0-9]+),?)+\\}$");

    public static Optional<Part> GetPart(String partString) {
        Matcher partMatcher = partPattern.matcher(partString);
        if (partMatcher.matches()) {
            Map<PartCategory, Long> ratings = new EnumMap<>(PartCategory.class);
            Matcher partSpecificationMatcher = partSpecificationPattern.matcher(partString);
            while (partSpecificationMatcher.find()) {
                PartCategory category = PartCategory.fromSymbol(partSpecificationMatcher.group(1).charAt(0)).get();
                Long categoryRating = Long.parseLong(partSpecificationMatcher.group(2));
                ratings.put(category, categoryRating);
            }
            return Optional.ofNullable(new Part(ratings));
        } else {
            return Optional.ofNullable(null);
        }
    }

    public Collection<Long> getAllRatings(){
        return partRatings.values();
    }

    public long getRating(PartCategory category) {
        return partRatings.get(category);
    }
}
