package com.brynnperit.aoc2023.week3.solver191;

import java.util.*;
import java.util.regex.*;

public class Workflow {
    final private static Pattern workStepPattern = Pattern.compile("([^\\{,]+)[\\},]");
    final private static Pattern namePattern = Pattern.compile("([a-z]+)\\{");

    final private String name;
    private List<WorkStep<String>> steps = new ArrayList<>();

    public String name(){
        return name;
    }

    public Workflow(String workflowString){
        Matcher nameMatcher = namePattern.matcher(workflowString);
        nameMatcher.find();
        name = nameMatcher.group(1);
        Matcher workStepMatcher = workStepPattern.matcher(workflowString);
        while (workStepMatcher.find()){
            addStep(workStepMatcher.group(1));
        }
    }

    private void addStep(String stepString){
        steps.add(WorkStepType.getWorkStep(stepString).get());
    }

    public WorkResult<String> process(Part part){
        for (WorkStep<String> step : steps){
            WorkResult<String> result = step.apply(part);
            if (result.resultType() != WorkResultType.FAILED_TEST){
                return result;
            }
        }
        return null;
    }
}
