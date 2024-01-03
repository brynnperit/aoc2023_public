package com.brynnperit.aoc2023.week3.solver192;

import java.util.EnumMap;

public class CombinedRangesLeaf implements CombinedRangesTreeNode{
    final private PartCategory partCategory;
    final private CombinedRangesMap map;

    public CombinedRangesLeaf(PartCategory partCategory, EnumMap<PartCategory, Range> toAdd) {
        this.partCategory = partCategory;
        map = new CombinedRangesMap(toAdd.get(partCategory), null);
    }

    public CombinedRangesLeaf(CombinedRangesLeaf other){
        this.partCategory = other.partCategory;
        this.map = new CombinedRangesMap(other.map);
    }

    @Override
    public CombinedRangesTreeNode copy() {
        return new CombinedRangesLeaf(this);
    }

    @Override
    public void addRanges(EnumMap<PartCategory, Range> toAdd) {
        map.addRanges(toAdd, partCategory);
    }

    @Override
    public long getDistinctCombinations() {
        return map.getDistinctCombinations();
    }

    @Override 
    public String toString(){
        return toString(0);
    }

    public String toString(int level){
        return String.format("[%s:%s",partCategory.symbol(),map.toString(level));
    }

    @Override 
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (!(o instanceof CombinedRangesLeaf))
            return false;
        CombinedRangesLeaf other = (CombinedRangesLeaf)o;
        return other.partCategory == partCategory && map.equals(other.map);
    }
}
