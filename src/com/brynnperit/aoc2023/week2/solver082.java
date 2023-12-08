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

        public String getLabel(){
            return label;
        }

        public Node followInstructions(Collection<Instruction> instructionsToFollow, Set<NodeFinishedStep> finishedSteps, long initialStepNumber) {
            Iterator<Instruction> instructionIterator = instructionsToFollow.iterator();
            return instructionIterator.next().followInstructionList(instructionIterator, this, finishedSteps, initialStepNumber);
        }
    }

    private enum Instruction {
        L() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode, Set<NodeFinishedStep> finishedSteps, long stepNumber) {
                addNodeToFinishedSetIfFinished(currentNode,finishedSteps, stepNumber);
                if (instructionListIterator.hasNext()) {
                    return instructionListIterator.next().followInstructionList(instructionListIterator,
                            currentNode.getLeft(), finishedSteps, stepNumber++);
                } else {
                    return currentNode.getLeft();
                }
            }
        },
        R() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode, Set<NodeFinishedStep> finishedSteps, long stepNumber) {
                addNodeToFinishedSetIfFinished(currentNode,finishedSteps, stepNumber);
                if (instructionListIterator.hasNext()) {
                    return instructionListIterator.next().followInstructionList(instructionListIterator,
                            currentNode.getRight(), finishedSteps, stepNumber++);
                } else {
                    return currentNode.getRight();
                }
            }
        };

        public abstract Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode, Set<NodeFinishedStep> finishedSteps, long stepNumber);

        public static void addNodeToFinishedSetIfFinished(Node possiblyFinishedNode, Set<NodeFinishedStep> finishedSteps, long stepNumber){
            if (possiblyFinishedNode.isEndingNode()){
                finishedSteps.add(new NodeFinishedStep(possiblyFinishedNode, stepNumber));
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

    protected record NodeFinishedStep(Node node, long step){};

    private static boolean hasSolution(Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps) {
        boolean allSetsHaveEntries = finishedNodeSteps.values().stream().map(set->!set.isEmpty()).reduce(true, (a,b)->a&&b);
        if (allSetsHaveEntries){
            return true;
        }
        return false;//nodesToCheck.stream().map(n -> n.isEndingNode()).reduce(true, (a, b) -> a && b);
    }

    private static long getSolution(Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps){
        return finishedNodeSteps.values().stream().mapToLong(set->set.stream().map(n->n.step()).reduce(Long.MAX_VALUE, (a,b) -> a < b ? a : b)).reduce(1, (a,b)->a*b);
    }

    public static void main(String[] args) {
        long totalSteps = -1;
        try (Stream<String> inputLines = Files.lines(new File("inputs/input_08").toPath())) {
            inputLines.forEach(solver082::processInputLine);

            long totalLoops = 0;
            Map<Integer, Set<NodeFinishedStep>> finishedNodeSteps = new TreeMap<>();
            for (int nodeIndex = 0; nodeIndex < startNodes.size(); nodeIndex++){
                finishedNodeSteps.put(nodeIndex, new HashSet<>());
            }
            List<Node> currentNodes = startNodes;
            
            while (!hasSolution(finishedNodeSteps)) {
                totalLoops++;
                final List<Node> currentNodesCopy = List.copyOf(currentNodes);
                final long totalLoopsCopy = totalLoops;
                currentNodes = IntStream.range(0, currentNodes.size()).mapToObj(i->currentNodesCopy.get(i).followInstructions(instructionList, finishedNodeSteps.get(i), totalLoopsCopy*instructionList.size())).collect(Collectors.toList());
            }
            totalSteps = getSolution(finishedNodeSteps);
            //totalSteps 
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("There are " + totalSteps + " steps required for all start nodes to reach an end node");
    }
}
