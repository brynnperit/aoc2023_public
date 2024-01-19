package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

public class solver082 {
    private static Pattern directionLinePattern = Pattern.compile(" = ");
    private static Pattern leftBranchPattern = Pattern.compile("\\(([A-Z0-9]{3}),");
    private static Pattern rightBranchPattern = Pattern.compile(" ([A-Z0-9]{3})\\)");
    private static Pattern labelPattern = Pattern.compile("([A-Z0-9]{3}) =");
    private static Pattern instructionPattern = Pattern.compile("[RL]");

    private static Collection<Instruction> instructionList = new ArrayList<>();

    private static Map<String, Node> nodeMap = new TreeMap<>();
    private static List<Node> startNodes = new ArrayList<>();

    private static class Node {
        private static Pattern startPattern = Pattern.compile("[A-Z0-9]{2}([A])");
        private static Pattern endPattern = Pattern.compile("[A-Z0-9]{2}([Z])");
        private Node left;
        private Node right;
        private final boolean isEndingNode;
        private final String label;

        public Node(String label, Collection<Node> startNodes, Map<String, Node> nodeMap) {
            this.label = label;
            Matcher endPatternMatcher = endPattern.matcher(label);
            if (endPatternMatcher.find()) {
                isEndingNode = true;
            } else {
                isEndingNode = false;
                Matcher startPatternMatcher = startPattern.matcher(label);
                if (startPatternMatcher.find()) {
                    startNodes.add(this);
                }
            }
            nodeMap.put(label, this);
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public boolean isEndingNode() {
            return isEndingNode;
        }

        @SuppressWarnings("unused")
        public String getLabel() {
            return label;
        }

        public Node followInstructions(Collection<Instruction> instructionsToFollow,
                Set<NodeFinishedStep> finishedSteps, long initialStepNumber) {
            Iterator<Instruction> instructionIterator = instructionsToFollow.iterator();
            return instructionIterator.next().followInstructionList(instructionIterator, this, finishedSteps,
                    initialStepNumber);
        }
    }

    private enum Instruction {
        L() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode,
                    Set<NodeFinishedStep> finishedSteps, long stepNumber) {
                return instructionListFollower(instructionListIterator, currentNode, currentNode.getLeft(),
                        finishedSteps, stepNumber);
            }
        },
        R() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode,
                    Set<NodeFinishedStep> finishedSteps, long stepNumber) {
                return instructionListFollower(instructionListIterator, currentNode, currentNode.getRight(),
                        finishedSteps, stepNumber);
            }
        };

        public abstract Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode,
                Set<NodeFinishedStep> finishedSteps, long stepNumber);

        private static void addNodeToFinishedSetIfFinished(Node possiblyFinishedNode,
                Set<NodeFinishedStep> finishedSteps, long stepNumber) {
            if (possiblyFinishedNode.isEndingNode()) {
                finishedSteps.add(new NodeFinishedStep(possiblyFinishedNode, stepNumber));
            }
        }

        private static Node instructionListFollower(Iterator<Instruction> instructionListIterator, Node currentNode,
                Node nextNode, Set<NodeFinishedStep> finishedSteps, long stepNumber) {
            addNodeToFinishedSetIfFinished(currentNode, finishedSteps, stepNumber);
            if (instructionListIterator.hasNext()) {

                return instructionListIterator.next().followInstructionList(instructionListIterator,
                        nextNode, finishedSteps, stepNumber+1);
            } else {
                return nextNode;
            }
        }
    }

    private static void processInputLine(String line) {
        Matcher directionLinePatternMatcher = directionLinePattern.matcher(line);
        if (directionLinePatternMatcher.find()) {
            Matcher labelPatternMatcher = labelPattern.matcher(line);
            Matcher leftBranchMatcher = leftBranchPattern.matcher(line);
            Matcher rightBranchMatcher = rightBranchPattern.matcher(line);

            labelPatternMatcher.find();
            leftBranchMatcher.find();
            rightBranchMatcher.find();
            String label = labelPatternMatcher.group(1);
            String leftBranch = leftBranchMatcher.group(1);
            String rightBranch = rightBranchMatcher.group(1);
            Node leftBranchNode;
            if (nodeMap.containsKey(leftBranch)) {
                leftBranchNode = nodeMap.get(leftBranch);
            } else {
                leftBranchNode = new Node(leftBranch, startNodes, nodeMap);
            }

            Node rightBranchNode;
            if (nodeMap.containsKey(rightBranch)) {
                rightBranchNode = nodeMap.get(rightBranch);
            } else {
                rightBranchNode = new Node(rightBranch, startNodes, nodeMap);
            }
            Node labelNode;
            if (nodeMap.containsKey(label)) {
                labelNode = nodeMap.get(label);
            } else {
                labelNode = new Node(label, startNodes, nodeMap);
            }
            labelNode.setLeft(leftBranchNode);
            labelNode.setRight(rightBranchNode);

        } else {
            Matcher instructionPatternMatcher = instructionPattern.matcher(line);
            while (instructionPatternMatcher.find()) {
                instructionList.add(Instruction.valueOf(instructionPatternMatcher.group()));
            }
        }
    }

    protected record NodeFinishedStep(Node node, long step) {
    };

    private static boolean hasSolution(Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps) {
        boolean allSetsComplete = finishedNodeSteps.values().stream().map(set -> set.stream().filter(nfs->nfs.step()%instructionList.size()==0).count()>0).reduce(true,
                (a, b) -> a && b);
        return allSetsComplete;
    }

    private static long getSolution(Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps) {
        List<Long> stepCounts = finishedNodeSteps.values().stream().mapToLong(set -> set.stream().mapToLong(n -> n.step()).min().orElse(-1)).mapToObj(l->l).toList();
        return stepCounts.stream().mapToLong(i->i).map(i->i/instructionList.size()).reduce(1, (a,b)->a*b)*instructionList.size();
    }

    public static void main(String[] args) {
        long totalSteps = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week2/input_08").toPath())) {
            inputLines.forEach(solver082::processInputLine);
            //System.out.println("There are " + instructionList.size() + " instructions");
            long totalLoops = 0;
            Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps = new TreeMap<>();
            for (int nodeIndex = 0; nodeIndex < startNodes.size(); nodeIndex++) {
                finishedNodeSteps.put(nodeIndex, new HashSet<>());
            }
            List<Node> currentNodes = startNodes;

            while (!hasSolution(finishedNodeSteps)) {
                final List<Node> currentNodesCopy = List.copyOf(currentNodes);
                final long totalLoopsCopy = totalLoops;
                currentNodes = IntStream.range(0, currentNodes.size())
                        .mapToObj(i -> currentNodesCopy.get(i).followInstructions(instructionList,
                                finishedNodeSteps.get(i), totalLoopsCopy * instructionList.size()))
                        .collect(Collectors.toList());
                totalLoops++;
            }
            //finishedNodeSteps.values().stream().forEach(i->{System.out.println("node----");i.stream().forEach(j->System.out.println("Steps: "+ j.step()+", on cycle "+j.step()/instructionList.size()+" with remainder " + j.step()%instructionList.size()));});
            totalSteps = getSolution(finishedNodeSteps);
            // totalSteps
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("There are " + totalSteps + " steps required for all start nodes to reach an end node");
    }
}
