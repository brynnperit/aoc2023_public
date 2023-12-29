package com.brynnperit.aoc2023.week3.solver182;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdgeCollection {
    private static Pattern edgePattern = Pattern.compile("\\(#([0-9a-f]+)([0-3])\\)");
    // private static Pattern edgePattern = Pattern.compile("^([RDLU]) ([0-9]+)");

    private List<Edge> edges = new ArrayList<>();
    private NavigableSet<Edge> verticalEdgesByX = new TreeSet<>();
    private NavigableSet<Edge> horizontalEdgesByX = new TreeSet<>();
    private NavigableSet<Edge> verticalEdgesByY = new TreeSet<>((e1, e2) -> e1.compareToY(e2));
    private NavigableSet<Edge> horizontalEdgesByY = new TreeSet<>((e1, e2) -> e1.compareToY(e2));
    private Coord2D edgeEndLocation = new Coord2D(0, 0);
    private Coord2D lowestCoord = new Coord2D(0, 0);
    private Coord2D highestCoord = new Coord2D(0, 0);
    private long edgeLength = 0;

    public long totalEdgeLength() {
        return edgeLength - (edges.size() + 1);
    }

    public long deltaY() {
        return highestCoord.y() - lowestCoord.y();
    }

    public long deltaX() {
        return highestCoord.x() - lowestCoord.x();
    }

    public long offsetX() {
        return lowestCoord.x();
    }

    public long offsetY() {
        return lowestCoord.y();
    }

    public List<Edge> edges() {
        return edges;
    }

    public void addEdge(String line) {
        Matcher edgeMatcher = edgePattern.matcher(line);
        edgeMatcher.find();
        Direction direction = Direction.fromNumber(edgeMatcher.group(2).charAt(0)).get();
        long length = Long.parseLong(edgeMatcher.group(1), 16);
        // Direction direction =
        // Direction.fromSymbol(edgeMatcher.group(1).charAt(0)).get();
        // long length = Long.parseLong(edgeMatcher.group(2), 10);
        Edge newEdge;
        if (edges.size() > 0) {
            Edge previousEdge = edges.get(edges.size() - 1);
            Edge firstEdge = edges.get(0);
            newEdge = new Edge(direction, length, previousEdge, firstEdge, edgeEndLocation);
            previousEdge.setNext(newEdge);
            firstEdge.setPrevious(newEdge);
        } else {
            newEdge = new Edge(direction, length, null, null, edgeEndLocation);
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            verticalEdgesByX.add(newEdge);
            verticalEdgesByY.add(newEdge);
        } else {
            horizontalEdgesByX.add(newEdge);
            horizontalEdgesByY.add(newEdge);
        }
        edgeEndLocation = newEdge.endCoord();
        lowestCoord.setX(Math.min(lowestCoord.x(), edgeEndLocation.x()));
        lowestCoord.setY(Math.min(lowestCoord.y(), edgeEndLocation.y()));
        highestCoord.setX(Math.max(highestCoord.x(), edgeEndLocation.x()));
        highestCoord.setY(Math.max(highestCoord.y(), edgeEndLocation.y()));
        edgeLength += newEdge.length();
        edges.add(newEdge);
    }

    public boolean edgesReverseDirection() {
        boolean reverses = false;
        Iterator<Edge> edgeIterator = edges.iterator();
        Direction previousDirection = null;
        while (!reverses && edgeIterator.hasNext()) {
            Direction currentDirection = edgeIterator.next().direction();
            if (previousDirection != null) {
                if (currentDirection == previousDirection.opposite()) {
                    reverses = true;
                }
            }
            previousDirection = currentDirection;
        }
        return reverses;
    }

    public boolean edgesAlwaysChangeDirection() {
        boolean alwaysChanges = true;
        Iterator<Edge> edgeIterator = edges.iterator();
        Direction previousDirection = null;
        while (alwaysChanges && edgeIterator.hasNext()) {
            Direction currentDirection = edgeIterator.next().direction();
            if (previousDirection != null) {
                if (currentDirection == previousDirection) {
                    alwaysChanges = false;
                }
            }
            previousDirection = currentDirection;
        }
        return alwaysChanges;
    }

    public long getInteriorSize() {
        determineInteriorSides();
        List<Rectangle> rectangles = formEdgesIntoRectangles();
        return rectangles.stream().mapToLong(rec -> rec.getArea()).sum();
    }

    private List<Rectangle> formEdgesIntoRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        List<LineSegment> allLines = new ArrayList<>();
        Edge currentEdge = edges.get(0);
        while (allLines.size() < edges.size()) {
            LineSegment newLineSegment = new LineSegment(currentEdge);
            if (newLineSegment.normal().isVertical()) {
                if (currentEdge.previous().interiorSide() == currentEdge.next().interiorSide()) {
                    if (currentEdge.previous().interiorSide() == Direction.LEFT) {
                        newLineSegment.lowerCoord().addX(1);
                    } else if (currentEdge.previous().interiorSide() == Direction.RIGHT) {
                        newLineSegment.higherCoord().addX(-1);
                    } else {
                        System.err.println("Shouldn't happen!");
                    }
                } else {
                    Edge leftEdge = currentEdge.previous().startCoord().x() < currentEdge.next().startCoord().x() ? currentEdge.previous() : currentEdge.next();
                    Edge rightEdge = currentEdge.previous() == leftEdge ? currentEdge.next() : currentEdge.previous();
                    if (leftEdge.interiorSide() == Direction.LEFT && rightEdge.interiorSide() == Direction.RIGHT) {
                        newLineSegment.lowerCoord().addX(1);
                        newLineSegment.higherCoord().addX(-1);
                        if (newLineSegment.length() == 0) {
                            System.err.println("Stalagmite or stalactite too narrow");
                        }
                    }
                }
            }
            allLines.add(newLineSegment);
            currentEdge = currentEdge.next();
        }

        NavigableSet<LineSegment> unassignedVerticalLinesByX = new TreeSet<>(allLines.stream().filter(line -> !line.normal().isVertical()).toList());
        NavigableSet<LineSegment> unassignedHorizontalLinesByY = new TreeSet<>((e1, e2) -> e1.compareToY(e2));
        unassignedHorizontalLinesByY.addAll(allLines.stream().filter(segment -> segment.normal().isVertical()).toList());
        while (!unassignedHorizontalLinesByY.isEmpty()) {
            LineSegment top = unassignedHorizontalLinesByY.first();
            LineSegment bottom;
            if (top.normal() == Direction.UP) {
                bottom = top;
            } else {
                bottom = unassignedHorizontalLinesByY.higher(top);
                while (bottom != null && (bottom.normal() != Direction.UP || !top.overlapsX(bottom))) {
                    bottom = unassignedHorizontalLinesByY.higher(bottom);
                }
            }
            if (bottom == null) {
                unassignedHorizontalLinesByY.remove(top);
            } else {
                Rectangle newRectangle = new Rectangle(top, bottom);
                boolean allSidesMatch = true;
                for (Direction direction : EnumSet.of(Direction.UP, Direction.DOWN)) {
                    LineSegment side = newRectangle.getSide(direction);
                    LineSegment oppositeSide = newRectangle.getSide(direction.opposite());
                    if (!side.fitsWithin(oppositeSide)) {
                        allSidesMatch = false;
                        List<LineSegment> newSegments = side.generateOppositeLines(oppositeSide);
                        unassignedHorizontalLinesByY.remove(side);
                        unassignedHorizontalLinesByY.addAll(newSegments);
                        break;
                    }
                }
                if (allSidesMatch) {
                    unassignedHorizontalLinesByY.remove(top);
                    unassignedHorizontalLinesByY.remove(bottom);
                    LineSegment left = newRectangle.getSide(Direction.LEFT);
                    removeSubsetLines(unassignedVerticalLinesByX, left);
                    LineSegment right = newRectangle.getSide(Direction.RIGHT);
                    removeSubsetLines(unassignedVerticalLinesByX, right);

                    rectangles.add(newRectangle);
                    // System.out.printf("Rectangle area is %d%n", newRectangle.getArea());
                    // printRectangles(rectangles);
                }
            }
        }
        for (LineSegment verticalLine : unassignedVerticalLinesByX) {
            if (verticalLine.length() >= 3) {
                Coord2D topCoord = new Coord2D(verticalLine.lowerCoord().x(), verticalLine.lowerCoord().y() + 1);
                Coord2D bottomCoord = new Coord2D(verticalLine.higherCoord().x(), verticalLine.higherCoord().y() - 1);
                LineSegment top = new LineSegment(topCoord, topCoord, Direction.DOWN);
                LineSegment bottom = new LineSegment(bottomCoord, bottomCoord, Direction.UP);
                Rectangle newRectangle = new Rectangle(top, bottom);
                rectangles.add(newRectangle);
                // System.out.printf("Rectangle area is %d%n", newRectangle.getArea());
                // printRectangles(rectangles);
            }
        }
        // printRectanglesToFile(rectangles);
        return rectangles;
    }

    private void removeSubsetLines(NavigableSet<LineSegment> unassignedVerticalLinesByX, LineSegment left) {
        LineSegment next = unassignedVerticalLinesByX.floor(left);
        while (next != null && next.isSubsetOf(left)) {
            unassignedVerticalLinesByX.remove(next);
            next = unassignedVerticalLinesByX.floor(left);
        }
        next = unassignedVerticalLinesByX.ceiling(left);
        while (next != null && next.isSubsetOf(left)) {
            unassignedVerticalLinesByX.remove(next);
            next = unassignedVerticalLinesByX.ceiling(left);
        }
    }

    private void determineInteriorSides() {
        Edge highestHorizontalEdge = horizontalEdgesByY.first();
        highestHorizontalEdge.setInteriorSide(Direction.DOWN);
        Edge previousEdge = highestHorizontalEdge;
        Edge nextEdge = highestHorizontalEdge.next();
        while (nextEdge.interiorSide() == null) {
            boolean nextEdgeTowardsInteriorSide = nextEdge.direction() == previousEdge.interiorSide();
            if (nextEdgeTowardsInteriorSide) {
                nextEdge.setInteriorSide(previousEdge.interiorSide().next());
            } else {
                nextEdge.setInteriorSide(previousEdge.interiorSide().previous());
            }
            previousEdge = nextEdge;
            nextEdge = previousEdge.next();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        drawEdges(rowStringBuilders()).forEach(row -> sb.append(row));
        return sb.toString();
    }

    public void printRectanglesToFile(List<Rectangle> rectangles) {
        try (java.io.BufferedWriter bWriter = Files.newBufferedWriter(new File(String.format("outputs/output_182/rectangles.txt")).toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
            bWriter.write(getRectanglesToString(rectangles));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printRectangles(List<Rectangle> rectangles) {
        System.out.println(getRectanglesToString(rectangles));
    }

    public String getRectanglesToString(List<Rectangle> rectangles) {
        StringBuilder sb = new StringBuilder();
        List<StringBuilder> rows = rowStringBuilders();
        int rectangleNumber = 0;
        for (Rectangle rectangle : rectangles) {
            rectangleNumber++;
            List<Coord2D> corners = rectangle.getAllCornerCoords();
            for (int cornerNumber = 0; cornerNumber < corners.size(); cornerNumber++) {
                Coord2D startCoord = corners.get(cornerNumber);
                Coord2D endCoord = corners.get((cornerNumber + 1) % corners.size());
                Direction direction = Direction.getDirection(startCoord, endCoord);
                Coord2D walker = new Coord2D(startCoord);
                rows.get((int) (walker.y() - lowestCoord.y())).setCharAt((int) (walker.x() - lowestCoord.x()), Integer.toString(rectangleNumber % 10).charAt(0));
                while (!walker.equals(endCoord)) {
                    walker.go(direction, 1);
                    rows.get((int) (walker.y() - lowestCoord.y())).setCharAt((int) (walker.x() - lowestCoord.x()), Integer.toString(rectangleNumber % 10).charAt(0));
                }
            }
        }
        rows.forEach(row -> sb.append(row));
        return sb.toString();
    }

    private List<StringBuilder> drawEdges(List<StringBuilder> rows) {
        for (Edge edge : edges) {
            Coord2D walker = new Coord2D(edge.startCoord());
            rows.get((int) (walker.y() - lowestCoord.y())).setCharAt((int) (walker.x() - lowestCoord.x()), edge.interiorSide().symbol());
            while (!walker.equals(edge.endCoord())) {
                walker.go(edge.direction(), 1);
                rows.get((int) (walker.y() - lowestCoord.y())).setCharAt((int) (walker.x() - lowestCoord.x()), edge.interiorSide().symbol());
            }
        }
        return rows;
    }

    private List<StringBuilder> rowStringBuilders() {
        List<StringBuilder> rows = new ArrayList<>();
        for (long y = lowestCoord.y(); y <= highestCoord.y(); y++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (long x = lowestCoord.x(); x <= highestCoord.x(); x++) {
                rowBuilder.append('.');
            }
            rowBuilder.append(String.format("%n"));
            rows.add(rowBuilder);
        }
        return rows;
    }
}