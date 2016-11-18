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
import com.betterjr.modules.workflow.service.WorkFlowBaseService;

/**
 * @author liuwl
 *
 */
public class WorkFlowBaseServiceTestCase extends BasicServiceTest<WorkFlowBaseService> {

    /* (non-Javadoc)
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowBaseService> getTargetServiceClass() {
        return WorkFlowBaseService.class;
    }

    @Test
    public void testQueryDefaultWorkFlow() {
        final WorkFlowBaseService baseService = this.getServiceObject();

        baseService.queryDefaultWorkFlow("SUPPLIER_USER").forEach(System.out::println);
    }
}
