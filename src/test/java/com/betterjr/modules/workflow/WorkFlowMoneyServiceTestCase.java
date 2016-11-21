// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import org.junit.Test;

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

    @Test
    public void testAddWorkFlowMoney() {
        final WorkFlowMoneyService flowMoneyService = this.getServiceObject();
        final String moneySection = "0,20000,50000,100000,-1";

        flowMoneyService.saveWorkFlowMoney(2L, moneySection);
    }


}
