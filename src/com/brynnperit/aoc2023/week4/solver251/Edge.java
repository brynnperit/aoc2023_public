package com.brynnperit.aoc2023.week4.solver251;

import java.lang.IllegalArgumentException;

public record Edge(Node firstNode, Node secondNode) {

    public Node getOther(Node graphNode) {
        if (firstNode.equals(graphNode)){
            return secondNode;
        }else if (secondNode.equals(graphNode)){
            return firstNode;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(){
        return String.format("%s, %s", firstNode.name(),secondNode.name());
    }
}
