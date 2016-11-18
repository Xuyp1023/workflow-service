// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.service.WorkFlowApproverService;

/**
 * @author liuwl
 *
 */
public class WorkFlowApproverServiceTestCase extends BasicServiceTest<WorkFlowApproverService> {

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowApproverService> getTargetServiceClass() {
        return WorkFlowApproverService.class;
    }

}
