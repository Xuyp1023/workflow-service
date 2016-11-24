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

        baseService.addWorkFlowBase(workFlowBase, -20000l, 102202019l);
    }

    @Test
    public void testAddWorkFlowBase1() {
        final WorkFlowBaseService baseService = this.getServiceObject();

        final WorkFlowBase workFlowBase = new WorkFlowBase();
        workFlowBase.setNickname("供应商票据融资申请流程");
        workFlowBase.setOperRole(WorkFlowConstants.SUPPLIER_USER);
        workFlowBase.setCategoryId(2L);

        baseService.addWorkFlowBase(workFlowBase, -20001l, 102202021l);
    }

    @Test
    public void testAddWorkFlowBase2() {
        final WorkFlowBaseService baseService = this.getServiceObject();

        final WorkFlowBase workFlowBase = new WorkFlowBase();
        workFlowBase.setNickname("供应商票据融资融资方案签约流程");
        workFlowBase.setOperRole(WorkFlowConstants.SUPPLIER_USER);
        workFlowBase.setCategoryId(2L);

        baseService.addWorkFlowBase(workFlowBase, -20002l, 102202021l);
    }

    @Test
    public void testAddWorkFlowBase3() {
        final WorkFlowBaseService baseService = this.getServiceObject();

        final WorkFlowBase workFlowBase = new WorkFlowBase();
        workFlowBase.setNickname("核心企业票据融资确认贸易背景流程");
        workFlowBase.setOperRole(WorkFlowConstants.CORE_USER);
        workFlowBase.setCategoryId(2L);

        baseService.addWorkFlowBase(workFlowBase, -20003l, 102200336l);
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
        baseService.savePublishWorkFlow(2l, "");
    }

}
