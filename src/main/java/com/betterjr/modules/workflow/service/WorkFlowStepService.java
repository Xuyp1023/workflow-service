// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.Map;

import com.betterjr.common.service.BaseService;
import com.betterjr.modules.workflow.dao.WorkFlowStepMapper;
import com.betterjr.modules.workflow.entity.WorkFlowStep;

/**
 * @author liuwl
 *
 */
public class WorkFlowStepService extends BaseService<WorkFlowStepMapper, WorkFlowStep> {
    /**
     * 添加流程步骤
     * @param anStep
     * @return
     */
    public WorkFlowStep addWorkFlowStep(final WorkFlowStep anStep) {
        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 将添加的步骤加到最后一步
        return null;
    }

    /**
     * 删除流程步骤
     * @param anId
     */
    public void saveDelWorkFlowStep(final Long anId) {
        // 检查当前步骤是否存在

        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 删除当前步骤

        // 调整受影响步骤顺序
    }

    /**
     * 上移流程步骤
     * @param anId
     */
    public void saveSetUpStep(final Long anId) {
        // 检查当前步骤是否存在

        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 判断当前步骤是否为第一步

        // 向上调速当前步骤的seq 并且将原来的上一步下调
    }

    /**
     * 下移流程步骤
     * @param anId
     */
    public void saveSetDownStep(final Long anId) {
        // 检查当前步骤是否存在

        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 判断当前步骤是否为最后一步

        // 调整当前步骤的seq 并且将原来的下一步上调
    }

    /**
     * 保存流程步骤定义
     * @param anDefMap
     */
    public void saveStepDefinition(final Long anStepId, final Map<String, Object> anDefMap) {
        // 检查当前步骤是否存在

        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 从map 中取到 步骤审批方式 ，是否启用金额段

        // 如果是 串行审批 并且未启用金额段  只接受一个 审批操作员, 否则报错

        // 如果是 串行审批 并且启用金额段 这时候需要根据金额段来匹配 每个金额段 只能一个审批操作员

        // 如果是并行审批，并且未启用金额段 可以选择多个审批操作员 并且每个操作员需要有权重值

        // 如果是并行审批，并且启用金额段 每个金额段可以选择多个审批操作员 并且每个操作员需要有权重值

        // 将原有数据清理
    }
}
