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

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowNodeMapper;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowNode;

/**
 * @author liuwl
 *
 */
public class WorkFlowNodeService extends BaseService<WorkFlowNodeMapper, WorkFlowNode> {

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowApproverService workFlowApproverService;

    /**
     * 查询流程的所有节点，按顺序查询
     *
     * @param anBaseId
     * @return
     */
    public List<WorkFlowNode> queryWorkFlowNode(final Long anBaseId) {
        // 检查流程是否存在
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到流程");

        // 用baseId 做流程节点查询条件 按seq排序 查询流程所有节点
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("baseId", workFlowBase.getId());

        // 返回
        return this.selectByProperty(conditionMap, "seq");
    }

    /**
     * 通过id 查找 WorkFlowNode
     *
     * @param anNodeId
     * @return
     */
    private WorkFlowNode findWorkFlowNodeById(final Long anNodeId) {
        return this.selectByPrimaryKey(anNodeId);
    }

    /**
     * 停用流程节点
     *
     * @param anBaseId
     * @param anNodeId
     * @return
     */
    public WorkFlowNode saveDiableWorkFlowNode(final Long anBaseId, final Long anNodeId) {
        // 检查流程是否存在
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到流程");
        // 检查是否为未发布流程 (不能修改已发布的流程)
        if (BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.IS_PUBLISHED)) {
            throw new BytterException("已发布流程不允许修改！");
        }

        final WorkFlowNode workFlowNode = findWorkFlowNodeById(anNodeId);
        // 检查节点是否存在
        BTAssert.notNull(workFlowNode, "没有找到对应节点");

        // 如果为未发布流程则修改为停用
        workFlowNode.setIsDisabled(WorkFlowConstants.IS_DISABLED);
        this.updateByPrimaryKeySelective(workFlowNode);

        return workFlowNode;
    }

    /**
     * 给经办节点分配 经办操作员
     *
     * @param anBaseId
     * @param anNodeId
     * @param anOperId
     * @return
     */
    public WorkFlowNode saveAssignOperator(final Long anBaseId, final Long anNodeId, final Long anOperId) {
        // 检查流程是否存在
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到流程");
        // 检查是否为未发布流程 (不能修改已发布的流程)
        if (BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.IS_PUBLISHED)) {
            throw new BytterException("已发布流程不允许修改！");
        }

        final WorkFlowNode workFlowNode = findWorkFlowNodeById(anNodeId);
        // 检查节点是否存在
        BTAssert.notNull(workFlowNode, "没有找到对应节点");

        // 检查是否为经办节点
        if (BetterStringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_OPER) == false) {
            throw new BytterException("只有经办节点能指定操作员！");
        }

        // 查找是否已有经办人
        final WorkFlowApprover tempApprover = workFlowApproverService.findApproverByNode(anNodeId);

        if (tempApprover != null) {
            workFlowApproverService.saveDelWorkFlowApprover(tempApprover.getId());
        }

        final WorkFlowApprover approver = new WorkFlowApprover();
        approver.setOperId(anOperId);
        // 指定经办人 WorkFlowApprover
        workFlowApproverService.addApproverByNode(anNodeId, approver);
        return workFlowNode;
    }
}
