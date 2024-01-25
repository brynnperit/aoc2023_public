package com.brynnperit.aoc2023.week4.solver241;

import java.math.BigDecimal;

public record IntersectionResult(ResultType type, Vector3D intersectPosition, BigDecimal interceptTimeFirst, BigDecimal interceptTimeSecond, Line3D first, Line3D second) {

    public static enum ResultType{
        INTERSECTS,
        PAST_INTERSECTS,
        PARALLEL;
    }

    public boolean isWithinTestArea2D(long minCoordinate, long maxCoordinate) {
        if (type == ResultType.PARALLEL || type == ResultType.PAST_INTERSECTS){
            return false;
        }
        BigDecimal minCoord = new BigDecimal(minCoordinate);
        BigDecimal maxCoord = new BigDecimal(maxCoordinate);
        boolean xWithinArea = intersectPosition.x().compareTo(minCoord) >= 0 && intersectPosition.x().compareTo(maxCoord) <= 0;
        boolean yWithinArea = intersectPosition.y().compareTo(minCoord) >= 0 && intersectPosition.y().compareTo(maxCoord) <= 0;
        return xWithinArea && yWithinArea;
    }

}
