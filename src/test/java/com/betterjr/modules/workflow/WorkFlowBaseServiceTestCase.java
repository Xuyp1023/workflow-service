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
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
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

    @Test
    public void testAddWorkFlowBase() {
        final WorkFlowBaseService baseService = this.getServiceObject();

        final WorkFlowBase workFlowBase = new WorkFlowBase();
        workFlowBase.setNickname("资金方票据融资业务流程");
        workFlowBase.setOperRole(WorkFlowConstants.FACTOR_USER);
        workFlowBase.setCategoryId(2L);

        baseService.addWorkFlowBase(workFlowBase, -20000l, 110l);
    }

    @Test
    public void testDisableWorkFlowBase() {
        final WorkFlowBaseService baseService = this.getServiceObject();
        baseService.saveDisableWorkFlow(5l);
    }

    @Test
    public void testFindLatestVersionWorkFlowBase() {
        final WorkFlowBaseService baseService = this.getServiceObject();
        final WorkFlowBase workFlowBase = baseService.findWorkFlowBaseLatestByName("资金方票据融资业务流程", 110l);

        System.out.println(workFlowBase);
    }

    @Test
    public void testPublishWorkFlowBase() {
        final WorkFlowBaseService baseService = this.getServiceObject();
        baseService.savePublishWorkFlow(2l);
    }

}
