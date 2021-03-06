// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
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
        final WorkFlowNode workFlowNode = workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        BTAssert.isTrue(StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_APP),
                "只允许审批节点删除步骤");

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
     * @param anBaseId
     * @param anNodeId
     * @param anNickname
     * @return
     */
    public WorkFlowStep addWorkFlowStep(final Long anBaseId, final Long anNodeId, final String anNickname) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowNode workFlowNode = workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

        BTAssert.isTrue(StringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_APP),
                "只允许审批节点创建步骤");

        final WorkFlowStep workFlowStep = new WorkFlowStep();

        workFlowStep.setName(workFlowNode.getName());
        workFlowStep.setNickname(anNickname);
        workFlowStep.setNodeId(anNodeId);
        workFlowStep.initAddValue();
        // 将添加的步骤加到最后一步
        workFlowStep.setSeq(queryWorkFlowStepByNodeId(anNodeId).size());

        this.insert(workFlowStep);
        return workFlowStep;
    }

    /**
     * 修改流程步骤
     *
     * @param anBaseId
     * @param anNodeId
     * @param anStepId
     * @param anWorkFlowStep
     * @return
     */
    public WorkFlowStep saveWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anStepId,
            final WorkFlowStep anWorkFlowStep) {
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        workFlowStep.setNickname(anWorkFlowStep.getNickname());
        workFlowStep.initModifyValue();

        return workFlowStep;
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

        return this.selectByProperty(conditionMap, "seq ASC");
    }

    /**
     * 查询流程步骤详情成功
     *
     * @param anStepId
     * @return
     */
    public WorkFlowStep findWorkFlowStepById(final Long anStepId) {
        BTAssert.notNull(anStepId, "流程步骤编号不允许为空！");
        return this.selectByPrimaryKey(anStepId);
    }

    /**
     * 删除流程步骤
     *
     * @param anStepId
     */
    public void saveDelWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        // XXX 删除已经存在的定义已经分配的操作员
        workFlowApproverService.saveDelWorkFlowApprover(WorkFlowConstants.PARENT_TYPE_STEP, anStepId);

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
        final WorkFlowNode workFlowNode = workFlowNodeService.checkWorkFlowNode(anBaseId, anNodeId);

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
    public void saveMoveUpStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        final Integer currentSeq = workFlowStep.getSeq();
        if (currentSeq > 0) { // 不是第一步
            // 向上调整当前步骤的seq 并且将原来的上一步下调
            final Integer previousSeq = currentSeq - 1;
            final WorkFlowStep previousStep = findWorkFlowStepByNodeAndSeq(anNodeId, previousSeq);
            BTAssert.notNull(previousStep, "没有找到当前步骤的前一步骤！");

            workFlowStep.setSeq(-1);
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
    public void saveMoveDownStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
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
     * 获取流程步骤定义
     *
     * @param anBaseId
     * @param anNodeId
     * @param anStepId
     * @return
     */
    public Map<String, Object> findStepDefinition(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        final WorkFlowStep workFlowStep = findWorkFlowStepById(anStepId);

        BTAssert.notNull(workFlowStep, "没有找到相应的流程步骤！");

        final Map<String, Object> result = new HashMap<>();

        final Long stepId = workFlowStep.getId();
        final String auditType = workFlowStep.getAuditType();
        final String isMoney = workFlowStep.getIsMoney();
        result.put("stepId", stepId);
        result.put("auditType", auditType);
        result.put("isMoney", isMoney);

        if (StringUtils.isNotBlank(auditType) && StringUtils.isNotBlank(isMoney)) {
            if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType)
                    && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
                final List<WorkFlowApprover> approvers = workFlowApproverService.queryApproverByStep(stepId);

                if (Collections3.isEmpty(approvers) == false) {
                    BTAssert.isTrue(approvers.size() == 1, "串行未启用金额段审批步骤只允许有一位审批人!");

                    result.put("approver", approvers.get(0).getOperId());
                }
            } else if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType)
                    && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
                final List<WorkFlowApprover> approvers = workFlowApproverService.queryApproverByStep(stepId);
                if (Collections3.isEmpty(approvers) == false) {
                    final List<Map<String, Object>> _temp = new ArrayList<>();
                    for (final WorkFlowApprover workFlowApprover : approvers) {
                        final Map<String, Object> approver = new HashMap<>();
                        approver.put("moneyId", workFlowApprover.getMoneyId());
                        approver.put("operId", workFlowApprover.getOperId());
                        _temp.add(approver);
                    }
                    result.put("approver", _temp);
                }
            } else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType)
                    && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
                final List<WorkFlowApprover> approvers = workFlowApproverService.queryApproverByStep(stepId);
                if (Collections3.isEmpty(approvers) == false) {
                    final List<Map<String, Object>> _temp = new ArrayList<>();
                    for (final WorkFlowApprover workFlowApprover : approvers) {
                        final Map<String, Object> approver = new HashMap<>();
                        approver.put("operId", workFlowApprover.getOperId());
                        approver.put("weight", workFlowApprover.getWeight());
                        _temp.add(approver);
                    }
                    result.put("approver", _temp);
                }
            } else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType)
                    && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
                final List<WorkFlowApprover> approvers = workFlowApproverService.queryApproverByStep(stepId);
                final List<WorkFlowMoney> workFlowMoneys = workFlowMoneyService.queryWorkFlowMoneyByBaseId(anBaseId);

                if (Collections3.isEmpty(workFlowMoneys) == false) {
                    final List<Map<String, Object>> _tempMoney = new ArrayList<>();
                    for (final WorkFlowMoney workFlowMoney : workFlowMoneys) {
                        final Map<String, Object> moneySection = new HashMap<>();
                        moneySection.put("moneyId", workFlowMoney.getId());

                        final List<Map<String, Object>> _temp = new ArrayList<>();
                        for (final WorkFlowApprover workFlowApprover : approvers) {
                            if (workFlowApprover.getMoneyId().equals(workFlowMoney.getId())) {
                                final Map<String, Object> appr = new HashMap<>();

                                appr.put("operId", workFlowApprover.getOperId());
                                appr.put("weight", workFlowApprover.getWeight());

                                _temp.add(appr);
                            }
                        }
                        moneySection.put("opers", _temp);

                        _tempMoney.add(moneySection);
                    }
                    result.put("approver", _tempMoney);
                }
            } else {
                throw new BytterException("审批方式和金额段标识不正确！");
            }
        }

        return result;
    }

    /**
     * 保存流程步骤定义
     *
     * @param anDefMap
     */
    /**
     * 格式定义 { auditType: 0串行 1并行 审批方式 isMoney: 0未启用 1启用 金额段
     *
     * 0,0串行审批未启用金额段 approver:operId
     * 0,1串行审批启用金额段 approver:[ {moneyId: , operId: }, {moneyId: , operId: } ]
     * 1,0并行未启用金额段 approver:[ {operId: ,weight:},{operId: , weight: } ]
     * 1,1并行启用金额段 approver:[ {moneyId: , opers:[ {operId: , weight: }, {operId: , weight: } ]}, {moneyId: , opers:[{operId: ,weight: }, {operId: , weight: } ]} ] }
     */
    public void saveStepDefinition(final Long anBaseId, final Long anNodeId, final Long anStepId,
            final Map<String, Object> anDefMap) {
        // 检查当前步骤对应的流程是否有操作权限
        final WorkFlowStep workFlowStep = checkWorkFlowStep(anBaseId, anNodeId, anStepId);

        final String auditType = (String) anDefMap.get("auditType");
        final String isMoney = (String) anDefMap.get("isMoney");

        BTAssert.isTrue(StringUtils.isNotBlank(auditType), "审批方式不允许为空！");
        BTAssert.isTrue(StringUtils.isNotBlank(isMoney), "金额段启用标识不允许为空！");

        workFlowStep.setAuditType(auditType);
        workFlowStep.setIsMoney(isMoney);

        final List<WorkFlowMoney> workFlowMoneys = workFlowMoneyService.queryWorkFlowMoneyByBaseId(anBaseId);

        // 将原有数据清理
        workFlowApproverService.saveDelWorkFlowApprover(WorkFlowConstants.PARENT_TYPE_STEP, anStepId);

        if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            // 如果是 串行审批 并且未启用金额段 只接受一个 审批操作员, 否则报错
            Long operId = null;
            if (anDefMap.get("approver") instanceof Integer) {
                operId = Long.valueOf((Integer) anDefMap.get("approver"));
            } else if (anDefMap.get("approver") instanceof String) {
                operId = Long.valueOf((String) anDefMap.get("approver"));
            }
            BTAssert.notNull(operId, "操作员编号未找到！");

            final WorkFlowApprover approver = new WorkFlowApprover();
            approver.setOperId(operId);

            workFlowApproverService.addApproverByStep(anStepId, approver);
        } else if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType)
                && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            // 如果是 串行审批 并且启用金额段 这时候需要根据金额段来匹配 每个金额段 只能一个审批操作员
            final List<Map<String, Object>> approvers = (List<Map<String, Object>>) anDefMap.get("approver");

            BTAssert.isTrue(approvers.size() == workFlowMoneys.size(), "金额段不匹配");

            for (final Map<String, Object> tempApprover : approvers) {
                final Long moneyId = Long.valueOf((Integer) tempApprover.get("moneyId"));
                Long operId = null; // 允许为空，为空为所有人
                if (tempApprover.get("operId") instanceof Integer) {
                    operId = Long.valueOf((Integer) tempApprover.get("operId"));
                } else if (tempApprover.get("operId") instanceof String) {
                    operId = Long.valueOf((String) tempApprover.get("operId"));
                }
                checkWorkFlowMoney(moneyId, workFlowMoneys); // 检查moneyId 是否在当前流程定义金额段内

                final WorkFlowApprover approver = new WorkFlowApprover();
                approver.setOperId(operId);
                approver.setMoneyId(moneyId);

                workFlowApproverService.addApproverByStep(anStepId, approver);
            }
        } else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType)
                && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            // 如果是并行审批，并且未启用金额段 可以选择多个审批操作员 并且每个操作员需要有权重值
            final List<Map<String, Object>> approvers = (List<Map<String, Object>>) anDefMap.get("approver");

            for (final Map<String, Object> tempApprover : approvers) {
                Long operId = null; // 允许为空，为空为所有人
                if (tempApprover.get("operId") instanceof Integer) {
                    operId = Long.valueOf((Integer) tempApprover.get("operId"));
                } else if (tempApprover.get("operId") instanceof String) {
                    operId = Long.valueOf((String) tempApprover.get("operId"));
                }
                Integer weight = null;
                if (tempApprover.get("weight") instanceof Integer) {
                    weight = (Integer) tempApprover.get("weight");
                } else if (tempApprover.get("weight") instanceof String) {
                    weight = Integer.valueOf((String) tempApprover.get("weight"));
                }
                final WorkFlowApprover approver = new WorkFlowApprover();
                approver.setOperId(operId);
                approver.setWeight(weight);

                workFlowApproverService.addApproverByStep(anStepId, approver);
            }
        } else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType)
                && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            // 如果是并行审批，并且启用金额段 每个金额段可以选择多个审批操作员 并且每个操作员需要有权重值
            final List<Map<String, Object>> approvers = (List<Map<String, Object>>) anDefMap.get("approver");

            for (final Map<String, Object> tempApprover : approvers) {
                final Long moneyId = Long.valueOf((Integer) tempApprover.get("moneyId"));

                checkWorkFlowMoney(moneyId, workFlowMoneys); // 检查moneyId 是否在当前流程定义金额段内

                final List<Map<String, Object>> opers = (List<Map<String, Object>>) tempApprover.get("opers");

                for (final Map<String, Object> oper : opers) {
                    Long operId = null; // 允许为空，为空为所有人
                    if (oper.get("operId") instanceof Integer) {
                        operId = Long.valueOf((Integer) oper.get("operId"));
                    } else if (oper.get("operId") instanceof String) {
                        operId = Long.valueOf((String) oper.get("operId"));
                    }
                    Integer weight = null;
                    if (oper.get("weight") instanceof Integer) {
                        weight = (Integer) oper.get("weight");
                    } else if (oper.get("weight") instanceof String) {
                        weight = Integer.valueOf((String) oper.get("weight"));
                    }

                    final WorkFlowApprover approver = new WorkFlowApprover();
                    approver.setMoneyId(moneyId);
                    approver.setOperId(operId);
                    approver.setWeight(weight);

                    workFlowApproverService.addApproverByStep(anStepId, approver);
                }
            }
        } else {
            throw new BytterException("审批方式和金额段标识不正确！");
        }

        this.updateByPrimaryKeySelective(workFlowStep);
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
    public void saveCopyWorkFlowStep(final WorkFlowNode anSourceNode, final WorkFlowNode anTargetNode,
            final Map<Long, Long> anWorkFlowMoneyMapping) {
        // copy 节点
        queryWorkFlowStepByNodeId(anSourceNode.getId()).forEach(tempWorkFlowStep -> {
            final WorkFlowStep workFlowStep = new WorkFlowStep();

            workFlowStep.initCopyValue(tempWorkFlowStep);
            workFlowStep.setNodeId(anTargetNode.getId());

            // copy审批员
            workFlowApproverService.saveCopyWorkFlowApproverByStep(tempWorkFlowStep, workFlowStep,
                    anWorkFlowMoneyMapping);
            this.insert(workFlowStep);
        });
    }

}
