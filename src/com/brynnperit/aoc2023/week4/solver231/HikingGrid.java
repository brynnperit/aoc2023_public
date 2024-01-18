package com.brynnperit.aoc2023.week4.solver231;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HikingGrid {
    final private List<List<HikingGridTile>> grid = new ArrayList<>();
    private Map<Boolean, NavigableMap<Coord2D, PathNode>> nodeSets = new HashMap<>();
    private Map<Boolean, PathNode> startNodes = new HashMap<>();
    private boolean hasBeenProcessed = false;

    public void addLine(String gridLine) {
        List<HikingGridTile> row = new ArrayList<>();
        for (char c : gridLine.toCharArray()) {
            row.add(HikingGridTile.fromSymbol(c).get());
        }
        grid.add(row);
        hasBeenProcessed = false;
    }

    public long getLongestPathLength(boolean respectSteepSlopes) {
        long longestPathLength = 0;
        processPaths();
        NavigableMap<Coord2D, PathNode> nodeMap = nodeSets.get(respectSteepSlopes);
        PathNode startNode = nodeMap.get(getStartPosition());
        PathNode endNode = nodeMap.get(getEndPosition());
        ConcurrentLinkedQueue<NodeExplorer> nodeExplorers = new ConcurrentLinkedQueue<NodeExplorer>();
        nodeExplorers.add(new NodeExplorer(startNode));
        while (!nodeExplorers.isEmpty()) {
            List<NodeExplorer> explorers = nodeExplorers.stream().toList();
            nodeExplorers.clear();
            longestPathLength = Math.max(longestPathLength, explorers.stream().parallel().mapToLong(ne -> exploreUntilEnd(ne, nodeExplorers, endNode)).max().orElse(-1));
        }
        return longestPathLength;
    }

    private long exploreUntilEnd(NodeExplorer currentExplorer, ConcurrentLinkedQueue<NodeExplorer> nodeExplorers, PathNode endNode) {
        PathNode currentNode = currentExplorer.currentNode();
        while (!currentNode.equals(endNode)) {
            Iterator<PathNode> nodeIterator = currentNode.getConnectedNodes().iterator();
            PathNode firstNextNode = null;
            while (nodeIterator.hasNext() && (firstNextNode == null || currentExplorer.hasVisited(firstNextNode))) {
                firstNextNode = nodeIterator.next();
            }
            while (nodeIterator.hasNext()) {
                PathNode nextNode = nodeIterator.next();
                if (!currentExplorer.hasVisited(nextNode)) {
                    NodeExplorer nextExplorer = new NodeExplorer(currentExplorer);
                    nextExplorer.visit(nextNode);
                    nodeExplorers.add(nextExplorer);
                }
            }
            if (!currentExplorer.hasVisited(firstNextNode)) {
                currentExplorer.visit(firstNextNode);
            }else{
                return -1;
            }
            currentNode = currentExplorer.currentNode();
        }
        return currentExplorer.lengthTravelled();
    }

    private void processPaths() {
        if (!hasBeenProcessed) {
            processPaths(true);
            processPaths(false);
            hasBeenProcessed = true;
        }
    }

    private void processPaths(boolean respectSteepSlopes) {
        NavigableMap<Coord2D, PathNode> pathSet = new TreeMap<>();
        Coord2D endPosition = getEndPosition();
        PathExplorer initialPath = new PathExplorer(getStartPosition(), endPosition, grid);
        PathNode startNode = new PathNode(initialPath.getPosition());
        startNodes.put(respectSteepSlopes, startNode);
        nodeSets.put(respectSteepSlopes, pathSet);
        pathSet.put(initialPath.getPosition(), startNode);
        Deque<PathExplorer> forkPaths = new ArrayDeque<PathExplorer>();
        Deque<PathNode> forkPathNodes = new ArrayDeque<PathNode>();
        forkPaths.add(initialPath);
        forkPathNodes.add(startNode);
        Set<Direction> validDirections = EnumSet.noneOf(Direction.class);
        while (!forkPaths.isEmpty()) {
            PathExplorer currentPath = forkPaths.poll();
            PathNode previousNode = forkPathNodes.poll();
            navigateToNextForkLocation(currentPath, validDirections, respectSteepSlopes);
            PathNode foundNode;
            Coord2D currentPosition = currentPath.getPosition();

            if (pathSet.containsKey(currentPosition)) {
                foundNode = pathSet.get(currentPosition);
            } else {
                foundNode = new PathNode(currentPosition);
                pathSet.put(currentPosition, foundNode);
                if (!currentPath.reachedDestination()) {
                    Iterator<Direction> directionIterator = validDirections.iterator();
                    while (directionIterator.hasNext()) {
                        PathExplorer newPath = new PathExplorer(currentPosition, endPosition, grid);
                        newPath.go(directionIterator.next());
                        forkPaths.add(newPath);
                        forkPathNodes.add(foundNode);
                    }
                }
            }
            if (!previousNode.getConnectedNodes().contains(foundNode) || (previousNode.getConnectedNodes().contains(foundNode) && previousNode.getDistanceTo(foundNode) < currentPath.pathLength())) {
                previousNode.addNode(foundNode, currentPath.pathLength());
                if (!respectSteepSlopes) {
                    foundNode.addNode(previousNode, currentPath.pathLength());
                }
            }
        }
    }

    private Coord2D getEndPosition() {
        int yPosition = grid.size() - 1;
        int xPosition = grid.get(yPosition).indexOf(HikingGridTile.PATH);
        return new Coord2D(xPosition, yPosition);
    }

    private Coord2D getStartPosition() {
        int yPosition = 0;
        int xPosition = grid.get(yPosition).indexOf(HikingGridTile.PATH);
        return new Coord2D(xPosition, yPosition);
    }

    private void navigateToNextForkLocation(PathExplorer toSolve, Set<Direction> validDirections, boolean respectSteepSlopes) {
        validDirections.clear();
        while (validDirections.size() <= 1 && !toSolve.reachedDestination()) {
            validDirections.clear();
            HikingGridTile currentTile = getTile(toSolve.positionX(), toSolve.positionY());
            if (!respectSteepSlopes && currentTile != HikingGridTile.FOREST) {
                currentTile = HikingGridTile.PATH;
            }
            for (Direction direction : currentTile.validDirections()) {
                int newX = toSolve.positionX() + direction.x();
                int newY = Math.max(0, toSolve.positionY() + direction.y());
                if (!toSolve.hasVisited(newX, newY)) {
                    HikingGridTile destinationTile = getTile(newX, newY);
                    if (!respectSteepSlopes && destinationTile != HikingGridTile.FOREST) {
                        destinationTile = HikingGridTile.PATH;
                    }
                    if (isValidDestination(destinationTile, direction)) {
                        validDirections.add(direction);
                    }
                }
            }
            if (validDirections.size() != 1) {
                return;
            }
            toSolve.go(validDirections.iterator().next());
        }
    }

    private boolean isValidDestination(HikingGridTile destinationTile, Direction direction) {
        if (destinationTile == HikingGridTile.FOREST) {
            return false;
        }
        if (destinationTile != HikingGridTile.PATH && direction == destinationTile.invalidEntryDirection()) {
            return false;
        }
        return true;
    }

    private HikingGridTile getTile(int x, int y) {
        return grid.get(y).get(x);
    }

    @Override
    public String toString(){
        return hikingGridWithNodesString(true);
    }

    private String hikingGridWithNodesString(boolean respectSteepSlopes) {
        processPaths();
        Set<Coord2D> nodeLocations = nodeSets.get(respectSteepSlopes).keySet();
        StringBuilder sb = new StringBuilder();
        List<StringBuilder> rowStringBuilders = new ArrayList<>();
        for (List<HikingGridTile> row : grid) {
            StringBuilder rowString = new StringBuilder();
            for (HikingGridTile tile : row) {
                rowString.append(tile.symbol());
            }
            rowString.append(String.format("%n"));
            rowStringBuilders.add(rowString);
        }
        for (Coord2D coord : nodeLocations) {
            rowStringBuilders.get(coord.y()).setCharAt(coord.x(), 'N');
        }
        rowStringBuilders.stream().forEachOrdered(rsb -> sb.append(rsb));
        return sb.toString();
    }
}
