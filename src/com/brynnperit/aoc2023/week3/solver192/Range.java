package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;

public record Range(long low, long high) implements Comparable<Range> {

    public boolean isValid() {
        return low <= high;
    }

    public long size() {
        return (high - low) + 1;
    }

    public Optional<Range> combine(Range other) {
        Range lowRange, highRange;
        if (this.compareTo(other) < 0) {
            lowRange = this;
            highRange = other;
        } else {
            lowRange = other;
            highRange = this;
        }
        if (lowRange.high >= highRange.low) {
            return Optional.of(new Range(lowRange.low, Math.max(lowRange.high, highRange.high)));
        }
        return Optional.empty();
    }

    public boolean overlaps(Range other){
        if (compareTo(other) < 0) {
            return high >= other.low;
        }else{
            return other.high >= low;
        }
    }

    public Range getRangeBeforeOverlap(Range other) {
        return new Range(Math.min(low, other.low), Math.max(low, other.low)-1);
    }

    public Range getOverlappingRange(Range other) {
        return new Range(Math.max(low, other.low), Math.min(high, other.high));
    }

    public Range getRangeAfterOverlap(Range other){
        return new Range(Math.min(high, other.high)+1, Math.max(high, other.high));
    }

    public Range subtract(Range other) {
        if (compareTo(other) < 0) {
            if (other.low <= high) {
                // 300 to 450 subtract 400 to 550 returns 300 to 399
                return new Range(low, other.low - 1);
            } else {
                return new Range(low, high);
            }
        } else {
            if (other.high >= low) {
                // 400 to 550 subtract 300 to 450 returns 451 to 550
                return new Range(other.high + 1, high);
            } else {
                return new Range(low, high);
            }
        }
    }

    @Override
    public int compareTo(Range other) {
        int result = Long.compare(low, other.low);
        if (result == 0) {
            result = Long.compare(high, other.high);
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%4d to %4d", low, high);
    }
}
