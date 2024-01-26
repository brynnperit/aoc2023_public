package com.brynnperit.aoc2023.week4.solver251;

import java.util.*;

public record Edge(Node firstNode, Node secondNode) {

    public Optional<Node> getOther(Node graphNode) {
        if (firstNode.equals(graphNode)){
            return Optional.of(secondNode);
        }else if (secondNode.equals(graphNode)){
            return Optional.of(firstNode);
        }
        return Optional.empty();
    }

    @Override
    public String toString(){
        return String.format("%s, %s", firstNode.name(),secondNode.name());
    }
}
