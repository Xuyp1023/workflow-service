// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

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

    public WorkFlowAudit addWorkFlowAudit(final WorkFlowAudit anWorkFlowAudit) {

        return anWorkFlowAudit;
    }

    public void queryWorkFlowAudit() {

    }
}
