package com.brynnperit.aoc2023.week3.solver192;

import java.util.EnumMap;

public interface CombinedRangesTreeNode {
    public static CombinedRangesTreeNode getCombinedRangesNode(PartCategory partCategory, EnumMap<PartCategory, Range> toAdd) {
        if (partCategory == null){
            return null;
        }
        if (partCategory.ordinal() == PartCategory.values().length-1){
            return new CombinedRangesLeaf(partCategory, toAdd);
        }else{
            return new CombinedRangesBranch(partCategory, toAdd);
        }
    }

    public CombinedRangesTreeNode copy();

    public void addRanges(EnumMap<PartCategory, Range> toAdd);

    public long getDistinctCombinations();

    public String toString(int level);
}
