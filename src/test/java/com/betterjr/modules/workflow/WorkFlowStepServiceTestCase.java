// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.service.WorkFlowStepService;

/**
 * @author liuwl
 *
 */
public class WorkFlowStepServiceTestCase extends BasicServiceTest<WorkFlowStepService> {

    /* (non-Javadoc)
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowStepService> getTargetServiceClass() {
        return WorkFlowStepService.class;
    }

}
