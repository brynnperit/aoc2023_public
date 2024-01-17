package com.brynnperit.aoc2023.week4.solver221;

import java.util.*;
import static java.util.stream.Collectors.*;

public class PileNode {
    private final RectangularPrism prism;
    private final List<PileNode> parents = new ArrayList<>();
    private final List<PileNode> children = new ArrayList<>();

    public PileNode() {
        this.prism = new RectangularPrism();
    }

    public PileNode(RectangularPrism prism) {
        this.prism = prism;
    }

    public void add(RectangularPrism newPrism) {
        PriorityQueue<PileNode> leafPileNodes = new PriorityQueue<>(
                (n1, n2) -> Integer.compare(n1.prism.getMaxAltitude(), n2.prism.getMaxAltitude()) * -1);
        Set<PileNode> visitedNodes = new HashSet<>();
        for (PileNode leaf : getLeafNodes()) {
            if (!visitedNodes.contains(leaf)) {
                visitedNodes.add(leaf);
                leafPileNodes.add(leaf);
            }
        }
        List<PileNode> foundLeaves = new ArrayList<>();
        boolean foundLeaf = false;
        int foundAltitude = Integer.MIN_VALUE;
        while (!leafPileNodes.isEmpty()) {
            PileNode currentLeaf = leafPileNodes.poll();
            if (foundLeaf) {
                if (currentLeaf.prism.getMaxAltitude() < foundAltitude) {
                    break;
                }
            }
            if (currentLeaf.prism.getMaxAltitude() <= newPrism.getMinAltitude()
                    && currentLeaf.prism.overlaps(newPrism)) {
                foundLeaves.add(currentLeaf);
                if (!foundLeaf) {
                    foundLeaf = true;
                    foundAltitude = currentLeaf.prism.getMaxAltitude();
                }
            } else {
                for (PileNode parent : currentLeaf.getParents()) {
                    if (!visitedNodes.contains(parent)) {
                        visitedNodes.add(parent);
                        leafPileNodes.add(parent);
                    }
                }
            }

        }
        if (foundLeaves.isEmpty()) {
            throw new IllegalStateException(String.format("New prism %s doesn't overlap base brick", newPrism));
        }
        newPrism.dropToAltitude(foundLeaves.get(0).prism.getMaxAltitude());
        PileNode newNode = new PileNode(newPrism);
        for (PileNode parentNode : foundLeaves) {
            parentNode.addChild(newNode);
            newNode.addParent(parentNode);
        }
    }

    public List<PileNode> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public List<PileNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private void addParent(PileNode parentNode) {
        parents.add(parentNode);
    }

    private void addChild(PileNode childNode) {
        children.add(childNode);
    }

    private List<PileNode> getLeafNodes() {
        return getLeafNodes(new HashSet<>());
    }

    private List<PileNode> getLeafNodes(Set<PileNode> visitedNodes) {
        visitedNodes.add(this);
        List<PileNode> leafNodes = new ArrayList<>();
        for (PileNode child : children) {
            if (!visitedNodes.contains(child)) {
                leafNodes.addAll(child.getLeafNodes(visitedNodes));
            }
        }
        if (leafNodes.isEmpty()) {
            leafNodes.add(this);
        }
        return leafNodes;
    }

    public Set<RectangularPrism> getRemovableBricks() {
        return getRemovablePileNodes().stream().map(n -> n.prism).collect(toSet());
    }

    private Set<PileNode> getRemovablePileNodes() {
        return getRemovablePileNodes(new HashSet<>());
    }

    private Set<PileNode> getRemovablePileNodes(Set<PileNode> visitedNodes) {
        visitedNodes.add(this);
        Set<PileNode> removablePileNodes = new HashSet<>();
        for (PileNode child : children) {
            if (!visitedNodes.contains(child)) {
                removablePileNodes.addAll(child.getRemovablePileNodes(visitedNodes));
            }
        }
        if (children.isEmpty() || children.stream().mapToInt(c -> c.parents.size()).min().getAsInt() > 1) {
            removablePileNodes.add(this);
        }
        return removablePileNodes;
    }

    public long getNumberOfBricksThatWouldFall() {
        Map<PileNode, Integer> visitedNodes = new HashMap<>();
        for (PileNode child : children) {
            child.getBricksFalling(visitedNodes);
        }
        return visitedNodes.values().stream().mapToInt(s -> s).sum();
    }

    private void getBricksFalling(Map<PileNode, Integer> visitedNodes) {
        Set<PileNode> fallingNodes = new HashSet<>();
        fallingNodes.add(this);
        cascadingFall(fallingNodes);
        visitedNodes.put(this, fallingNodes.size() - 1);
        for (PileNode child : children) {
            if (!visitedNodes.containsKey(child)) {
                child.getBricksFalling(visitedNodes);
            }
        }
    }

    private void cascadingFall(Set<PileNode> fallingNodes) {
        List<PileNode> fallingChildren = new ArrayList<>();
        for (PileNode child : children) {
            if (!fallingNodes.contains(child) && child.testFalling(fallingNodes)) {
                fallingNodes.add(child);
                fallingChildren.add(child);
            }
        }
        for (PileNode child : fallingChildren) {
            child.cascadingFall(fallingNodes);
        }
    }

    private boolean testFalling(Set<PileNode> fallingNodes) {
        boolean willFall = true;
        for (PileNode parent : parents) {
            if (!fallingNodes.contains(parent)) {
                willFall = false;
                break;
            }
        }
        return willFall;
    }

}
