// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月24日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.data;

import org.snaker.engine.entity.Order;

import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;

/**
 * 流程实例
 *
 * @author liuwl
 *
 */
public class WorkFlowOrder {
    private Order order;
    private WorkFlowBase workFlowBase;
    private WorkFlowBusiness workFlowBusiness;

    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order anOrder) {
        order = anOrder;
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
