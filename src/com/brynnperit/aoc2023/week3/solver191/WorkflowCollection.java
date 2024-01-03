package com.brynnperit.aoc2023.week3.solver191;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public boolean process(Part part) {
        WorkResult<String> result = null;
        while (result == null || !result.resultType().isFinalResult()) {
            String workflowName = result == null ? "in" : result.result().get();
            result = workflows.get(workflowName).process(part);
        }
        if (result.resultType() == WorkResultType.ACCEPTED) {
            return true;
        } else {
            return false;
        }
    }

}
