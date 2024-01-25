package com.brynnperit.aoc2023.week4.solver241;

import java.util.*;
import java.util.stream.*;

import java.nio.file.*;
import java.io.*;
import java.math.BigDecimal;

public class solver241 {

    public static void main(String[] args) {
        final long minCoordinate = 7;
        // final long minCoordinate = 200000000000000L;
        final long maxCoordinate = 27;
        // final long maxCoordinate = 400000000000000L;
        long intersectionsWithinTestArea = -1;
        long addedPositionCoordinatesOfInterceptingLine = -1;
        try (Stream<String> lines = Files.lines(new File("inputs/week4/input_24").toPath())) {
            List<Line3D> hailstones = lines.map(l -> new Line3D(l)).toList();
            intersectionsWithinTestArea = get2DAreaResults(minCoordinate, maxCoordinate, hailstones);
            Line3D allInterceptingLine = getAllInterceptingLine(hailstones);
            addedPositionCoordinatesOfInterceptingLine = allInterceptingLine.position().x().longValue() + allInterceptingLine.position().y().longValue() + allInterceptingLine.position().z().longValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("There are %d intersections between %d and %d%n", intersectionsWithinTestArea, minCoordinate, maxCoordinate);
        System.out.printf("The added components of the intercepting line's start position are %d%n", addedPositionCoordinatesOfInterceptingLine);
    }

    private static long get2DAreaResults(final long minCoordinate, final long maxCoordinate, List<Line3D> hailstones) {
        long intersectionsWithinTestArea;
        ListIterator<Line3D> firstIterator = hailstones.listIterator();
        List<IntersectionResult> results = new ArrayList<>();
        while (firstIterator.hasNext()) {
            Line3D firstVector = firstIterator.next();
            if (firstIterator.hasNext()) {
                ListIterator<Line3D> secondIterator = hailstones.listIterator(firstIterator.nextIndex());
                while (secondIterator.hasNext()) {
                    results.add(firstVector.getIntersection2D(secondIterator.next()));
                }
            }
        }
        intersectionsWithinTestArea = results.stream().filter(r -> r.isWithinTestArea2D(minCoordinate, maxCoordinate)).count();
        return intersectionsWithinTestArea;
    }

    private static Line3D getAllInterceptingLine(List<Line3D> hailstones) {
        //From https://aidiakapi.com/blog/2024-01-20-advent-of-code-2023-day-24/
        Iterator<Line3D> hailstoneIterator = hailstones.iterator();
        Line3D first = hailstoneIterator.next();
        Line3D second = hailstoneIterator.next();
        while (first.closestIntersection(second).compareTo(BigDecimal.ZERO) == 0) {
            second = hailstoneIterator.next();
        }
        Line3D third = hailstoneIterator.next();
        while (first.closestIntersection(third).compareTo(BigDecimal.ZERO) == 0) {
            third = hailstoneIterator.next();
        }

        Vector3D firstSecondPosition = second.position().subtract(first.position());
        Vector3D secondSecondPosition = second.position().add(second.velocity()).subtract(first.position().add(first.velocity()));
        Vector3D firstThirdPosition = third.position().subtract(first.position());
        Vector3D secondThirdPosition = third.position().add(third.velocity()).subtract(first.position().add(first.velocity()));
        Vector3D secondPlaneNormal =firstSecondPosition.cross(secondSecondPosition);
        Vector3D thirdPlaneNormal = firstThirdPosition.cross(secondThirdPosition);
        BigDecimal thirdIntersectionTime = firstSecondPosition.subtract(firstThirdPosition).dot(secondPlaneNormal).divide(third.velocity().subtract(first.velocity()).dot(secondPlaneNormal));
        BigDecimal secondIntersectionTime = firstThirdPosition.subtract(firstSecondPosition).dot(thirdPlaneNormal).divide(second.velocity().subtract(first.velocity()).dot(thirdPlaneNormal));
        Vector3D thirdActualPositionAtIntercept = third.positionAtTime(thirdIntersectionTime);
        Vector3D secondActualPositionAtIntercept = second.positionAtTime(secondIntersectionTime);
        Vector3D velocity = thirdActualPositionAtIntercept.subtract(secondActualPositionAtIntercept).divide(thirdIntersectionTime.subtract(secondIntersectionTime));
        Vector3D position = secondActualPositionAtIntercept.subtract(velocity.multiply(secondIntersectionTime));


        Line3D bestInterceptingLine = new Line3D(position, velocity);//extracted(first, second, third).orElse(extracted(second, first, third).get());
        BigDecimal firstIntersection = bestInterceptingLine.closestIntersection(first);
        BigDecimal secondIntersection = bestInterceptingLine.closestIntersection(second);
        BigDecimal thirdIntersection = bestInterceptingLine.closestIntersection(third);
        BigDecimal firstIntersectionTimeCheck = bestInterceptingLine.getIntersection(first).interceptTimeSecond();
        BigDecimal secondIntersectionTimeCheck = bestInterceptingLine.getIntersection(second).interceptTimeSecond();
        BigDecimal thirdIntersectionTimeCheck = bestInterceptingLine.getIntersection(third).interceptTimeSecond();

        return bestInterceptingLine;
    }

    //Look at this fancy idea that didn't work at all!
    private static Optional<Line3D> lineGetter(Line3D first, Line3D second, Line3D third) {
        boolean foundValidLine = false;
        long timeBetweenIntercepts = 1;
        long timeJumpValue = 32767;
        long lowerWindow = 1;
        long upperWindow = Long.MAX_VALUE;
        long bestInterceptTimeDelta = Long.MAX_VALUE;
        boolean increasingJumps = true;
        while (!foundValidLine) {
            if (lowerWindow >= upperWindow) {
                System.out.println("window closed, break");
                break;
            }
            Line3D newInterceptingLine = getBestInterceptingLine(first, second, third, timeBetweenIntercepts);
            BigDecimal interceptDistance = newInterceptingLine.closestIntersection(third);
            double doubleInterceptDistance = interceptDistance.doubleValue();
            if (doubleInterceptDistance < 0.1) {// interceptDistance.compareTo(BigDecimal.ZERO) == 0) {
                IntersectionResult result = newInterceptingLine.getIntersection(third, false);
                long interceptTimeDelta = Math.abs(result.interceptTimeFirst().longValue() - result.interceptTimeSecond().longValue());
                if (result.interceptTimeFirst().compareTo(result.interceptTimeSecond()) == 0) {// &&!bestInterceptingLine.velocity.containsFractions()) {
                    return Optional.of(newInterceptingLine);
                } else if (interceptTimeDelta <= bestInterceptTimeDelta) {
                    bestInterceptTimeDelta = interceptTimeDelta;
                    if (increasingJumps) {
                        timeJumpValue *= 2;
                    }
                    lowerWindow = timeBetweenIntercepts;
                    System.out.printf("Best delta: %d, current delta: %d, time: %d, lower window: %d, upper window: %d, JUMPING%n", bestInterceptTimeDelta, interceptTimeDelta, timeBetweenIntercepts, lowerWindow, upperWindow);
                    timeBetweenIntercepts += timeJumpValue;

                } else {
                    // Went too far, revert to lower window with half the jump value
                    if (increasingJumps) {
                        increasingJumps = false;
                    }
                    upperWindow = Math.min(upperWindow-1, timeBetweenIntercepts-1);
                    timeBetweenIntercepts = lowerWindow;
                    timeJumpValue /= 2;
                    System.out.printf("Best delta: %d, current delta: %d, time: %d, lower window: %d, upper window: %d, REVERTING%n", bestInterceptTimeDelta, interceptTimeDelta, timeBetweenIntercepts, lowerWindow, upperWindow);
                }
            }else{
                timeBetweenIntercepts++;
            }
            

        }
        return Optional.empty();
    }

    private static Line3D getBestInterceptingLine(Line3D first, Line3D second, Line3D third, long timeBetweenIntercepts) {
        long timeBeforeFirstIntercept = 0;
        boolean inExpandingJumpPhase = true;
        long jumpValue = 1;
        Line3D bestInterceptingLine;
        BigDecimal interceptDistanceToThird;
        while (true) {
            Line3D lowerInterceptLine = getInterceptingLine(first, second, timeBeforeFirstIntercept, timeBetweenIntercepts);
            Line3D higherInterceptLine = getInterceptingLine(first, second, timeBeforeFirstIntercept + 1, timeBetweenIntercepts);
            BigDecimal lowerInterceptDistance = lowerInterceptLine.closestIntersection(third);
            BigDecimal higherInterceptDistance = higherInterceptLine.closestIntersection(third);
            boolean bestLineIsHigher = higherInterceptDistance.compareTo(lowerInterceptDistance) <= 0;
            if (bestLineIsHigher) {
                bestInterceptingLine = higherInterceptLine;
                interceptDistanceToThird = higherInterceptDistance;
                // If the best intercept is with time 64, this will test
                // 0,1->2,3->6,7->14,15->30,31->62,63->126,127->94,95->78,79->70,71->66,67->64,65
                if (inExpandingJumpPhase) {
                    jumpValue *= 2;
                } else {
                    jumpValue /= 2;
                }
                timeBeforeFirstIntercept += jumpValue;
            } else {
                bestInterceptingLine = lowerInterceptLine;
                interceptDistanceToThird = lowerInterceptDistance;
                inExpandingJumpPhase = false;
                jumpValue /= 2;
                timeBeforeFirstIntercept -= jumpValue;
            }
            if (interceptDistanceToThird.compareTo(BigDecimal.ZERO) == 0 || jumpValue == 0) {
                break;
            }
        }
        return bestInterceptingLine;
    }

    private static Line3D getInterceptingLine(Line3D first, Line3D second, long timeBeforeFirstIntercept, long timeBetweenIntercepts) {
        long secondInterceptTime = timeBeforeFirstIntercept + timeBetweenIntercepts;
        Vector3D firstAtTime = first.positionAtTime(timeBeforeFirstIntercept);
        Vector3D secondAtTime = second.positionAtTime(secondInterceptTime);
        Vector3D interceptingVelocity = secondAtTime.subtract(firstAtTime).divide(timeBetweenIntercepts);
        Vector3D interceptingPosition = firstAtTime.subtract(interceptingVelocity.multiply(timeBeforeFirstIntercept));
        Line3D interceptingLine = new Line3D(interceptingPosition, interceptingVelocity);
        return interceptingLine;
    }
}
