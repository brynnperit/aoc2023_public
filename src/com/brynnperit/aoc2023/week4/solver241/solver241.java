package com.brynnperit.aoc2023.week4.solver241;

import java.util.*;
import java.util.stream.*;

import java.nio.file.*;
import java.io.*;
import java.math.BigDecimal;

public class solver241 {

    public static void main(String[] args) {
        // final long minCoordinate = 7;
        final long minCoordinate = 200000000000000L;
        // final long maxCoordinate = 27;
        final long maxCoordinate = 400000000000000L;
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

        return bestInterceptingLine;
    }
}
