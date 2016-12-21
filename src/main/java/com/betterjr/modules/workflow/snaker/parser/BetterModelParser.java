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
import org.snaker.engine.model.ExtJoinModel;
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
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
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
     *
     */
    private final static int X_INC = 150;
    private final static int Y_INC = 80;

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
     *
     * @param anNode
     * @param anCoordinate
     */
    private static void setNodeLayout(final NodeModel anNode, final Coordinate anCoordinate) {
        setNodeLayout(anNode, anCoordinate.getX(), anCoordinate.getY());
    }

    /**
     *
     * @param anNode
     * @param anX
     * @param anY
     */
    private static void setNodeLayout(final NodeModel anNode, final int anX, final int anY) {
        anNode.setX(anX);
        anNode.setY(anY);
    }

    /**
     *
     * @param nodeModel
     */
    private static void buildNodeLayout(final NodeModel nodeModel) {
        nodeModel.buildLayout();
    }

    /**
     *
     * @param nodeModels
     * @param anCoordinate
     */
    private static void adjustNodeLayout(final List<NodeModel> nodeModels, final Coordinate anCoordinate, final int incLevel) {
        final int orginLevel = anCoordinate.getLevel();
        if (orginLevel < incLevel) {
            anCoordinate.setLevel(incLevel);
            nodeModels.forEach(nodeModel -> nodeModel.setY(nodeModel.getY() + (Y_INC * (incLevel - orginLevel))));
        }
    }

    /**
     * 根据 WorkFlowBase解析model
     *
     * @param anWorkFlowBase
     * @return
     */
    private static ProcessModel parse(final WorkFlowBase anWorkFlowBase) {
        final Coordinate coordinate = new Coordinate(0, 200);

        BTAssert.notNull(anWorkFlowBase, "流程定义不允许为空！");

        final ProcessModel process = new ProcessModel();
        process.setName(anWorkFlowBase.getName());
        process.setDisplayName(anWorkFlowBase.getNickname());
        process.setOperRole(anWorkFlowBase.getOperRole());
        process.setWorkFlowBase(anWorkFlowBase);

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
                startModel.setDisplayName("开始");

                setNodeLayout(startModel, coordinate);

                prevStep = new TransitionModel();
                prevStep.setName(startModel.getName() + "-outputPath");
                prevStep.setDisplayName("");

                startModel.setOutputs(Collections.singletonList(prevStep));
                prevStep.setSource(startModel);

                nodeModels.add(startModel);
            }
            else if (i == flowNodes.size() - 1) {// 最后一步
                BTAssert.isTrue(BetterStringUtils.equals(flowNode.getType(), WorkFlowConstants.NODE_TYPE_END), "最后一个节点必须为结束节点");
                final EndModel endModel = new EndModel();
                endModel.setName("结束");
                endModel.setDisplayName("结束");

                //prevStep.setName(endModel.getName() + "-inputPath");
                //prevStep.setDisplayName(endModel.getName() + "-inputPath");
                coordinate.setX(coordinate.getX() + X_INC);
                setNodeLayout(endModel, coordinate);

                prevStep.setTarget(endModel);
                endModel.setInputs(Collections.singletonList(prevStep));

                nodeModels.add(endModel);
            }
            else {
                switch (flowNode.getType()) {
                case WorkFlowConstants.NODE_TYPE_OPER: { // 经办节点
                    final TaskModel taskModel = parseOperNode(anWorkFlowBase, flowNode);

                    coordinate.setX(coordinate.getX() + X_INC);
                    setNodeLayout(taskModel, coordinate);

                    prevStep.setTarget(taskModel);
                    final TransitionModel nextStep = new TransitionModel();
                    taskModel.setOutputs(Collections.singletonList(nextStep));
                    nextStep.setSource(taskModel);
                    prevStep = nextStep;

                    prevStep.setName(taskModel.getName() + "-outputPath");
                    prevStep.setDisplayName("");

                    nodeModels.add(taskModel);
                }
                break;
                case WorkFlowConstants.NODE_TYPE_APP: { // 审批节点 此处可能生成若干节点
                    final List<NodeModel> stepNodeList = new ArrayList<NodeModel>();
                    prevStep = parseAppNode(anWorkFlowBase, flowNode, prevStep, stepNodeList, nodeModels, coordinate);

                    nodeModels.addAll(stepNodeList);
                }
                break;
                case WorkFlowConstants.NODE_TYPE_SUB: { // 子流程
                    final SubProcessModel subProcessModel = new SubProcessModel();
                    subProcessModel.setName(flowNode.getName());
                    subProcessModel.setOperRole(flowNode.getOperRole());
                    subProcessModel.setDisplayName(flowNode.getNickname());
                    subProcessModel.setProcessName(flowNode.getName());

                    coordinate.setX(coordinate.getX() + X_INC);
                    setNodeLayout(subProcessModel, coordinate);

                    prevStep.setTarget(subProcessModel);
                    final TransitionModel nextStep = new TransitionModel();
                    subProcessModel.setOutputs(Collections.singletonList(nextStep));
                    nextStep.setSource(subProcessModel);

                    prevStep = nextStep;

                    prevStep.setName(subProcessModel.getName() + "-outputPath");
                    prevStep.setDisplayName("");

                    nodeModels.add(subProcessModel);
                }
                break;
                default:
                    throw new BytterException(flowNode + "流程定义不正确！");
                }
            }
        }
        nodeModels.forEach(BetterModelParser::buildNodeLayout);
        return process;
    }

    /**
     * 解析经办人节点
     *
     * @param anWorkFlowBase
     *
     * @param anFlowNode
     * @return
     */
    private static TaskModel parseOperNode(final WorkFlowBase anWorkFlowBase, final WorkFlowNode anFlowNode) {
        final TaskModel operTaskModel = new TaskModel();
        operTaskModel.setName(anFlowNode.getName());
        operTaskModel.setDisplayName(anFlowNode.getNickname());
        operTaskModel.setForm(anFlowNode.getForm());

        operTaskModel.setWorkFlowBase(anWorkFlowBase);
        operTaskModel.setWorkFlowNode(anFlowNode);

        final WorkFlowApproverService workFlowApproverService = SpringContextHolder.getBean(WorkFlowApproverService.class);
        final WorkFlowApprover workFlowApprover = workFlowApproverService.findApproverByNode(anFlowNode.getId());

        if (workFlowApprover != null) {
            operTaskModel.setAssignee(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(workFlowApprover.getOperId()));
        }
        else {
            operTaskModel.setAssignee(WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(anWorkFlowBase.getCustNo()));
        }
        return operTaskModel;
    }

    /**
     * 解析审批节点
     *
     * @param anWorkFlowBase
     *
     * @param anFlowNode
     * @param anStepNodeList
     * @param anPrevStep
     * @param anNodeModels
     * @param anCoordinate
     * @return
     */
    private static TransitionModel parseAppNode(final WorkFlowBase anWorkFlowBase, final WorkFlowNode anFlowNode, final TransitionModel anPrevStep,
            final List<NodeModel> anStepNodeList, final List<NodeModel> anNodeModels, final Coordinate anCoordinate) {
        BTAssert.notNull(anFlowNode, "流程节点不允许为空");

        final List<WorkFlowStep> flowSteps = getSteps(anFlowNode.getId());

        TransitionModel nextStep = null;

        if (Collections3.isEmpty(flowSteps) == false) { // 有审批步骤
            if (flowSteps.size() == 1) { // 仅有一步时
                final WorkFlowStep flowStep = flowSteps.get(0);

                anCoordinate.setX(anCoordinate.getX() + X_INC);
                nextStep = parseStep(anWorkFlowBase, anFlowNode, flowStep, anPrevStep, anStepNodeList, 0, anNodeModels, anCoordinate);
            }
            else {
                TransitionModel tempNextStep = null;
                for (int i = 0; i < flowSteps.size(); i++) {
                    final WorkFlowStep flowStep = flowSteps.get(i);
                    anCoordinate.setX(anCoordinate.getX() + X_INC);
                    if (i == 0) { // 有多步时 第一步
                        tempNextStep = parseStep(anWorkFlowBase, anFlowNode, flowStep, anPrevStep, anStepNodeList, i, anNodeModels, anCoordinate);
                    }
                    else if (i == flowSteps.size() - 1) { // 有多步时 最后一步
                        nextStep = parseStep(anWorkFlowBase, anFlowNode, flowStep, tempNextStep, anStepNodeList, i, anNodeModels, anCoordinate);
                    }
                    else { // 中间步骤
                        tempNextStep = parseStep(anWorkFlowBase, anFlowNode, flowStep, anPrevStep, anStepNodeList, i, anNodeModels, anCoordinate);
                    }
                }
            }

            nextStep.setName(anFlowNode.getName() + "-outputPath");
            nextStep.setDisplayName("");
        }
        else {
            nextStep = anPrevStep;
        }

        return nextStep;
    }

    /**
     * @param anWorkFlowNode
     * @param anWorkFlowStep
     * @param anStep
     * @param anNodeModels
     * @param anCoordinate
     */
    private static TransitionModel parseStep(final WorkFlowBase anWorkFlowBase, final WorkFlowNode anWorkFlowNode, final WorkFlowStep anWorkFlowStep,
            final TransitionModel anPrevStep, final List<NodeModel> anStepNodeList, final int anStep, final List<NodeModel> anNodeModels,
            final Coordinate anCoordinate) {
        final WorkFlowNodeService workFlowNodeService = SpringContextHolder.getBean(WorkFlowNodeService.class);
        final WorkFlowMoneyService workFlowMoneyService = SpringContextHolder.getBean(WorkFlowMoneyService.class);

        final Long anNodeId = anWorkFlowStep.getNodeId();
        final WorkFlowNode workFlowNode = workFlowNodeService.findWorkFlowNodeById(anNodeId);

        final String auditType = anWorkFlowStep.getAuditType();
        final String isMoney = anWorkFlowStep.getIsMoney();

        final List<WorkFlowApprover> flowApprovers = getApprovers(anWorkFlowStep.getId());
        final List<WorkFlowMoney> flowMoneys = workFlowMoneyService.queryWorkFlowMoneyByBaseId(anWorkFlowBase.getId());

        if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() == 1, "此节点只能拥有1位审批人");

            final TaskModel taskModel = new TaskModel();
            taskModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep));
            taskModel.setDisplayName(anWorkFlowStep.getNickname());

            taskModel.setWorkFlowBase(anWorkFlowBase);
            taskModel.setWorkFlowNode(anWorkFlowNode);
            taskModel.setWorkFlowStep(anWorkFlowStep);

            setNodeLayout(taskModel, anCoordinate);

            final WorkFlowApprover workFlowApprover = flowApprovers.get(0);
            taskModel.setAssignee(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(workFlowApprover.getOperId()));

            anPrevStep.setTarget(taskModel);
            final TransitionModel nextStep = new TransitionModel();
            taskModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(taskModel);

            anStepNodeList.add(taskModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_SERIAL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() > 1, "此节点至少2位审批人");

            final DecisionModel decisionModel = new DecisionModel();

            setNodeLayout(decisionModel, anCoordinate);

            anPrevStep.setTarget(decisionModel);
            decisionModel.setInputs(Collections.singletonList(anPrevStep));

            final JoinModel joinModel = new JoinModel();
            final List<TransitionModel> decisionOutputs = new ArrayList<>();
            final List<TransitionModel> joinInputs = new ArrayList<>();

            anStepNodeList.add(decisionModel);

            anCoordinate.setX(anCoordinate.getX() + X_INC);

            final int incLevel = getYAxisIncLevel(flowApprovers.size());
            adjustNodeLayout(anNodeModels, anCoordinate, incLevel);

            for (int i = 0; i < flowApprovers.size(); i++) {
                final WorkFlowApprover workFlowApprover = flowApprovers.get(i);
                final Long moneyId = workFlowApprover.getMoneyId();
                final WorkFlowMoney workFlowMoney = workFlowMoneyService.findWorkFlowMoney(moneyId);
                BTAssert.notNull(workFlowMoney, "未找到正确的金额段");

                final TransitionModel enterTrans = new TransitionModel();
                enterTrans.setSource(decisionModel);

                enterTrans.setName(anWorkFlowStep.getName() + "-money-" + i + "-enterPath");
                enterTrans.setDisplayName("");

                String moneyVariable = workFlowNode.getMoneyVariable();
                if (BetterStringUtils.isBlank(moneyVariable)) {
                    moneyVariable = anWorkFlowBase.getMoneyVariable();
                }

                BTAssert.isTrue(BetterStringUtils.isNotBlank(moneyVariable), "金额段变量不允许为空！");
                enterTrans.setExpr(workFlowMoney.getSpelExpr(moneyVariable));

                final TaskModel taskModel = new TaskModel();

                taskModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-" + String.valueOf(i));
                taskModel.setDisplayName(anWorkFlowStep.getNickname());

                setNodeLayout(taskModel, anCoordinate.getX(), anCoordinate.getY() + (Y_INC * (incLevel - i)));

                taskModel.setWorkFlowBase(anWorkFlowBase);
                taskModel.setWorkFlowNode(anWorkFlowNode);
                taskModel.setWorkFlowStep(anWorkFlowStep);

                taskModel.setAssignee(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(workFlowApprover.getOperId()));

                enterTrans.setTarget(taskModel);
                decisionOutputs.add(enterTrans);

                final TransitionModel exitTrans = new TransitionModel();
                taskModel.setOutputs(Collections.singletonList(exitTrans));
                exitTrans.setSource(taskModel);
                exitTrans.setName(anWorkFlowStep.getName() + "-money-" + i + "-exitPath");
                exitTrans.setDisplayName("");

                joinInputs.add(exitTrans);

                anStepNodeList.add(taskModel);
                exitTrans.setTarget(joinModel);
            }
            decisionModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-decision");
            decisionModel.setOutputs(decisionOutputs);
            final TransitionModel nextStep = new TransitionModel();

            anCoordinate.setX(anCoordinate.getX() + X_INC);
            setNodeLayout(joinModel, anCoordinate);

            joinModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-join");
            joinModel.setInputs(joinInputs);
            joinModel.setOutputs(Collections.singletonList(nextStep));
            nextStep.setSource(joinModel);
            nextStep.setName(anWorkFlowStep.getName() + "-money--exitPath");
            nextStep.setDisplayName("");
            anStepNodeList.add(joinModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_FALSE.equals(isMoney)) {
            BTAssert.isTrue(flowApprovers.size() > 1, "此节点至少2位审批人");
            final ForkModel forkModel = new ForkModel();

            setNodeLayout(forkModel, anCoordinate);
            anPrevStep.setTarget(forkModel);
            forkModel.setInputs(Collections.singletonList(anPrevStep));

            final ExtJoinModel joinModel = new ExtJoinModel();
            anStepNodeList.add(forkModel);

            final List<TransitionModel> forkOutputs = new ArrayList<>();
            final List<TransitionModel> joinInputs = new ArrayList<>();

            anCoordinate.setX(anCoordinate.getX() + X_INC);

            final int incLevel = getYAxisIncLevel(flowApprovers.size());
            adjustNodeLayout(anNodeModels, anCoordinate, incLevel);

            for (int i = 0; i < flowApprovers.size(); i++) {
                final WorkFlowApprover workFlowApprover = flowApprovers.get(i);

                final TransitionModel enterTrans = new TransitionModel();
                enterTrans.setSource(forkModel);
                enterTrans.setName(anWorkFlowStep.getName() + "-parallel-" + i + "-enterPath");
                enterTrans.setDisplayName("");

                final TaskModel taskModel = new TaskModel();
                taskModel.setHasWeight(true);
                taskModel.setWeight(workFlowApprover.getWeight());
                taskModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-" + String.valueOf(i));
                taskModel.setDisplayName(anWorkFlowStep.getNickname());

                setNodeLayout(taskModel, anCoordinate.getX(), anCoordinate.getY() + (Y_INC * (incLevel - i)));

                taskModel.setWorkFlowBase(anWorkFlowBase);
                taskModel.setWorkFlowNode(anWorkFlowNode);
                taskModel.setWorkFlowStep(anWorkFlowStep);

                taskModel.setAssignee(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(workFlowApprover.getOperId()));

                enterTrans.setTarget(taskModel);
                forkOutputs.add(enterTrans);

                final TransitionModel exitTrans = new TransitionModel();
                taskModel.setOutputs(Collections.singletonList(exitTrans));
                joinInputs.add(exitTrans);
                exitTrans.setSource(taskModel);
                exitTrans.setName(anWorkFlowStep.getName() + "-parallel-" + i + "-exitPath");
                exitTrans.setDisplayName("");

                anStepNodeList.add(taskModel);

                exitTrans.setTarget(joinModel);
            }
            forkModel.setOutputs(forkOutputs);
            forkModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-fork");
            final TransitionModel nextStep = new TransitionModel();
            joinModel.setInputs(joinInputs);
            joinModel.setOutputs(Collections.singletonList(nextStep));

            anCoordinate.setX(anCoordinate.getX() + X_INC); // 递进
            setNodeLayout(joinModel, anCoordinate);

            joinModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-join");

            nextStep.setSource(joinModel);
            nextStep.setName(anWorkFlowStep.getName() + "-parallel--exitPath");
            nextStep.setDisplayName("");
            anStepNodeList.add(joinModel);

            return nextStep;
        }
        else if (WorkFlowConstants.AUDIT_TYPE_PARALLEL.equals(auditType) && WorkFlowConstants.IS_MONEY_TRUE.equals(isMoney)) {
            // 先按moneyId 分组
            final DecisionModel decisionModel = new DecisionModel();
            anPrevStep.setTarget(decisionModel);
            decisionModel.setInputs(Collections.singletonList(anPrevStep));

            setNodeLayout(decisionModel, anCoordinate);

            final List<TransitionModel> decisionOutputs = new ArrayList<>();
            final List<TransitionModel> decisionJoinInputs = new ArrayList<>();

            final JoinModel decisionJoinModel = new JoinModel();

            anStepNodeList.add(decisionModel);

            anCoordinate.setX(anCoordinate.getX() + X_INC);

            final int incLevel = getYAxisIncLevel(flowApprovers.size());
            adjustNodeLayout(anNodeModels, anCoordinate, incLevel);

            final int incMoneyLevel = getYAxisIncLevel(flowMoneys.size());
            for (int i = 0; i < flowMoneys.size(); i++) {
                final WorkFlowMoney workFlowMoney = flowMoneys.get(i);
                final TransitionModel decisionEnterTrans = new TransitionModel();
                decisionEnterTrans.setSource(decisionModel);
                decisionEnterTrans.setName(anWorkFlowStep.getName() + "-money-" + i + "-enterPath");
                decisionEnterTrans.setDisplayName("");

                String moneyVariable = workFlowNode.getMoneyVariable();
                if (BetterStringUtils.isBlank(moneyVariable)) {
                    moneyVariable = anWorkFlowBase.getMoneyVariable();
                }

                BTAssert.isTrue(BetterStringUtils.isNotBlank(moneyVariable), "金额段变量不允许为空！");
                decisionEnterTrans.setExpr(workFlowMoney.getSpelExpr(moneyVariable));

                final ForkModel forkModel = new ForkModel();

                setNodeLayout(forkModel, anCoordinate.getX(), anCoordinate.getY() + (Y_INC * (incMoneyLevel - i)));

                decisionEnterTrans.setTarget(forkModel);
                forkModel.setInputs(Collections.singletonList(decisionEnterTrans));
                decisionOutputs.add(decisionEnterTrans);

                final ExtJoinModel joinModel = new ExtJoinModel();
                final List<TransitionModel> forkOutputs = new ArrayList<>();
                final List<TransitionModel> joinInputs = new ArrayList<>();
                anStepNodeList.add(forkModel);

                for (int j = 0; j < flowApprovers.size(); j++) {
                    final WorkFlowApprover flowApprover = flowApprovers.get(j);

                    if (workFlowMoney.getId().equals(flowApprover.getMoneyId())) {
                        final TransitionModel enterTrans = new TransitionModel();
                        enterTrans.setSource(forkModel);
                        enterTrans.setName(anWorkFlowStep.getName() + "-parallel-" + i + "-" + j + "-enterPath");
                        enterTrans.setDisplayName("");

                        final TaskModel taskModel = new TaskModel();
                        taskModel.setHasWeight(true);
                        taskModel.setWeight(flowApprover.getWeight());
                        taskModel
                        .setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-" + String.valueOf(i) + "-" + String.valueOf(j));
                        taskModel.setDisplayName(anWorkFlowStep.getNickname());

                        setNodeLayout(taskModel, anCoordinate.getX() + X_INC, anCoordinate.getY() + (Y_INC * (incLevel - i)));
                        taskModel.setWorkFlowBase(anWorkFlowBase);
                        taskModel.setWorkFlowNode(anWorkFlowNode);
                        taskModel.setWorkFlowStep(anWorkFlowStep);

                        taskModel.setAssignee(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowApprover.getOperId()));

                        enterTrans.setTarget(taskModel);
                        forkOutputs.add(enterTrans);

                        final TransitionModel exitTrans = new TransitionModel();
                        taskModel.setOutputs(Collections.singletonList(exitTrans));
                        exitTrans.setSource(taskModel);
                        exitTrans.setName(anWorkFlowStep.getName() + "-parallel-" + i + "-" + j + "-exitPath");
                        exitTrans.setDisplayName("");
                        joinInputs.add(exitTrans);
                        anStepNodeList.add(taskModel);

                        exitTrans.setTarget(joinModel);
                    }
                }

                forkModel.setOutputs(forkOutputs);
                forkModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-" + String.valueOf(i) + "-fork");
                final TransitionModel decisionExitTrans = new TransitionModel();
                joinModel.setOutputs(Collections.singletonList(decisionExitTrans));
                joinModel.setInputs(joinInputs);

                anCoordinate.setX(anCoordinate.getX() + X_INC);
                setNodeLayout(joinModel, anCoordinate.getX(), anCoordinate.getY() + (Y_INC * (incMoneyLevel - i)));

                joinModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-" + String.valueOf(i) + "-join");
                decisionExitTrans.setSource(joinModel);
                decisionExitTrans.setName(anWorkFlowStep.getName() + "-money-" + i + "-exitPath");
                decisionExitTrans.setDisplayName("");

                decisionJoinInputs.add(decisionExitTrans);
                anStepNodeList.add(joinModel);

                decisionExitTrans.setTarget(decisionJoinModel);
            }

            decisionModel.setOutputs(decisionOutputs);
            decisionModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-decision");
            final TransitionModel nextStep = new TransitionModel();
            decisionJoinModel.setOutputs(Collections.singletonList(nextStep));
            decisionJoinModel.setInputs(decisionJoinInputs);

            anCoordinate.setX(anCoordinate.getX() + X_INC);
            setNodeLayout(decisionJoinModel, anCoordinate);

            decisionJoinModel.setName(anWorkFlowStep.getName() + "-" + String.valueOf(anStep) + "-decisionJoin");
            nextStep.setName(anWorkFlowStep.getName() + "-money--exitPath");
            nextStep.setDisplayName("");
            nextStep.setSource(decisionJoinModel);
            anStepNodeList.add(decisionJoinModel);

            return nextStep;
        }
        else {
            throw new BytterException("审批方式和金额段标识不正确！");
        }

    }

    /**
     * @param anSize
     * @return
     */
    private static int getYAxisIncLevel(final int anSize) {
        int result = 0;
        switch (anSize) {
        case 1:
            result = 0;
            break;
        default: {
            if (anSize % 2 != 0) {
                result = anSize / 2;
            }
            else {
                result = (anSize + 1) / 2;
            }
        }
        }
        return result;
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

    /**
     * 坐标
     *
     * @author liuwl
     *
     */
    private static class Coordinate {
        public int x = 0;
        public int y = 0;
        public int level = 2;

        /**
         * @param anI
         * @param anI2
         */
        public Coordinate(final int anX, final int anY) {
            this.x = anX;
            this.y = anY;
        }

        public int getX() {
            return x;
        }

        public void setX(final int anX) {
            x = anX;
        }

        public int getY() {
            return y;
        }

        public void setY(final int anY) {
            y = anY;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(final int anLevel) {
            level = anLevel;
        }

    }
}
