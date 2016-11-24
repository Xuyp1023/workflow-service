// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月24日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.data;

import org.snaker.engine.entity.HistoryOrder;

import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;

/**
 * 流程实例
 *
 * @author liuwl
 *
 */
public class WorkFlowHistoryOrder {
    private HistoryOrder historyOrder;
    private WorkFlowBase workFlowBase;
    private WorkFlowBusiness workFlowBusiness;

    public HistoryOrder getHistoryOrder() {
        return historyOrder;
    }

    public void setHistoryOrder(final HistoryOrder anHistoryOrder) {
        historyOrder = anHistoryOrder;
    }

    public WorkFlowBase getWorkFlowBase() {
        return workFlowBase;
    }

    public void setWorkFlowBase(final WorkFlowBase anWorkFlowBase) {
        workFlowBase = anWorkFlowBase;
    }

    public WorkFlowBusiness getWorkFlowBusiness() {
        return workFlowBusiness;
    }

    public void setWorkFlowBusiness(final WorkFlowBusiness anWorkFlowBusiness) {
        workFlowBusiness = anWorkFlowBusiness;
    }
}
