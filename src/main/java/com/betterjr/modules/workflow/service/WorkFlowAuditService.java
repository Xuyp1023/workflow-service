// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.betterjr.common.service.BaseService;
import com.betterjr.modules.workflow.dao.WorkFlowAuditMapper;
import com.betterjr.modules.workflow.entity.WorkFlowAudit;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowAuditService extends BaseService<WorkFlowAuditMapper, WorkFlowAudit> {

    /**
     * 添加流程审批结果
     * @param anWorkFlowAudit
     * @return
     */
    public WorkFlowAudit addWorkFlowAudit(final WorkFlowAudit anWorkFlowAudit) {
        anWorkFlowAudit.initAddValue();
        this.insert(anWorkFlowAudit);
        return anWorkFlowAudit;
    }

    public void queryWorkFlowAudit() {

    }

    /**
     * @param anOrderId
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowAudit> queryWorkFlowAuditByBusinessId(final Long anBusinessId, final int anFlag, final int anPageNum, final int anPageSize) {

        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("businessId", anBusinessId);

        return this.selectPropertyByPage(conditionMap, anPageNum, anPageSize, anFlag == 1);
    }
}
