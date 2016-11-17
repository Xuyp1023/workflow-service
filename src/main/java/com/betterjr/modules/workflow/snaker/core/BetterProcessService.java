// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.core;

import javax.inject.Inject;

import org.snaker.engine.core.ProcessService;

import com.betterjr.modules.workflow.service.WorkFlowBaseService;

/**
 * @author liuwl
 *
 */
public class BetterProcessService extends ProcessService {

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    /**
     * 根据 Better 流程定义发布流程
     * @param baseId 基础流程编号
     * @return String 流程定义id
     */
    @Override
    public String deploy(final Long baseId) {

        return "";
    }
}
