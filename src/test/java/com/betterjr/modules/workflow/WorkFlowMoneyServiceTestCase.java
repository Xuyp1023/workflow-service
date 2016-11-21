// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.service.WorkFlowMoneyService;

/**
 * @author liuwl
 *
 */
public class WorkFlowMoneyServiceTestCase extends BasicServiceTest<WorkFlowMoneyService> {

    /* (non-Javadoc)
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowMoneyService> getTargetServiceClass() {
        return WorkFlowMoneyService.class;
    }

}
