package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;
import java.util.Map.Entry;

public class CombinedRangesMap {
    private NavigableMap<Range, CombinedRangesTreeNode> rangeSet = new TreeMap<>();
    final private boolean isLeafNode;

    public CombinedRangesMap(Range firstRange, CombinedRangesTreeNode node) {
        rangeSet.put(firstRange, node);
        isLeafNode = node == null;
    }

    public CombinedRangesMap(CombinedRangesMap other) {
        for (Entry<Range, CombinedRangesTreeNode> branch : other.rangeSet.entrySet()) {
            rangeSet.put(branch.getKey(), branch.getValue() != null ? branch.getValue().copy() : null);
        }
        this.isLeafNode = other.isLeafNode;
    }

    public void addRanges(EnumMap<PartCategory, Range> toAdd, PartCategory partCategory) {
        Range newRange = toAdd.get(partCategory);
        Range previousRange = rangeSet.floorKey(newRange);
        if (previousRange != null && previousRange.overlaps(newRange)) {
            CombinedRangesTreeNode toDuplicate = rangeSet.remove(previousRange);
            Range lowNoOverlap = previousRange.getRangeBeforeOverlap(newRange);
            if (lowNoOverlap.isValid()) {
                rangeSet.put(lowNoOverlap, toDuplicate != null ? toDuplicate.copy() : toDuplicate);
            }
            Range overlap = previousRange.getOverlappingRange(newRange);
            rangeSet.put(overlap, toDuplicate != null ? toDuplicate.copy() : toDuplicate);
            if (toDuplicate != null) {
                rangeSet.get(overlap).addRanges(toAdd);
            }
            Range highNoOverlap = previousRange.getRangeAfterOverlap(newRange);

            if (highNoOverlap.isValid() && highNoOverlap.overlaps(previousRange)) {
                rangeSet.put(highNoOverlap, toDuplicate != null ? toDuplicate.copy() : toDuplicate);
            }
            newRange = newRange.subtract(previousRange);
        }

        Range nextRange = rangeSet.ceilingKey(new Range(newRange.low(),newRange.low()));
        // Todo: handle the new range exists before and overlaps with the low part of
        // the next range
        while (newRange.isValid()) {
            if (nextRange == null || !newRange.overlaps(nextRange)) {
                rangeSet.put(newRange,
                        CombinedRangesTreeNode.getCombinedRangesNode(partCategory.nextCategory().orElse(null), toAdd));
            } else {
                Range lowNoOverlap = newRange.getRangeBeforeOverlap(nextRange);
                if (lowNoOverlap.isValid()) {
                    rangeSet.put(lowNoOverlap, CombinedRangesTreeNode
                            .getCombinedRangesNode(partCategory.nextCategory().orElse(null), toAdd));
                    newRange = newRange.subtract(lowNoOverlap);
                }
                Range overlap = newRange.getOverlappingRange(nextRange);
                CombinedRangesTreeNode toDuplicate = rangeSet.get(nextRange);
                rangeSet.remove(nextRange);
                rangeSet.put(overlap, toDuplicate != null ? toDuplicate.copy() : toDuplicate);
                if (toDuplicate != null) {
                    rangeSet.get(overlap).addRanges(toAdd);
                }
                Range highNoOverlap = newRange.getRangeAfterOverlap(nextRange);
                newRange = newRange.subtract(overlap);
                if (highNoOverlap.isValid() && highNoOverlap.overlaps(nextRange)) {
                    rangeSet.put(highNoOverlap, toDuplicate != null ? toDuplicate.copy() : toDuplicate);
                }
            }
            nextRange = rangeSet.ceilingKey(new Range(newRange.low(),newRange.low()));
        }
        for (int keyIndex = 0; keyIndex < rangeSet.keySet().size() - 1; keyIndex++) {
            Iterator<Range> keys = rangeSet.navigableKeySet().iterator();
            for (int x = 0; x < keyIndex; x++) {
                keys.next();
            }
            Range firstRange = keys.next();
            Range secondRange = keys.next();
            if (firstRange.high() == secondRange.low() - 1) {
                if (isLeafNode) {
                    rangeSet.remove(firstRange);
                    rangeSet.remove(secondRange);
                    rangeSet.put(new Range(firstRange.low(), secondRange.high()), null);
                    keyIndex--;
                } else if (rangeSet.get(firstRange).equals(rangeSet.get(secondRange))) {
                    CombinedRangesTreeNode node = rangeSet.remove(firstRange);
                    rangeSet.remove(secondRange);
                    rangeSet.put(new Range(firstRange.low(), secondRange.high()), node);
                    keyIndex--;
                }
            }
        }
    }

    public long getDistinctCombinations() {
        long sum = 0;
        for (Entry<Range, CombinedRangesTreeNode> branch : rangeSet.entrySet()) {
            sum += branch.getKey().size()
                    * (branch.getValue() != null ? branch.getValue().getDistinctCombinations() : 1);
        }
        return sum;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        for (Entry<Range, CombinedRangesTreeNode> branch : rangeSet.entrySet()) {
            if (level != PartCategory.values().length - 1) {
                sb.append(String.format("%n"));
                for (int x = 0; x < level; x++) {
                    sb.append(String.format("\t"));
                }
            }
            sb.append(branch.getKey().toString());
            sb.append(':');
            // if (level != PartCategory.values().length - 1) {
            // sb.append(String.format("%n"));
            // for (int x = 0; x < level; x++) {
            // sb.append(String.format("\t"));
            // }
            // }
            // sb.append('[');

            sb.append(branch.getValue() != null ? branch.getValue().toString(level + 1) : "-");
            sb.append(',');
            // if (level == PartCategory.values().length - 2) {
            // sb.append(String.format("%n"));
            // for (int x = 0; x < level; x++) {
            // sb.append(String.format("\t"));
            // }
            // }

            // sb.append(']');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        if (level == PartCategory.values().length - 1) {
            // sb.append(String.format("%n"));
            // for (int x = 0; x < level + 1; x++) {
            // sb.append(String.format("\t"));
            // }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CombinedRangesMap))
            return false;
        CombinedRangesMap other = (CombinedRangesMap) o;
        return other.isLeafNode == isLeafNode && rangeSet.equals(other.rangeSet);
    }
}