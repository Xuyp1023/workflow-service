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

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.account.dubbo.interfaces.ICustOperatorService;
import com.betterjr.modules.account.entity.CustOperatorInfo;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowApproverMapper;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.entity.WorkFlowStep;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowApproverService extends BaseService<WorkFlowApproverMapper, WorkFlowApprover> {
    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Reference(interfaceClass = ICustOperatorService.class)
    private ICustOperatorService custOperatorService;

    /**
     * 添加分配的经办人
     * @param anBaseId
     * @param anNodeId
     * @param anOperId
     */
    public WorkFlowApprover addApproverByNode(final Long anBaseId, final Long anNodeId, final Long anOperId) {
        workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        final CustOperatorInfo operator = custOperatorService.findCustOperatorById(anOperId);

        BTAssert.notNull(operator, "没有找到相应操作员");

        final WorkFlowApprover workFlowApprover = new WorkFlowApprover();

        workFlowApprover.setOperId(anOperId);

        return addApproverByNode(anNodeId, workFlowApprover);
    }

    /**
     * 添加分配的经办人
     *
     * @return
     */
    public WorkFlowApprover addApproverByNode(final Long anNodeId, final WorkFlowApprover anApprover) {
        // 选删除
        saveDelWorkFlowApprover(WorkFlowConstants.PARENT_TYPE_NODE, anNodeId);

        anApprover.setParentType(WorkFlowConstants.PARENT_TYPE_NODE);
        anApprover.setParentId(anNodeId);

        anApprover.initAddValue();

        this.insert(anApprover);
        return anApprover;
    }

    /**
     * 添加分配的经办人
     *
     * @return
     */
    public WorkFlowApprover addApproverByStep(final Long anStepId, final WorkFlowApprover anApprover) {
        anApprover.setParentType(WorkFlowConstants.PARENT_TYPE_STEP);
        anApprover.setParentId(anStepId);

        anApprover.initAddValue();

        this.insert(anApprover);
        return anApprover;
    }

    /**
     * 删除所有经办人
     *
     * @param anParentType
     * @param anParentId
     */
    public void saveDelWorkFlowApprover(final String anParentType, final Long anParentId) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("parentType", anParentType);
        conditionMap.put("parentId", anParentId);

        this.deleteByExample(conditionMap);
    }

    /**
     * 删除分配的经办人
     */
    public void saveDelWorkFlowApprover(final Long anId) {
        this.deleteByPrimaryKey(anId);
    }

    /**
     * 查找经办节点对应的经办操作员
     *
     * @param anNodeId
     * @return
     */
    public WorkFlowApprover findApproverByNode(final Long anNodeId) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("parentType", WorkFlowConstants.PARENT_TYPE_NODE);
        conditionMap.put("parentId", anNodeId);

        // 通过节点编号 与 类型 查找相应的审批员
        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 查询步骤对应的审批人
     *
     * @param anStepId
     * @return
     */
    public List<WorkFlowApprover> queryApproverByStep(final Long anStepId) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("parentType", WorkFlowConstants.PARENT_TYPE_STEP);
        conditionMap.put("parentId", anStepId);

        // 通过节点编号 与 类型 查找相应的审批员
        return this.selectByProperty(conditionMap);
    }

    /**
     * copy到新的流程上
     *
     * @param anWorkFlowNode
     * @param anTempWorkFlowNode
     */
    public void saveCopyWorkFlowApproverByNode(final WorkFlowNode anSourceNode, final WorkFlowNode anTargetNode) {
        final WorkFlowApprover workFlowApprover = findApproverByNode(anSourceNode.getId());
        if (workFlowApprover != null) { // 有指定经办人的情况下需要copy
            final WorkFlowApprover approver = new WorkFlowApprover();
            approver.setOperId(workFlowApprover.getOperId());
            this.addApproverByNode(anTargetNode.getId(), approver);
        }
    }

    /**
     * copy到新的流程上
     *
     * @param anWorkFlowNode
     * @param anTempWorkFlowNode
     */
    public void saveCopyWorkFlowApproverByStep(final WorkFlowStep anSourceStep, final WorkFlowStep anTargetStep,
            final Map<Long, Long> anWorkFlowMoneyMapping) {
        final List<WorkFlowApprover> workFlowApprovers = queryApproverByStep(anSourceStep.getId());
        if (Collections3.isEmpty(workFlowApprovers) == false) {
            for (final WorkFlowApprover workFlowApprover : workFlowApprovers) {
                final WorkFlowApprover approver = new WorkFlowApprover();

                final Long moneyId = workFlowApprover.getMoneyId();
                if (moneyId != null) {
                    final Long targetMoneyId = anWorkFlowMoneyMapping.get(moneyId);
                    if (targetMoneyId != null) {
                        approver.setMoneyId(targetMoneyId);
                    } else {
                        throw new BytterException("复制金额段关系发生错误！");
                    }
                }
                approver.setOperId(workFlowApprover.getOperId());
                approver.setWeight(workFlowApprover.getWeight());
                this.addApproverByStep(anTargetStep.getId(), approver);
            }
        }
    }

}
