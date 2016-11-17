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

import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowApproverMapper;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;

/**
 * @author liuwl
 *
 */
public class WorkFlowApproverService extends BaseService<WorkFlowApproverMapper, WorkFlowApprover> {
    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowStepService workFlowStepService;


    /**
     * 添加分配的经办人
     * @return
     */
    public WorkFlowApprover addApproverByNode(final Long anNodeId, final WorkFlowApprover anApprover) {
        // 检查步骤是否存在

        // 检查当前流程是否为未发布流程

        // 添加审批人

        return null;
    }


    /**
     * 添加分配的经办人
     * @return
     */
    public WorkFlowApprover addApproverByStep(final Long anNodeId, final WorkFlowApprover anApprover) {
        // 检查步骤是否存在

        // 检查当前流程是否为未发布流程

        // 添加审批人

        return null;
    }

    /**
     * 删除所有经办人
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
}
