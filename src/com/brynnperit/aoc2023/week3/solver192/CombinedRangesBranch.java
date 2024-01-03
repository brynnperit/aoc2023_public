package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;

public class CombinedRangesBranch implements CombinedRangesTreeNode {
    final private PartCategory partCategory;
    final private CombinedRangesMap map;

    public CombinedRangesBranch(PartRanges initial) {
        this(PartCategory.values()[0],initial.partRanges());
    }

    private CombinedRangesBranch(CombinedRangesBranch other) {
        this.partCategory = other.partCategory;
        this.map = new CombinedRangesMap(other.map);
    }

    public CombinedRangesBranch(PartCategory category, EnumMap<PartCategory, Range> map) {
        this.partCategory = category;
        this.map = new CombinedRangesMap(map.get(partCategory),CombinedRangesTreeNode.getCombinedRangesNode(category.nextCategory().get(), map));
    }

    public CombinedRangesTreeNode copy() {
        return new CombinedRangesBranch(this);
    }

    public void addRanges(PartRanges toAdd){
        addRanges(toAdd.partRanges());
    }

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
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s:",partCategory.symbol()));
        // for(int x = 0; x < level+1;x++){
        //     sb.append(String.format("\t"));
        // }
        sb.append(String.format("%s",map.toString(level)));
        return sb.toString();
    }

    @Override 
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (!(o instanceof CombinedRangesBranch))
            return false;
        CombinedRangesBranch other = (CombinedRangesBranch)o;
        return other.partCategory == partCategory && map.equals(other.map);
    }
}
