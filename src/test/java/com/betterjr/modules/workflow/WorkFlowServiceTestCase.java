// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月23日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import org.junit.Test;
import org.snaker.engine.IProcessService;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.data.WorkFlowBusinessType;
import com.betterjr.modules.workflow.data.WorkFlowInput;
import com.betterjr.modules.workflow.service.WorkFlowService;
import com.betterjr.modules.workflow.snaker.core.BetterSpringSnakerEngine;

/**
 * @author liuwl
 *
 */
public class WorkFlowServiceTestCase extends BasicServiceTest<WorkFlowService> {

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowService> getTargetServiceClass() {
        return WorkFlowService.class;
    }

    @Test
    public void testStartWorkFlow() {
        final WorkFlowService workFlowService = this.getServiceObject();

        final WorkFlowInput flowInput = new WorkFlowInput(300l, "资金方票据融资业务流程", 110l, 1000l, WorkFlowBusinessType.SUPPLIER_BILL_FINANCING);
        flowInput.setFactorCustNo(110l);
        flowInput.setCoreCustNo(112l);
        flowInput.setSupplierCustNo(111l);

        workFlowService.saveStart(flowInput);
    }

    @Test
    public void testQueryCurrentTask() {
        final WorkFlowService workFlowService = this.getServiceObject();

        workFlowService.queryCurrentTask(300l, 1);
    }

    @Test
    public void testExecTask1() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(300l, "e390711373e748c0a9c8bf1588022a21");
        workFlowService.savePassTask(flowInput);
    }

    @Test
    public void testExecTask2() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(200l, "0f61524b1efd43daa8bfbd2c38cb51b0");
        workFlowService.savePassTask(flowInput);
    }

    @Test
    public void testExecTask3() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(201l, "fe70d43bd9274a79ba6ebaa3139b3411");
        workFlowService.savePassTask(flowInput);
    }
    @Test
    public void testExecTask4() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(301l, "5b5d0845ce16479e986dabcd99779f1f");
        workFlowService.savePassTask(flowInput);
    }
    @Test
    public void testExecTask5() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(202l, "6bb12f95ce70404d974533836bab80fd");
        workFlowService.savePassTask(flowInput);
    }
    @Test
    public void testExecTask6() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final WorkFlowInput flowInput = new WorkFlowInput(400l, "ab57973eb79c4836a585db927baa0fd1");
        workFlowService.savePassTask(flowInput);
    }

    @Test
    public void testDeployWorkFlow1() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(7l);
    }

    @Test
    public void testDeployWorkFlow2() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(8l);
    }

    @Test
    public void testDeployWorkFlow3() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(9l);
    }

    @Test
    public void testDeployWorkFlow4() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(10l);
    }
}
