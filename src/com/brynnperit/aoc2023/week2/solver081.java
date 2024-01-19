package com.brynnperit.aoc2023.week2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class solver081 {
    private static final String START_NODE_STRING = "AAA";
    private static final String END_NODE_STRING = "ZZZ";

    private static Pattern directionLinePattern = Pattern.compile(" = ");
    private static Pattern leftBranchPattern = Pattern.compile("\\(([A-Z]{3}),");
    private static Pattern rightBranchPattern = Pattern.compile(" ([A-Z]{3})\\)");
    private static Pattern labelPattern = Pattern.compile("([A-Z]{3}) =");
    private static Pattern instructionPattern = Pattern.compile("[RL]");

    private static List<Instruction> instructionList = new ArrayList<>();

    private static Map<String, Node> nodeMap = new TreeMap<>();
    private static Node startNode;
    private static Node endNode;

    private static class Node {
        private Node left;
        private Node right;

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
    }

    private enum Instruction {
        L() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode) {
                if (instructionListIterator.hasNext()) {
                    return instructionListIterator.next().followInstructionList(instructionListIterator,
                            currentNode.getLeft());
                } else {
                    return currentNode.getLeft();
                }
            }
        },
        R() {
            public Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode) {
                if (instructionListIterator.hasNext()) {
                    return instructionListIterator.next().followInstructionList(instructionListIterator,
                            currentNode.getRight());
                } else {
                    return currentNode.getRight();
                }
            }
        };

        public abstract Node followInstructionList(Iterator<Instruction> instructionListIterator, Node currentNode);
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
                leftBranchNode = new Node();
                nodeMap.put(leftBranch, leftBranchNode);
            }

            Node rightBranchNode;
            if (nodeMap.containsKey(rightBranch)) {
                rightBranchNode = nodeMap.get(rightBranch);
            } else {
                rightBranchNode = new Node();
                nodeMap.put(rightBranch, rightBranchNode);
            }
            Node labelNode;
            if (nodeMap.containsKey(label)) {
                labelNode = nodeMap.get(label);
            } else {
                labelNode = new Node();
                nodeMap.put(label, labelNode);
            }
            labelNode.setLeft(leftBranchNode);
            labelNode.setRight(rightBranchNode);
            if (label.equals(START_NODE_STRING)) {
                startNode = labelNode;
            } else if (label.equals(END_NODE_STRING)) {
                endNode = labelNode;
            }

        } else {
            Matcher instructionPatternMatcher = instructionPattern.matcher(line);
            while (instructionPatternMatcher.find()) {
                instructionList.add(Instruction.valueOf(instructionPatternMatcher.group()));
            }
        }
    }

    public static void main(String[] args) {
        long totalSteps = 0;
        try (Stream<String> inputLines = Files.lines(new File("inputs/week2/input_08").toPath())) {
            inputLines.forEach(solver081::processInputLine);
            Node currentNode = startNode;
            while (currentNode != endNode) {
                totalSteps++;
                Iterator<Instruction> instructionListIterator = instructionList.iterator();
                Instruction firstInstruction = instructionListIterator.next();
                currentNode = firstInstruction.followInstructionList(instructionListIterator, currentNode);
            }
            totalSteps *= instructionList.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("There are " + totalSteps + " steps required to reach ZZZ from AAA");
    }
}
