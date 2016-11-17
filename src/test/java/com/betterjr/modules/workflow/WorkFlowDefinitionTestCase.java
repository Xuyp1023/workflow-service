// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import java.util.List;

import org.junit.Test;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Process;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.service.WorkFlowCategoryService;
import com.betterjr.modules.workflow.snaker.core.BetterSpringSnakerEngine;

/**
 * @author liuwl
 *
 */
public class WorkFlowDefinitionTestCase extends BasicServiceTest {

    /* (non-Javadoc)
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class getTargetServiceClass() {
        return WorkFlowCategoryService.class;
    }

    @Test
    public void test() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final QueryFilter queryFilter = new QueryFilter();
        final List<Process> processes = engine.process().getProcesss(queryFilter);

        System.out.println("Test" + processes);
    }
}
