package org.snaker.engine.model;

import org.snaker.engine.core.Execution;
import org.snaker.engine.handlers.impl.MergeBranchWithWeightHandler;

/**
 *
 * @author liuwl
 *
 */
public class ExtJoinModel extends JoinModel {
    @Override
    public void exec(final Execution execution) {
        fire(new MergeBranchWithWeightHandler(this), execution);
        if (execution.isMerged()) {
            runOutTransition(execution);
        }
    }
}
