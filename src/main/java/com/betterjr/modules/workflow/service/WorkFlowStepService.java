// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.dao.WorkFlowStepMapper;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.entity.WorkFlowStep;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowStepService extends BaseService<WorkFlowStepMapper, WorkFlowStep> {
    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowApproverService workFlowApproverService;

    /**
     * 检查步骤是否可以修改
     *
     * @param anBaseId
     * @param anNodeId
     * @param anStepId
     * @return
     */
    public WorkFlowStep checkWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 先检查节点是可修改
        workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        final WorkFlowStep workFlowStep = findWorkFlowStep(anNodeId, anStepId);

        BTAssert.notNull(workFlowStep, "没有找到该步骤");

        return workFlowStep;
    }

    /**
     * @param anNodeId
     * @param anStepId
     * @return
     */
    private WorkFlowStep findWorkFlowStep(final Long anNodeId, final Long anStepId) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("nodeId", anNodeId);
        conditionMap.put("id", anStepId);

        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 添加流程步骤
     *
     * @param anStep
     * @return
     */
    public WorkFlowStep addWorkFlowStep(final Long anBaseId, final Long anNodeId, final WorkFlowStep anStep) {
        // 检查当前步骤对应的流程是否有操作权限
        workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        anStep.setNodeId(anNodeId);
        anStep.initAddValue();
        // 将添加的步骤加到最后一步
        anStep.setSeq(queryWorkFlowStepByNodeId(anNodeId).size());

        this.insert(anStep);
        return anStep;
    }

    /**
     * 通过节点编号 查询步骤
     *
     * @param anNodeId
     * @return
     */
    public List<WorkFlowStep> queryWorkFlowStepByNodeId(final Long anNodeId) {
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("nodeId", anNodeId);

        return this.selectByProperty(conditionMap, "seq,ASC");
    }

    /**
     * 删除流程步骤
     *
     * @param anId
     */
    public void saveDelWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anId);

        // 删除当前步骤
        this.delete(workFlowStep);

        // 调整受影响步骤顺序
        saveAdjustStep(anBaseId, anNodeId);
    }

    /**
     * 调整步骤顺序
     *
     * @param anBaseId
     * @param anNodeId
     */
    private void saveAdjustStep(final Long anBaseId, final Long anNodeId) {
        // 先检查节点是可修改
        workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        final List<WorkFlowStep> flowSteps = queryWorkFlowStepByNodeId(anNodeId);

        for (int i = 0; i < flowSteps.size(); i++) {
            final WorkFlowStep workFlowStep = flowSteps.get(i);
            workFlowStep.setSeq(i);
            this.updateByPrimaryKeySelective(flowSteps.get(i));
        }
    }

    /**
     * 上移流程步骤
     *
     * @param anId
     */
    public void saveSetUpStep(final Long anBaseId, final Long anNodeId, final Long anId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anId);

        // 检查当前步骤是否存在

        // 判断当前步骤是否为第一步

        // 向上调速当前步骤的seq 并且将原来的上一步下调
    }

    /**
     * 下移流程步骤
     *
     * @param anId
     */
    public void saveSetDownStep(final Long anBaseId, final Long anNodeId, final Long anId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anId);

        // 检查当前步骤是否存在

        // 判断当前步骤是否为最后一步

        // 调整当前步骤的seq 并且将原来的下一步上调
    }

    /**
     * 保存流程步骤定义
     *
     * @param anDefMap
     */
    public void saveStepDefinition(final Long anBaseId, final Long anNodeId, final Long anStepId, final Map<String, Object> anDefMap) {
        // 检查当前步骤是否存在

        // 检查当前步骤对应的流程是否有操作权限

        // 检查当前步骤对应的流程 是否为 未发布状态

        // 从map 中取到 步骤审批方式 ，是否启用金额段

        // 如果是 串行审批 并且未启用金额段 只接受一个 审批操作员, 否则报错

        // 如果是 串行审批 并且启用金额段 这时候需要根据金额段来匹配 每个金额段 只能一个审批操作员

        // 如果是并行审批，并且未启用金额段 可以选择多个审批操作员 并且每个操作员需要有权重值

        // 如果是并行审批，并且启用金额段 每个金额段可以选择多个审批操作员 并且每个操作员需要有权重值

        // 将原有数据清理
    }

    /**
     * copy到新的节点上
     *
     * @param anWorkFlowMoneyMapping
     * @param anWorkFlowNode
     * @param anTempWorkFlowNode
     */
    public void saveCopyWorkFlowStep(final WorkFlowNode anSourceNode, final WorkFlowNode anTargetNode, final Map<Long, Long> anWorkFlowMoneyMapping) {
        // copy 节点
        queryWorkFlowStepByNodeId(anSourceNode.getId()).forEach(tempWorkFlowStep -> {
            final WorkFlowStep workFlowStep = new WorkFlowStep();

            workFlowStep.initCopyValue(tempWorkFlowStep);
            workFlowStep.setNodeId(anTargetNode.getId());

            // copy审批员
            workFlowApproverService.saveCopyWorkFlowApproverByStep(tempWorkFlowStep, workFlowStep, anWorkFlowMoneyMapping);
            this.insert(workFlowStep);
        });
    }
}
