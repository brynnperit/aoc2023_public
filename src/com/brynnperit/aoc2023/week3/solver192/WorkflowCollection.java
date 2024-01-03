package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;
import java.util.regex.*;

public class WorkflowCollection {
    final private static Pattern workflowPattern = Pattern.compile("^[a-z]+\\{.+\\}$");
    private Map<String, Workflow> workflows = new HashMap<>();

    public boolean addWorkflow(String workFlowString) {
        Matcher workflowMatcher = workflowPattern.matcher(workFlowString);
        if (workflowMatcher.matches()) {
            Workflow newWorkFlow = new Workflow(workFlowString);
            workflows.put(newWorkFlow.name(), newWorkFlow);
            return true;
        }
        return false;
    }

    // public boolean process(Part part) {
    //     WorkResult<String> result = null;
    //     while (result == null || !result.resultType().isFinalResult()) {
    //         String workflowName = result == null ? "in" : result.result().get();
    //         result = workflows.get(workflowName).process(part);
    //     }
    //     if (result.resultType() == WorkResultType.ACCEPTED) {
    //         return true;
    //     } else {
    //         return false;
    //     }
    // }

    public List<PartRanges> getRanges() {
        List<PartRanges> resultList = new ArrayList<>();
        PartRanges initialPart = new PartRanges();
        Deque<PartRanges> nextRanges = new ArrayDeque<PartRanges>();
        Deque<String> nextDestinations = new ArrayDeque<String>();
        nextRanges.add(initialPart);
        nextDestinations.add("in");
        while (!nextRanges.isEmpty()) {
            PartRanges currentRanges = nextRanges.pop();
            String currentDestination = nextDestinations.pop();
            workflows.get(currentDestination).getRanges(currentRanges,resultList,nextRanges,nextDestinations);
        }
        return resultList;
    }

}
