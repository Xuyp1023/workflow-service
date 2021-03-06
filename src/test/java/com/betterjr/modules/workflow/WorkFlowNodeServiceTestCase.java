// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import java.util.List;

import org.junit.Test;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.service.WorkFlowNodeService;

/**
 * @author liuwl
 *
 */
public class WorkFlowNodeServiceTestCase extends BasicServiceTest<WorkFlowNodeService> {

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowNodeService> getTargetServiceClass() {
        return WorkFlowNodeService.class;
    }

    @Test
    public void testQueryNode() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        final List<WorkFlowNode> flowNodes = flowNodeService.queryWorkFlowNode(6L);

        flowNodes.forEach(System.out::println);
    }

    @Test
    public void testDisableNode() {
        final WorkFlowNodeService flowNodeService = getServiceObject();

        //        flowNodeService.saveDiableWorkFlowNode(2l, 3l);
        flowNodeService.saveDiableWorkFlowNode(6l, 34l);
    }

    @Test
    public void testEnableNode() {
        final WorkFlowNodeService flowNodeService = getServiceObject();

        //        flowNodeService.saveDiableWorkFlowNode(2l, 3l);
        flowNodeService.saveEnableWorkFlowNode(6l, 34l);
    }

    @Test
    public void testAssignOper1() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(22l, 158l, 17108l);
    }
    @Test
    public void testAssignOper2() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(22l, 161l, 17112l);
    }
    @Test
    public void testAssignOper3() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(22l, 164l, 17108l);
    }

    @Test
    public void testAssignOper4() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(23l, 169l, 1000156l);
    }

    @Test
    public void testAssignOper5() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(24l, 174l, 1000157l);
    }

    @Test
    public void testAssignOper6() {
        final WorkFlowNodeService flowNodeService = getServiceObject();
        flowNodeService.saveAssignOperator(25l, 178l, 1000566l);
    }
}
