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

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowStepMapper;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;
import com.betterjr.modules.workflow.entity.WorkFlowMoney;
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
    private WorkFlowMoneyService workFlowMoneyService;

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
     * @param anStepId
     */
    public void saveDelWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

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
     * @param anStepId
     */
    public void saveSetUpStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        final Integer currentSeq = workFlowStep.getSeq();
        if (currentSeq > 0) { // 不是第一步
            // 向上调整当前步骤的seq 并且将原来的上一步下调
            final Integer previousSeq = currentSeq - 1;
            final WorkFlowStep previousStep = findWorkFlowStepByNodeAndSeq(anNodeId, previousSeq);
            BTAssert.notNull(previousStep, "没有找到当前步骤的前一步骤！");

            workFlowStep.setSeq(-previousSeq);
            this.updateByPrimaryKeySelective(workFlowStep); // 避免唯一索引冲突
            previousStep.setSeq(currentSeq);
            this.updateByPrimaryKeySelective(previousStep);
            workFlowStep.setSeq(previousSeq);
            this.updateByPrimaryKeySelective(workFlowStep); // 改回来
        }
    }

    /**
     * 根据step nodeId 和 step seq 查找 step
     *
     * @param anNodeId
     * @param anPreviousSeq
     * @return
     */
    private WorkFlowStep findWorkFlowStepByNodeAndSeq(final Long anNodeId, final Integer anSeq) {
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("nodeId", anNodeId);
        conditionMap.put("seq", anSeq);

        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 下移流程步骤
     *
     * @param anStepId
     */
    public void saveSetDownStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        final List<WorkFlowStep> flowSteps = queryWorkFlowStepByNodeId(anNodeId);

        final Integer currentSeq = workFlowStep.getSeq();
        if (currentSeq < flowSteps.size() - 1) { // 不是最后一步
            // 调整当前步骤的seq 并且将原来的下一步上调
            final Integer nextSeq = currentSeq + 1;
            final WorkFlowStep nextStep = findWorkFlowStepByNodeAndSeq(anNodeId, nextSeq);
            BTAssert.notNull(nextStep, "没有找到当前步骤的前一步骤！");

            workFlowStep.setSeq(-nextSeq);
            this.updateByPrimaryKeySelective(workFlowStep); // 避免唯一索引冲突
            nextStep.setSeq(currentSeq);
            this.updateByPrimaryKeySelective(nextStep);
            workFlowStep.setSeq(nextSeq);
            this.updateByPrimaryKeySelective(workFlowStep); // 改回来
        }
    }

    /**
     * 保存流程步骤定义
     *
     * @param anDefMap
     */
    /**
     * 格式定义 { auditType: 0串行 1并行 审批方式 isMoney: 0未启用 1启用 金额段
     *
     * 0,0串行审批未启用金额段
     * approver:operId
     * 0,1串行审批启用金额段
     * approver:[ {moneyId: , operId: }, {moneyId: , operId: } ]
     * 1,0并行未启用金额段
     * approver:[ {operId: , weight:}, {operId: , weight: } ]
     * 1,1并行启用金额段
     * approver:[ {moneyId: , opers:[ {operId: , weight: }, {operId: , weight: } ]}, {moneyId: , opers:[ {operId: , weight: }, {operId: , weight: } ]} ] }
     */
    public void saveStepDefinition(final Long anBaseId, final Long anNodeId, final Long anStepId, final Map<String, Object> anDefMap) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        final String auditType = (String) anDefMap.get("auditType");
        final String isMoney = (String) anDefMap.get("isMoney");

        BTAssert.isTrue(BetterStringUtils.isNotBlank(auditType), "审批方式不允许为空！");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(isMoney), "金额段启用标识不允许为空！");

        workFlowStep.setAuditType(auditType);
        workFlowStep.setIsMoney(isMoney);

        final List<WorkFlowMoney> workFlowMoneys = workFlowMoneyService.queryWorkFlowMoneyByBaseId(anBaseId);

        // 将原有数据清理
        workFlowApproverService.saveDelWorkFlowApprover(WorkFlowConstants.PARENT_TYPE_STEP, anStepId);

        if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            // 如果是 串行审批 并且未启用金额段 只接受一个 审批操作员, 否则报错
            final Long operId = (Long) anDefMap.get("approver");
            BTAssert.notNull(operId, "操作员编号未找到！");

            final WorkFlowApprover approver = new WorkFlowApprover();
            approver.setOperId(operId);

            workFlowApproverService.addApproverByStep(anStepId, approver);
        }
        else if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            // 如果是 串行审批 并且启用金额段 这时候需要根据金额段来匹配 每个金额段 只能一个审批操作员
            final Map<String, Object>[] approvers = (Map<String, Object>[]) anDefMap.get("approver");

            BTAssert.isTrue(approvers.length == workFlowMoneys.size(), "金额段不匹配");

            for (final Map<String, Object> tempApprover : approvers) {
                final Long moneyId = (Long) tempApprover.get("moneyId");
                final Long operId = (Long) tempApprover.get("operId"); // 允许为空，为空为所有人
                checkWorkFlowMoney(moneyId, workFlowMoneys); // 检查moneyId 是否在当前流程定义金额段内

                final WorkFlowApprover approver = new WorkFlowApprover();
                approver.setOperId(operId);
                approver.setMoneyId(moneyId);

                workFlowApproverService.addApproverByStep(anStepId, approver);
            }
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            // 如果是并行审批，并且未启用金额段 可以选择多个审批操作员 并且每个操作员需要有权重值
            final Map<String, Object>[] approvers = (Map<String, Object>[]) anDefMap.get("approver");

            for (final Map<String, Object> tempApprover : approvers) {
                final Long operId = (Long) tempApprover.get("operId");
                final Integer weight = (Integer) tempApprover.get("weight");

                final WorkFlowApprover approver = new WorkFlowApprover();
                approver.setOperId(operId);
                approver.setWeight(weight);

                workFlowApproverService.addApproverByStep(anStepId, approver);
            }
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            // 如果是并行审批，并且启用金额段 每个金额段可以选择多个审批操作员 并且每个操作员需要有权重值
            final Map<String, Object>[] approvers = (Map<String, Object>[]) anDefMap.get("approver");

            for (final Map<String, Object> tempApprover : approvers) {
                final Long moneyId = (Long) tempApprover.get("moneyId");

                checkWorkFlowMoney(moneyId, workFlowMoneys); // 检查moneyId 是否在当前流程定义金额段内

                final Map<String, Object>[] opers = (Map<String, Object>[]) tempApprover.get("opers");

                for (final Map<String, Object> oper : opers) {
                    final Long operId = (Long) tempApprover.get("operId");
                    final Integer weight = (Integer) tempApprover.get("weight");

                    final WorkFlowApprover approver = new WorkFlowApprover();
                    approver.setMoneyId(moneyId);
                    approver.setOperId(operId);
                    approver.setWeight(weight);

                    workFlowApproverService.addApproverByStep(anStepId, approver);
                }
            }
        }
        else {
            throw new BytterException("审批方式和金额段标识不正确！");
        }
    }

    /**
     * 金额段合法性检测
     *
     * @param anMoneyId
     * @param anWorkFlowMoneys
     */
    private void checkWorkFlowMoney(final Long anMoneyId, final List<WorkFlowMoney> anWorkFlowMoneys) {
        BTAssert.notNull(anMoneyId, "金额段编号不允许为空!");
        boolean flag = false;
        for (final WorkFlowMoney workFlowMoney : anWorkFlowMoneys) {
            if (anMoneyId.equals(workFlowMoney.getId())) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            throw new BytterException("输入金额段不正确！");
        }
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
