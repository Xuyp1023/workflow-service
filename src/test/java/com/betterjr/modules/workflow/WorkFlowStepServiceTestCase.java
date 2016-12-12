// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.entity.WorkFlowStep;
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

    @Test
    public void testQueryWorkFlowStepByNodeId() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();

    }

    @Test
    public void testAddWorkFlowStep() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();

        workFlowStepService.addWorkFlowStep(6l, 33l, "测试1步");

        workFlowStepService.addWorkFlowStep(6l, 33l, "测试2步");

        workFlowStepService.addWorkFlowStep(6l, 33l, "测试3步");
    }

    @Test
    public void testAddWorkFlowStep1() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        workFlowStepService.addWorkFlowStep(6l, 33l, "测试4步");
    }

    @Test
    public void testMoveUpWorkFlowStep() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        workFlowStepService.saveMoveDownStep(6l, 33l, 6l);
    }

    @Test
    public void testMoveDownWorkFlowStep() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        workFlowStepService.saveMoveUpStep(6l, 33l, 6l);
    }

    @Test
    public void testQueryWorkFlowStep() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();

        final List<WorkFlowStep> flowSteps = workFlowStepService.queryWorkFlowStepByNodeId(33l);
        flowSteps.forEach(System.out::println);
    }

    @Test
    public void testDefineWorkFlowStep1() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        final Map<String, Object> anDefMap = new HashMap<>();
        anDefMap.put("auditType", WorkFlowConstants.AUDIT_TYPE_SERIAL);
        anDefMap.put("isMoney", WorkFlowConstants.IS_MONEY_FALSE);


        anDefMap.put("approver", 100l);
        workFlowStepService.saveStepDefinition(6l, 33l, 6l, anDefMap);
    }

    @Test
    public void testDefineWorkFlowStep2() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        final Map<String, Object> anDefMap = new HashMap<>();
        anDefMap.put("auditType", WorkFlowConstants.AUDIT_TYPE_SERIAL);
        anDefMap.put("isMoney", WorkFlowConstants.IS_MONEY_TRUE);

        final Map<String, Object> appMap1 = new HashMap<>();
        appMap1.put("moneyId", 19l);
        appMap1.put("operId", 1000l);

        final Map<String, Object> appMap2 = new HashMap<>();
        appMap2.put("moneyId", 20l);
        appMap2.put("operId", 1000l);

        final Map<String, Object> appMap3 = new HashMap<>();
        appMap3.put("moneyId", 21l);
        appMap3.put("operId", 1000l);

        final Map<String, Object> appMap4 = new HashMap<>();
        appMap4.put("moneyId", 22l);
        appMap4.put("operId", 1000l);

        final Map[] apps = new Map[4];
        apps[0] = appMap1;
        apps[1] = appMap2;
        apps[2] = appMap3;
        apps[3] = appMap4;
        anDefMap.put("approver", apps);

        workFlowStepService.saveStepDefinition(6l, 33l, 5l, anDefMap);
    }

    @Test
    public void testDefineWorkFlowStep3() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        final Map<String, Object> anDefMap = new HashMap<>();
        anDefMap.put("auditType", WorkFlowConstants.AUDIT_TYPE_PARALLEL);
        anDefMap.put("isMoney", WorkFlowConstants.IS_MONEY_FALSE);

        final Map<String, Object> appMap1 = new HashMap<>();
        appMap1.put("weight", 50);
        appMap1.put("operId", 1000l);

        final Map<String, Object> appMap2 = new HashMap<>();
        appMap2.put("weight", 50);
        appMap2.put("operId", 1000l);


        final Map[] apps = new Map[2];
        apps[0] = appMap1;
        apps[1] = appMap2;

        anDefMap.put("approver", apps);

        workFlowStepService.saveStepDefinition(6l, 33l, 7l, anDefMap);
    }

    @Test
    public void testDefineWorkFlowStep4() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();
        final Map<String, Object> anDefMap = new HashMap<>();
        anDefMap.put("auditType", WorkFlowConstants.AUDIT_TYPE_PARALLEL);
        anDefMap.put("isMoney", WorkFlowConstants.IS_MONEY_TRUE);

        final Map<String, Object> appMap1 = new HashMap<>();
        appMap1.put("moneyId", 19l);

        final Map<String, Object> appMap11 = new HashMap<>();
        appMap11.put("weight", 50);
        appMap11.put("operId", 1000l);

        final Map[] opers1 = new Map[1];
        opers1[0] = appMap11;
        appMap1.put("opers", opers1);


        final Map<String, Object> appMap2 = new HashMap<>();
        appMap2.put("moneyId", 20l);
        final Map<String, Object> appMap21 = new HashMap<>();
        appMap21.put("weight", 50);
        appMap21.put("operId", 1000l);
        final Map<String, Object> appMap22 = new HashMap<>();
        appMap22.put("weight", 50);
        appMap22.put("operId", 1001l);
        final Map[] opers2 = new Map[2];
        opers2[0] = appMap21;
        opers2[1] = appMap22;
        appMap2.put("opers", opers2);

        final Map<String, Object> appMap3 = new HashMap<>();
        appMap3.put("moneyId", 21l);
        final Map<String, Object> appMap31 = new HashMap<>();
        appMap31.put("weight", 50);
        appMap31.put("operId", 1000l);
        final Map<String, Object> appMap32 = new HashMap<>();
        appMap32.put("weight", 50);
        appMap32.put("operId", 1001l);
        final Map[] opers3 = new Map[2];
        opers3[0] = appMap31;
        opers3[1] = appMap32;
        appMap3.put("opers", opers3);

        final Map<String, Object> appMap4 = new HashMap<>();
        appMap4.put("moneyId", 22l);
        final Map<String, Object> appMap41 = new HashMap<>();
        appMap41.put("weight", 50);
        appMap41.put("operId", 1001l);
        final Map[] opers4 = new Map[1];
        opers4[0] = appMap41;
        appMap4.put("opers", opers4);

        final Map[] apps = new Map[4];
        apps[0] = appMap1;
        apps[1] = appMap2;
        apps[2] = appMap3;
        apps[3] = appMap4;
        anDefMap.put("approver", apps);

        workFlowStepService.saveStepDefinition(6l, 33l, 8l, anDefMap);
    }

    @Test
    public void testDelWorkFlowStep() {
        final WorkFlowStepService workFlowStepService = this.getServiceObject();

        //workFlowStepService.saveDelWorkFlowStep(6l, 32l, 21l);
        workFlowStepService.saveDelWorkFlowStep(6l, 33l, 21l);
    }
}
