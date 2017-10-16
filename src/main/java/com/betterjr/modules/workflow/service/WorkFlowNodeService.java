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
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.account.dubbo.interfaces.ICustOperatorService;
import com.betterjr.modules.account.entity.CustOperatorInfo;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowNodeMapper;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowNode;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowNodeService extends BaseService<WorkFlowNodeMapper, WorkFlowNode> {

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowStepService workFlowStepService;

    @Inject
    private WorkFlowMoneyService workFlowMoneyService;

    @Inject
    private WorkFlowApproverService workFlowApproverService;

    @Reference(interfaceClass = ICustOperatorService.class)
    private ICustOperatorService custOperatorService;

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
        final List<WorkFlowNode> workFlowNodes = this.selectByProperty(conditionMap, "seq");

        for (final WorkFlowNode workFlowNode : workFlowNodes) {
            fillOperator(workFlowNode);
        }
        return workFlowNodes;
    }

    /**
     * 查询可驳回节点
     * @param anId
     * @param anId2
     * @return
     */
    public List<SimpleDataEntity> queryRejectNodeList(final WorkFlowBase anWorkFlowBase,
            final WorkFlowNode anWorkFlowNode) {
        BTAssert.notNull(anWorkFlowBase, "没有找到流程！");
        BTAssert.notNull(anWorkFlowNode, "没有找到流程结点！");

        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("baseId", anWorkFlowBase.getId());
        conditionMap.put("LTseq", anWorkFlowNode.getSeq());
        conditionMap.put("type", new String[] { "2" }); // 只允许退回到经办和子流程

        final List<WorkFlowNode> workFlowNodes = this.selectByProperty(conditionMap, "seq DESC");
        return workFlowNodes.stream().map(workFlowNode -> {
            return new SimpleDataEntity(workFlowNode.getNickname(), workFlowNode.getName());
        }).collect(Collectors.toList());
    }

    /**
     * @param anWorkFlowNode
     */
    private void fillOperator(final WorkFlowNode anWorkFlowNode) {
        if (StringUtils.equals(anWorkFlowNode.getType(), WorkFlowConstants.NODE_TYPE_OPER)) { // 经办节点指定经办人
            final WorkFlowApprover approver = workFlowApproverService.findApproverByNode(anWorkFlowNode.getId());
            if (approver != null) {
                anWorkFlowNode.setOperId(approver.getOperId());
                final CustOperatorInfo operator = custOperatorService.findCustOperatorById(approver.getOperId());
                if (operator != null) {
                    anWorkFlowNode.setOperName(operator.getName());
                }
            }
        }
    }

    /**
     * 通过id 查找 WorkFlowNode
     *
     * @param anNodeId
     * @return
     */
    public WorkFlowNode findWorkFlowNodeById(final Long anNodeId) {
        return this.selectByPrimaryKey(anNodeId);
    }

    /**
     * 通过节点name + baseId 查询流程结点
     *
     * @param anName
     * @param anBaseId
     * @return
     */
    private WorkFlowNode findWorkFlowNodeByNameAndBaseId(final String anName, final Long anBaseId) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("baseId", anBaseId);

        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 检查节点是否可操作
     *
     * @param anBaseId
     * @param anNodeId
     */
    public WorkFlowNode checkWorkFlowNode(final Long anBaseId, final Long anNodeId) {
        workFlowBaseService.checkWorkFlowBase(anBaseId);

        final WorkFlowNode workFlowNode = findWorkFlowNodeById(anNodeId);
        // 检查节点是否存在
        BTAssert.notNull(workFlowNode, "没有找到对应节点");

        if (StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_OPER)
                || StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_APP)
                || StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_SUB)) {
            return workFlowNode;
        }
        throw new BytterException("当前节点不允许修改");
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
        final WorkFlowNode workFlowNode = checkWorkFlowNode(anBaseId, anNodeId);

        if (StringUtils.equals(WorkFlowConstants.IS_DISABLED, workFlowNode.getIsDisabled())) {
            throw new BytterException("当前节点已是禁用状态");
        }

        // 如果为未发布流程则修改为停用
        workFlowNode.setIsDisabled(WorkFlowConstants.IS_DISABLED);
        this.updateByPrimaryKeySelective(workFlowNode);

        return workFlowNode;
    }

    /**
     * 启用流程节点
     *
     * @param anBaseId
     * @param anNodeId
     * @return
     */
    public WorkFlowNode saveEnableWorkFlowNode(final Long anBaseId, final Long anNodeId) {
        // 检查流程是否存在
        final WorkFlowNode workFlowNode = checkWorkFlowNode(anBaseId, anNodeId);

        if (StringUtils.equals(WorkFlowConstants.NOT_DISABLED, workFlowNode.getIsDisabled())) {
            throw new BytterException("当前节点已是启用状态");
        }
        // 如果为未发布流程则修改为启用
        workFlowNode.setIsDisabled(WorkFlowConstants.NOT_DISABLED);
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
        if (StringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.IS_PUBLISHED)) {
            throw new BytterException("已发布流程不允许修改！");
        }

        final WorkFlowNode workFlowNode = findWorkFlowNodeById(anNodeId);
        // 检查节点是否存在
        BTAssert.notNull(workFlowNode, "没有找到对应节点");

        // 检查是否为经办节点
        if (StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_OPER) == false) {
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

    /**
     * copy到新的流程上
     *
     * @param anWorkFlowBase
     * @param anWorkFlowBase2
     */
    public void saveCopyWorkFlowNode(final WorkFlowBase anSourceBase, final WorkFlowBase anTargetBase) {
        if (StringUtils.equals(anSourceBase.getIsDefault(), WorkFlowConstants.IS_DEFAULT)) { // 从default copy过来
            queryWorkFlowNode(anSourceBase.getId()).forEach(tempWorkFlowNode -> {
                final WorkFlowNode workFlowNode = new WorkFlowNode();
                workFlowNode.initCopyValue(tempWorkFlowNode);
                workFlowNode.setBaseId(anTargetBase.getId());
                workFlowNode.setIsDisabled(WorkFlowConstants.NOT_DISABLED);
                this.insert(workFlowNode); // 不会有经办人，不会有金额段，故不考虑
            });
        } else { // 从上一版本copy过来
                 // 先copy 金额段，并将新旧金额段的 id映射保存
            final Map<Long, Long> workFlowMoneyMapping = new HashMap<>();
            workFlowMoneyService.saveCopyWorkFlowMoney(anSourceBase, anTargetBase, workFlowMoneyMapping);

            final WorkFlowBase workFlowBaseDefault = workFlowBaseService
                    .findDefaultWorkFlowBaseByName(anSourceBase.getName());
            BTAssert.notNull(workFlowBaseDefault, "找不到相应的默认流程");
            // 找到此流程对应的 default 流程 节点以 default 流程为准，然后叠加 最后一版的分配情况
            queryWorkFlowNode(workFlowBaseDefault.getId()).forEach(tempWorkFlowNodeDefault -> {
                final WorkFlowNode tempWorkFlowNode = findWorkFlowNodeByNameAndBaseId(tempWorkFlowNodeDefault.getName(),
                        anSourceBase.getId());

                final WorkFlowNode workFlowNode = new WorkFlowNode();
                if (tempWorkFlowNode != null) { // 有对应节点 需要做深度copy
                    workFlowNode.initCopyValue(tempWorkFlowNode);

                    if (StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_OPER)) {
                        // 处理经办人
                        workFlowApproverService.saveCopyWorkFlowApproverByNode(tempWorkFlowNode, workFlowNode);
                    } else if (StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_APP)) {
                        // copy 步骤
                        workFlowStepService.saveCopyWorkFlowStep(tempWorkFlowNode, workFlowNode, workFlowMoneyMapping);
                    }
                } else {
                    workFlowNode.initCopyValue(tempWorkFlowNodeDefault);
                }

                workFlowNode.setBaseId(anTargetBase.getId());
                this.insert(workFlowNode);
            });

        }
    }

}
