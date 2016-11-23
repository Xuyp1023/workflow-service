// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.snaker.engine.model.DecisionModel;
import org.snaker.engine.model.EndModel;
import org.snaker.engine.model.ForkModel;
import org.snaker.engine.model.JoinModel;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.StartModel;
import org.snaker.engine.model.SubProcessModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TransitionModel;

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.SpringContextHolder;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.entity.WorkFlowApprover;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowMoney;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.entity.WorkFlowStep;
import com.betterjr.modules.workflow.service.WorkFlowApproverService;
import com.betterjr.modules.workflow.service.WorkFlowBaseService;
import com.betterjr.modules.workflow.service.WorkFlowMoneyService;
import com.betterjr.modules.workflow.service.WorkFlowNodeService;
import com.betterjr.modules.workflow.service.WorkFlowStepService;

/**
 * @author liuwl
 *
 */
public class BetterModelParser {
    /**
     * 通过流程编号解析model
     *
     * @param anProcessId
     * @return
     */
    public static ProcessModel parse(final String anProcessId) {
        final WorkFlowBaseService workFlowBaseService = SpringContextHolder.getBean(WorkFlowBaseService.class);

        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseByProcessId(anProcessId);

        return parse(workFlowBase);
    }

    /**
     * 通过流程模板baseId解析model
     *
     * @param anBaseId
     * @return
     */
    public static ProcessModel parse(final Long anBaseId) {
        final WorkFlowBaseService workFlowBaseService = SpringContextHolder.getBean(WorkFlowBaseService.class);

        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);

        return parse(workFlowBase);
    }

    /**
     * 根据 WorkFlowBase解析model
     *
     * @param anWorkFlowBase
     * @return
     */
    private static ProcessModel parse(final WorkFlowBase anWorkFlowBase) {
        BTAssert.notNull(anWorkFlowBase, "流程定义不允许为空！");

        final ProcessModel process = new ProcessModel();
        process.setName(anWorkFlowBase.getName());
        process.setDisplayName(anWorkFlowBase.getNickname());
        process.setOperRole(anWorkFlowBase.getOperRole());

        final List<WorkFlowNode> flowNodes = getNodes(anWorkFlowBase.getId());

        BTAssert.isTrue(flowNodes != null && flowNodes.size() > 2, "流程至少需要3个结点！");

        final List<NodeModel> nodeModels = process.getNodes();

        TransitionModel prevStep = null;
        // 解析节点
        for (int i = 0; i < flowNodes.size(); i++) {
            final WorkFlowNode flowNode = flowNodes.get(i);
            BTAssert.notNull(flowNode, "流程节点不允许为空！");
            if (i == 0) { // 第一步
                BTAssert.isTrue(BetterStringUtils.equals(flowNode.getType(), WorkFlowConstants.NODE_TYPE_START), "第一个节点必须为开始节点");
                final StartModel startModel = new StartModel();
                startModel.setName("开始");

                prevStep = new TransitionModel();
                startModel.setOutputs(Collections.singletonList(prevStep));
                prevStep.setSource(startModel);

                nodeModels.add(startModel);
            }
            else if (i == flowNodes.size() - 1) {// 最后一步
                BTAssert.isTrue(BetterStringUtils.equals(flowNode.getType(), WorkFlowConstants.NODE_TYPE_END), "最后一个节点必须为结束节点");
                final EndModel endModel = new EndModel();
                endModel.setName("结束");

                prevStep.setTarget(endModel);
                endModel.setInputs(Collections.singletonList(prevStep));

                nodeModels.add(endModel);
            }
            else {
                switch (flowNode.getType()) {
                case WorkFlowConstants.NODE_TYPE_OPER: { // 经办节点
                    final TaskModel taskModel = parseOperNode(flowNode);

                    prevStep.setTarget(taskModel);
                    final TransitionModel nextStep = new TransitionModel();
                    taskModel.setOutputs(Collections.singletonList(nextStep));
                    nextStep.setSource(taskModel);

                    prevStep = nextStep;
                }
                break;
                case WorkFlowConstants.NODE_TYPE_APP: { // 审批节点 此处可能生成若干节点
                    final List<NodeModel> stepNodeList = new ArrayList<NodeModel>();
                    prevStep = parseAppNode(flowNode, prevStep, stepNodeList);

                    nodeModels.addAll(stepNodeList);
                }
                break;
                case WorkFlowConstants.NODE_TYPE_SUB: { // 子流程
                    final SubProcessModel subProcessModel = new SubProcessModel();
                    subProcessModel.setName(flowNode.getName());
                    subProcessModel.setOperRole(flowNode.getOperRole());
                    subProcessModel.setDisplayName(flowNode.getNickname());
                    subProcessModel.setProcessName(flowNode.getName());

                    prevStep.setTarget(subProcessModel);
                    final TransitionModel nextStep = new TransitionModel();
                    subProcessModel.setOutputs(Collections.singletonList(nextStep));
                    nextStep.setSource(subProcessModel);

                    prevStep = nextStep;
                }
                break;
                default:
                    throw new BytterException(flowNode + "流程定义不正确！");
                }
            }
        }

        return null;
    }

    /**
     * 解析经办人节点
     *
     * @param anFlowNode
     * @return
     */
    private static TaskModel parseOperNode(final WorkFlowNode anFlowNode) {
        final TaskModel operTaskModel = new TaskModel();
        operTaskModel.setName(anFlowNode.getName());
        operTaskModel.setDisplayName(anFlowNode.getNickname());

        final WorkFlowApproverService workFlowApproverService = SpringContextHolder.getBean(WorkFlowApproverService.class);
        final WorkFlowApprover workFlowApprover = workFlowApproverService.findApproverByNode(anFlowNode.getId());

        if (workFlowApprover != null) {
            operTaskModel.setAssignee(String.valueOf(workFlowApprover.getOperId()));
        }
        return operTaskModel;
    }

    /**
     * 解析审批节点
     *
     * @param anFlowNode
     * @param anStepNodeList
     * @param anPrevStep
     * @return
     */
    private static TransitionModel parseAppNode(final WorkFlowNode anFlowNode, final TransitionModel anPrevStep,
            final List<NodeModel> anStepNodeList) {
        BTAssert.notNull(anFlowNode, "流程节点不允许为空");

        final List<WorkFlowStep> flowSteps = getSteps(anFlowNode.getId());

        TransitionModel nextStep = null;

        if (Collections3.isEmpty(flowSteps) == false) { // 有审批步骤
            if (flowSteps.size() == 1) { // 仅有一步时
                final WorkFlowStep flowStep = flowSteps.get(0);

                nextStep = parseStep(anFlowNode.getBaseId(), flowStep, anPrevStep, anStepNodeList);
            }
            else {
                for (int i = 0; i < flowSteps.size(); i++) {
                    final WorkFlowStep flowStep = flowSteps.get(i);
                    if (i == 0) {
                        parseStep(anFlowNode.getBaseId(), flowStep, anPrevStep, anStepNodeList);
                    }
                    else if (i == flowSteps.size() - 1) {
                        parseStep(anFlowNode.getBaseId(), flowStep, anPrevStep, anStepNodeList);
                    }
                    else {
                        parseStep(anFlowNode.getBaseId(), flowStep, anPrevStep, anStepNodeList);
                    }
                }
            }
        }
        else { // 如果没有步骤，则为自动结点
            final TaskModel taskModel = new TaskModel();
            taskModel.setName(anFlowNode.getName());
            taskModel.setAutoExecute("Y");
            nextStep = new TransitionModel();
            anPrevStep.setTarget(taskModel);
            taskModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(taskModel);
        }

        return nextStep;
    }

    /**
     * @param anFlowStep
     */
    private static TransitionModel parseStep(final Long anBaseId, final WorkFlowStep anFlowStep, final TransitionModel anPrevStep, final List<NodeModel> anStepNodeList) {
        final WorkFlowMoneyService workFlowMoneyService = SpringContextHolder.getBean(WorkFlowMoneyService.class);
        final WorkFlowApproverService workFlowApproverService = SpringContextHolder.getBean(WorkFlowApproverService.class);

        final String auditType = anFlowStep.getAuditType();
        final String isMoney = anFlowStep.getIsMoney();

        final List<WorkFlowApprover> flowApprovers = getApprovers(anFlowStep.getId());
        final List<WorkFlowMoney> flowMoneys = workFlowMoneyService.queryWorkFlowMoneyByBaseId(anBaseId);

        if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() == 1, "此节点只能拥有1位审批人");

            final TaskModel appTaskModel = new TaskModel();
            appTaskModel.setName(anFlowStep.getName());
            appTaskModel.setDisplayName(anFlowStep.getNickname());

            final WorkFlowApprover workFlowApprover = flowApprovers.get(0);
            if (workFlowApprover != null) {
                appTaskModel.setAssignee(String.valueOf(workFlowApprover.getOperId()));
            }

            anPrevStep.setTarget(appTaskModel);
            final TransitionModel nextStep = new TransitionModel();
            appTaskModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(appTaskModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() > 1, "此节点至少2位审批人");

            final DecisionModel decisionModel = new DecisionModel(); // 进
            anPrevStep.setTarget(decisionModel);

            final JoinModel joinModel = new JoinModel();  // 出
            final List<TransitionModel> decisionOutputs = new ArrayList<>();

            for (final WorkFlowApprover workFlowApprover: flowApprovers) {
                final TransitionModel enterTrans = new TransitionModel();
                enterTrans.setSource(decisionModel);
                final Long moneyId = workFlowApprover.getMoneyId();
                final WorkFlowMoney workFlowMoney = workFlowMoneyService.findWorkFlowMoney(moneyId);
                BTAssert.notNull(workFlowMoney, "未找到正确的金额段");

                enterTrans.setExpr(workFlowMoney.getSpelExpr());// TODO 此处需要处理 transition expr 表达式

                final TaskModel taskModel = new TaskModel();
                taskModel.setName(anFlowStep.getName());
                taskModel.setDisplayName(anFlowStep.getNickname());
                taskModel.setAssignee(String.valueOf(workFlowApprover.getOperId()));

                enterTrans.setTarget(taskModel);
                decisionOutputs.add(enterTrans);

                final TransitionModel exitTrans = new TransitionModel();
                taskModel.setOutputs(Collections.singletonList(exitTrans));
                exitTrans.setSource(taskModel);

                exitTrans.setTarget(joinModel);
            }

            decisionModel.setOutputs(decisionOutputs);
            final TransitionModel nextStep = new TransitionModel();
            joinModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(joinModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() > 1, "此节点至少2位审批人");
            final ForkModel forkModel = new ForkModel();
            anPrevStep.setTarget(forkModel);

            final JoinModel joinModel = new JoinModel();

            final List<TransitionModel> forkOutputs = new ArrayList<>();
            for (final WorkFlowApprover workFlowApprover: flowApprovers) {
                final TransitionModel enterTrans = new TransitionModel();
                enterTrans.setSource(forkModel);

                final TaskModel taskModel = new TaskModel();
                taskModel.setHasWeight(true);
                taskModel.setWeight(workFlowApprover.getWeight());// TODO 此处需要处理权重
                taskModel.setName(anFlowStep.getName());
                taskModel.setDisplayName(anFlowStep.getNickname());
                taskModel.setAssignee(String.valueOf(workFlowApprover.getOperId()));

                enterTrans.setTarget(taskModel);
                forkOutputs.add(enterTrans);

                final TransitionModel exitTrans = new TransitionModel();
                taskModel.setOutputs(Collections.singletonList(exitTrans));
                exitTrans.setSource(taskModel);

                exitTrans.setTarget(joinModel);
            }
            forkModel.setOutputs(forkOutputs);
            final TransitionModel nextStep = new TransitionModel();
            joinModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(joinModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            //先按moneyId 分组
            final DecisionModel decisionModel = new DecisionModel();
            anPrevStep.setTarget(decisionModel);
            final List<TransitionModel> decisionOutputs = new ArrayList<>();
            final JoinModel decisionJoinModel = new JoinModel();

            for (final WorkFlowMoney workFlowMoney: flowMoneys) {
                final TransitionModel decisionEnterTrans = new TransitionModel();
                decisionEnterTrans.setSource(decisionModel);
                decisionEnterTrans.setExpr(workFlowMoney.getSpelExpr());// TODO 此处需要处理 transition expr 表达式

                //各个金额段下的并行
                final ForkModel forkModel = new ForkModel();

                decisionEnterTrans.setTarget(forkModel);
                decisionOutputs.add(decisionEnterTrans);

                final JoinModel joinModel = new JoinModel();
                final List<TransitionModel> forkOutputs = new ArrayList<>();
                //当前段中所有节点
                for(final WorkFlowApprover flowApprover: flowApprovers) {
                    if (workFlowMoney.getId().equals(flowApprover.getMoneyId())) {
                        final TransitionModel enterTrans = new TransitionModel();
                        enterTrans.setSource(forkModel);

                        final TaskModel taskModel = new TaskModel();
                        taskModel.setHasWeight(true);
                        taskModel.setWeight(flowApprover.getWeight());// TODO 此处需要处理权重
                        taskModel.setName(anFlowStep.getName());
                        taskModel.setDisplayName(anFlowStep.getNickname());
                        taskModel.setAssignee(String.valueOf(flowApprover.getOperId()));

                        enterTrans.setTarget(taskModel);
                        forkOutputs.add(enterTrans);

                        final TransitionModel exitTrans = new TransitionModel();
                        taskModel.setOutputs(Collections.singletonList(exitTrans));
                        exitTrans.setSource(taskModel);

                        exitTrans.setTarget(joinModel);
                    }
                }

                final TransitionModel decisionExitTrans = new TransitionModel();
                joinModel.setOutputs(Collections.singletonList(decisionExitTrans));
                decisionExitTrans.setSource(joinModel);
                decisionExitTrans.setTarget(decisionJoinModel);
            }

            decisionModel.setOutputs(decisionOutputs);
            final TransitionModel nextStep = new TransitionModel();
            decisionJoinModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(decisionJoinModel);

            return nextStep;
        }
        else {
            throw new BytterException("审批方式和金额段标识不正确！");
        }

    }

    /**
     *
     * @param anStepId
     * @return
     */
    private static List<WorkFlowApprover> getApprovers(final Long anStepId) {
        final WorkFlowApproverService workFlowApproverService = SpringContextHolder.getBean(WorkFlowApproverService.class);
        return workFlowApproverService.queryApproverByStep(anStepId);
    }

    /**
     *
     * @param anNodeId
     * @return
     */
    private static List<WorkFlowStep> getSteps(final Long anNodeId) {
        final WorkFlowStepService workFlowStepService = SpringContextHolder.getBean(WorkFlowStepService.class);
        return workFlowStepService.queryWorkFlowStepByNodeId(anNodeId);
    }

    /**
     *
     * @param anBaseId
     * @return
     */
    private static List<WorkFlowNode> getNodes(final Long anBaseId) {
        final WorkFlowNodeService workFlowNodeService = SpringContextHolder.getBean(WorkFlowNodeService.class);
        return workFlowNodeService.queryWorkFlowNode(anBaseId);
    }
}
