package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;

public record PartRanges(EnumMap<PartCategory, Range> partRanges)implements Comparable<PartRanges> {
    public PartRanges(PartRanges other){
        this(new EnumMap<>(other.partRanges()));
    }

    public Collection<Range> getRanges(){
        return partRanges.values();
    }

    public PartRanges(){
        this(new EnumMap<>(PartCategory.class));
        for (PartCategory category : PartCategory.values()){
            partRanges.put(category, new Range(1L, 4000L));
        }
    }

    public boolean isValid(){
        for (PartCategory category: PartCategory.values()){
            if (!getRange(category).isValid()){
                return false;
            }
        }
        return true;
    }

    public void setRange(PartCategory category, Range newRange){
        partRanges.put(category, newRange);
    }

    public Range getRange(PartCategory category){
        return partRanges.get(category);
    }

    public long countDistinct(){
        long distinct = 1;
        for (PartCategory category:PartCategory.values()){
            distinct*=getRange(category).size();
        }
        return distinct;
    }

    public PartRanges subtractDifferingRange(PartRanges other){
        for (PartCategory category:PartCategory.values()){
            if (!getRange(category).equals(other.getRange(category))){
                PartRanges subtracted = new PartRanges(this);
                subtracted.setRange(category, getRange(category).subtract(other.getRange(category)));
                return subtracted;
            }
        }
        return this;
    }

    // public boolean combineRange(PartRanges other){
    //     PartCategory differingCategory = null;
    //     int sameCount = 0;
    //     for (PartCategory category:PartCategory.values()){
    //         if (getRange(category).equals(other.getRange(category))){
    //             sameCount++;
    //         }else{
    //             differingCategory = category;
    //         }
    //     }
    //     if (sameCount != 3){
    //         return false;
    //     }else{
    //         Optional<Range> combinedRange = getRange(differingCategory).combine(other.getRange(differingCategory));
    //         if (combinedRange.isPresent()){
    //             setRange(differingCategory, combinedRange.get());
    //             return true;
    //         }else{
    //             System.err.printf("Couldn't combine ranges:%n%s%n%s%n",getRange(differingCategory),other.getRange(differingCategory));
    //             return false;
    //         }
    //     }
    // }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (PartCategory category:PartCategory.values()){
            sb.append(String.format("%s: %s,",category.symbol,getRange(category).toString()));
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int compareTo(PartRanges other) {
        int compareValue = 0;
        for (PartCategory category:PartCategory.values()){
            compareValue = getRange(category).compareTo(other.getRange(category));
            if (compareValue != 0){
                break;
            }
        }
        return compareValue;
    }
}
