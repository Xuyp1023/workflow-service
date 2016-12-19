// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月23日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import java.util.List;

import org.junit.Test;
import org.snaker.engine.IProcessService;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.constants.WorkFlowInput;
import com.betterjr.modules.workflow.data.WorkFlowBusinessType;
import com.betterjr.modules.workflow.data.WorkFlowHistoryOrder;
import com.betterjr.modules.workflow.data.WorkFlowOrder;
import com.betterjr.modules.workflow.data.WorkFlowTask;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.service.WorkFlowService;
import com.betterjr.modules.workflow.snaker.core.BetterSpringSnakerEngine;
import com.betterjr.modules.workflow.snaker.util.SnakerHelper;

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

        final WorkFlowInput flowInput = new WorkFlowInput(1000156l, "资金方票据融资业务流程", 102202019l, "1001l", WorkFlowBusinessType.SUPPLIER_BILL_FINANCING);
        flowInput.setFactorCustNo(102202019l);
        flowInput.setCoreCustNo(102200336l);
        flowInput.setSupplierCustNo(102202021l);
        flowInput.addParam("request_amount", 120000);

        final WorkFlowBusiness workFlowBusiness = workFlowService.saveStart(flowInput);

        System.out.println("到此" + workFlowBusiness);
    }

    @Test
    public void testQueryCurrentOrder() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final List<WorkFlowOrder> workFlowOrders = workFlowService.queryCurrentOrder(17108l, 1, 10);

        workFlowOrders.forEach(workFlowOrder-> {
            System.out.println("ORDER-PROCESSNAME:" + workFlowOrder.getProcessName());
            System.out.println("NAME:" + workFlowOrder.getWorkFlowBase().getName());
            System.out.println("PROCESSID:" + workFlowOrder.getWorkFlowBase().getProcessId());
            System.out.println("BUSINESSID:" + workFlowOrder.getWorkFlowBusiness().getBusinessId());
        });
    }

    @Test
    public void testQueryHistoryOrder() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final List<WorkFlowHistoryOrder> workFlowOrders = workFlowService.queryHistoryOrder(1000156l, 1, 10);

        workFlowOrders.forEach(workFlowOrder-> {
            System.out.println("ORDER-PROCESSNAME:" + SnakerHelper.getProcessName(workFlowOrder.getProcessId()));
            System.out.println("NAME:" + workFlowOrder.getWorkFlowBase().getName());
            System.out.println("PROCESSID:" + workFlowOrder.getWorkFlowBase().getProcessId());
            System.out.println("BUSINESSID:" + workFlowOrder.getWorkFlowBusiness().getBusinessId());
        });
    }

    @Test
    public void testQueryCurrentTask() {
        final WorkFlowService workFlowService = this.getServiceObject();
        final List<WorkFlowTask> workFlowTasks = workFlowService.queryCurrentTask(17112l, 1, 10);

        for (final WorkFlowTask workFlowTask: workFlowTasks) {
            System.out.println("TASK-ID:" + workFlowTask.getId() );
            System.out.println("TASK-PROCESSNAME:" + workFlowTask.getProcessName());
            System.out.println("NAME:" + workFlowTask.getWorkFlowStep().getName());
            System.out.println("FORM:" + workFlowTask.getWorkFlowNode().getForm());
            System.out.println("HANDLER:" + workFlowTask.getWorkFlowNode().getHandler());
            System.out.println("NICKNAME:" + workFlowTask.getWorkFlowStep().getNickname());
            System.out.println("PROCESSID:" + workFlowTask.getWorkFlowBase().getProcessId());
            System.out.println("BUSINESSID:" + workFlowTask.getWorkFlowBusiness().getBusinessId());
        }
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
        processService.deploy(22l);
    }

    @Test
    public void testDeployWorkFlow2() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(23l);
    }

    @Test
    public void testDeployWorkFlow3() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(24l);
    }

    @Test
    public void testDeployWorkFlow4() {
        final BetterSpringSnakerEngine engine = this.getCtx().getBean(BetterSpringSnakerEngine.class);

        final IProcessService processService = engine.process();
        processService.deploy(25l);
    }
}
