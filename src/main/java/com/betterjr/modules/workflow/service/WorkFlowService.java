// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.betterjr.modules.workflow.data.WorkFlowInput;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowService {
    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowStepService workFlowStepService;

    @Inject
    private WorkFlowMoneyService workFlowMoneyService;

    @Inject
    private WorkFlowApproverService workFlowApproverService;

    /**
     * 启动流程
     *
     * @param flowInput
     * @return
     */
    public WorkFlowBusiness saveStart(final WorkFlowInput flowInput) {
        // 启动流程，并处理第一个经办任务

        // 如果第一个节点为 子流程，则处理 子流程第一个经办任务

        return null;
    }

    public void saveExec() {

    }

    public void queryCurrentTask() {

    }

    public void queryHistoryTask() {

    }

    public void queryMonitorTask() {

    }

    public void saveChangeApprover() {

    }
}
