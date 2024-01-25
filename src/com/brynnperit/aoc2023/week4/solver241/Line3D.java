package com.brynnperit.aoc2023.week4.solver241;

import java.math.*;
import java.util.regex.*;

import com.brynnperit.aoc2023.week4.solver241.IntersectionResult.ResultType;

public class Line3D {
    final private Vector3D position;
    final private Vector3D velocity;
    final private static Pattern vectorPattern3D = Pattern.compile("([0-9]+),\\s+([0-9]+),\\s+([0-9]+)\\s+@\\s+(-?[0-9]+),\\s+(-?[0-9]+),\\s+(-?[0-9]+)");

    public Vector3D position() {
        return position;
    }

    public Vector3D velocity() {
        return velocity;
    }

    public Line3D(String vectorString) {
        Matcher vectorMatcher = vectorPattern3D.matcher(vectorString);
        vectorMatcher.find();
        long x = Long.parseLong(vectorMatcher.group(1));
        long y = Long.parseLong(vectorMatcher.group(2));
        long z = Long.parseLong(vectorMatcher.group(3));
        long vx = Long.parseLong(vectorMatcher.group(4));
        long vy = Long.parseLong(vectorMatcher.group(5));
        long vz = Long.parseLong(vectorMatcher.group(6));
        this.position = new Vector3D(x, y, z);
        this.velocity = new Vector3D(vx, vy, vz);
    }

    public Line3D(Vector3D position, Vector3D velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public BigDecimal closestIntersection(Line3D other) {
        // From
        // https://math.stackexchange.com/questions/3081301/shortest-distance-between-two-vectors
        Vector3D vectorCrossProduct = velocity.cross(other.velocity);
        BigDecimal numerator = position.subtract(other.position).dot(vectorCrossProduct).abs();
        BigDecimal denominator = vectorCrossProduct.length();
        return numerator.divide(denominator, MathContext.DECIMAL128);
    }

    private Line3D removeZComponents(){
        return new Line3D(new Vector3D(position.x(), position.y(), BigDecimal.ZERO), new Vector3D(velocity.x(), velocity.y(), BigDecimal.ZERO));
    }

    public static Vector3D getIntersectionPoint2D(Line3D first, Line3D second) {
        return getIntersectionPoint(first.removeZComponents(), second.removeZComponents());
    }

    public static Vector3D getIntersectionPoint(Line3D first, Line3D second) {
        //From https://math.stackexchange.com/questions/270767/find-intersection-of-two-3d-lines
        Vector3D betweenLinesVector = second.position.subtract(first.position);
        BigDecimal fractionTop = second.velocity.cross(betweenLinesVector).length();
        BigDecimal fractionBottom = second.velocity.cross(first.velocity).length();
        Vector3D vectorToIntercept = first.velocity.multiply(fractionTop.divide(fractionBottom, MathContext.DECIMAL128));
        Vector3D interceptPoint;
        if (second.velocity.cross(betweenLinesVector).dot(second.velocity.cross(first.velocity)).signum()>=0){
            interceptPoint = first.position.add(vectorToIntercept);
        }else{
            interceptPoint = first.position.subtract(vectorToIntercept);
        }
        return interceptPoint;
    }

    public Vector3D positionAtTime(long time) {
        return position.add(velocity.multiply(time));
    }

    public Vector3D positionAtTime(BigDecimal time) {
        return position.add(velocity.multiply(time));
    }

    public IntersectionResult getIntersection2D(Line3D next) {
        return getIntersection2D(next, true);
    }

    public IntersectionResult getIntersection(Line3D next) {
        return getIntersection(next, true);
    }

    public IntersectionResult getIntersection2D(Line3D next, boolean checkParallel) {
        boolean parallel = false;
        if (checkParallel) {
            BigDecimal dotProduct = velocity.dot2D(next.velocity);
            BigDecimal velocityProduct = velocity.length2D().multiply(next.velocity.length2D());
            double difference = dotProduct.abs().subtract(velocityProduct.abs()).abs().doubleValue();
            parallel = difference < 0.00001;
        }
        return getIntersectResult2D(next, parallel);
    }

    public IntersectionResult getIntersection(Line3D next, boolean checkParallel) {
        boolean parallel = false;
        if (checkParallel) {
            BigDecimal dotProduct = velocity.dot(next.velocity);
            BigDecimal velocityProduct = velocity.length().multiply(next.velocity.length());
            double difference = dotProduct.abs().subtract(velocityProduct.abs()).abs().doubleValue();
            parallel = difference < 0.00001;
        }
        return getIntersectResult(next, parallel);
    }

    private IntersectionResult getIntersectResult2D(Line3D next, boolean parallel) {
        if (!parallel){
            Vector3D intersectPosition = getIntersectionPoint2D(this, next);
            return constructIntersectionResult(next, intersectPosition);
        }
    return new IntersectionResult(ResultType.PARALLEL, null, null, null, this, next);
    }
    private IntersectionResult getIntersectResult(Line3D next, boolean parallel) {
        if (!parallel) {
            Vector3D intersectPosition = getIntersectionPoint(this, next);
            return constructIntersectionResult(next, intersectPosition);
        }
        return new IntersectionResult(ResultType.PARALLEL, null, null, null, this, next);
    }

    private IntersectionResult constructIntersectionResult(Line3D next, Vector3D intersectPosition) {
        BigDecimal intersectTimeThis = intersectPosition.x().subtract(position.x()).divide(velocity.x(), MathContext.DECIMAL128);
        BigDecimal intersectTimeOther = intersectPosition.x().subtract(next.position.x()).divide(next.velocity.x(), MathContext.DECIMAL128);
        if (intersectTimeThis.compareTo(BigDecimal.ZERO) >= 0 && intersectTimeOther.compareTo(BigDecimal.ZERO) >= 0) {
            return new IntersectionResult(ResultType.INTERSECTS, intersectPosition, intersectTimeThis, intersectTimeOther, this, next);
        } else {
            return new IntersectionResult(ResultType.PAST_INTERSECTS, intersectPosition, intersectTimeThis, intersectTimeOther, this, next);
        }
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", position.toString(), velocity.toString());
    }
}
