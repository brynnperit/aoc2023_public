package com.brynnperit.aoc2023.week3.solver192;

import java.util.*;
import java.util.regex.*;

public class Workflow {
    final private static Pattern workStepPattern = Pattern.compile("([^\\{,]+)[\\},]");
    final private static Pattern namePattern = Pattern.compile("([a-z]+)\\{");

    final private String name;
    private List<WorkStep<String, PartRanges>> steps = new ArrayList<>();

    public String name() {
        return name;
    }

    public Workflow(String workflowString) {
        Matcher nameMatcher = namePattern.matcher(workflowString);
        nameMatcher.find();
        name = nameMatcher.group(1);
        Matcher workStepMatcher = workStepPattern.matcher(workflowString);
        while (workStepMatcher.find()) {
            addStep(workStepMatcher.group(1));
        }
    }

    private void addStep(String stepString) {
        steps.add(WorkStepType.getWorkStep(stepString).get());
    }

    public void getRanges(PartRanges currentRanges, List<PartRanges> resultList, Deque<PartRanges> nextRanges,
            Deque<String> nextDestinations) {
        for (WorkStep<String, PartRanges> step : steps) {
            if (!currentRanges.isValid()) {
                return;
            }
            WorkResult<String, PartRanges> result = step.apply(currentRanges);
            switch (result.resultType()) {
            case ACCEPTED:
                if (result.rangeResult().isPresent()) {
                    // This is a conditional accept, continue with this workflow
                    PartRanges resultRanges = result.rangeResult().get();
                    if (resultRanges.isValid()) {
                        resultList.add(resultRanges);
                        currentRanges = currentRanges.subtractDifferingRange(resultRanges);
                    }
                } else {
                    resultList.add(currentRanges);
                }
                break;
            case BRANCH:
                if (result.rangeResult().isPresent()) {
                    // Conditional branch, only branch out the returned ranges
                    // Remove the returned range from the normal range
                    PartRanges resultRanges = result.rangeResult().get();
                    if (resultRanges.isValid()) {
                        nextRanges.add(resultRanges);
                        nextDestinations.add(result.result().get());
                        currentRanges = currentRanges.subtractDifferingRange(resultRanges);
                    }
                } else {
                    nextRanges.add(currentRanges);
                    nextDestinations.add(result.result().get());
                }
                break;
            case REJECTED:
                if (result.rangeResult().isPresent()) {
                    // Conditional rejection, the range result is the range that can continue
                    // onwards.
                    currentRanges = currentRanges.subtractDifferingRange(result.rangeResult().get());
                }
                break;
            default:
                throw new IllegalStateException("Unhandled work result type present");
            }
        }
    }
}
