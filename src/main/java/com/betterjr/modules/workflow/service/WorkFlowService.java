// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.2.0 : 2017年1月11日，liuwl, BUG-452
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.snaker.engine.IOrderService;
import org.snaker.engine.IProcessService;
import org.snaker.engine.IQueryService;
import org.snaker.engine.ITaskService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.SpringContextHolder;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterDateUtils;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.account.dubbo.interfaces.ICustOperatorService;
import com.betterjr.modules.account.entity.CustInfo;
import com.betterjr.modules.account.entity.CustOperatorInfo;
import com.betterjr.modules.customer.ICustMechBaseService;
import com.betterjr.modules.customer.entity.CustMechBase;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.constants.WorkFlowInput;
import com.betterjr.modules.workflow.data.WorkFlowHistoryOrder;
import com.betterjr.modules.workflow.data.WorkFlowHistoryTask;
import com.betterjr.modules.workflow.data.WorkFlowOrder;
import com.betterjr.modules.workflow.data.WorkFlowTask;
import com.betterjr.modules.workflow.data.WorkFlowWorkItem;
import com.betterjr.modules.workflow.entity.WorkFlowAudit;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.entity.WorkFlowStep;
import com.betterjr.modules.workflow.handler.INodeHandler;
import com.betterjr.modules.workflow.handler.IProcessHandler;
import com.betterjr.modules.workflow.snaker.util.SnakerHelper;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowService {

    /**
     *
     */
    private static final String _23_59_59 = " 23:59:59";

    @Reference(interfaceClass = ICustMechBaseService.class)
    private ICustMechBaseService custMechBaseService;

    @Reference(interfaceClass = ICustOperatorService.class)
    private ICustOperatorService custOperatorService;

    @Inject
    private SnakerEngine engine;

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowStepService workFlowStepService;

    @Inject
    private WorkFlowAuditService workFlowAuditService;

    @Inject
    private WorkFlowBusinessService workFlowBusinessService;

    /**
     * @param anFlowInput
     */
    private void checkAndInitWorkFlowDefinition(final WorkFlowInput anFlowInput) {
        // 检查并初始化流程定义
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseLatestByName(anFlowInput.getFlowName(), anFlowInput.getFlowCustNo());
        BTAssert.notNull(workFlowBase, "没有找到主流程定义！");

        BTAssert.isTrue(BetterStringUtils.equals(workFlowBase.getIsDisabled(), WorkFlowConstants.NOT_DISABLED),
                workFlowBase.getName() + " 流程已经被停用！请联系相关人员！");

        // 检查整条流程
        final List<WorkFlowNode> workFlowNodes = workFlowNodeService.queryWorkFlowNode(workFlowBase.getId());

        // 子流程可以自动初始化 供应商 经销商 核心企业 流程未定义，否则为它初始化
        for (final WorkFlowNode workFlowNode : workFlowNodes) {
            if (BetterStringUtils.equals(workFlowNode.getType(), WorkFlowConstants.NODE_TYPE_SUB)) {
                final Long custNo = findCustNo(workFlowNode, anFlowInput);
                final String workFlowName = workFlowNode.getName();

                final WorkFlowBase subWorkFlowBaseLatest = workFlowBaseService.findWorkFlowBaseLatestByName(workFlowName, custNo);
                if (subWorkFlowBaseLatest == null) { // 继续检查，是否有未发布流程
                    final CustMechBase subCustMechBase = custMechBaseService.findBaseInfo(custNo);
                    BTAssert.notNull(subCustMechBase, "没有找到 " + workFlowName + "：子流程所属公司！");

                    final WorkFlowBase subWorkFlowBaseLast = workFlowBaseService.findWorkFlowBaseLastByName(workFlowName, custNo);
                    if (subWorkFlowBaseLast != null) {
                        throw new BytterException(subCustMechBase.getCustName() + "：公司  " + workFlowName + "：流程尚未发布。");
                    }
                    else { // 创建并发布一条默认流程
                        final WorkFlowBase defaultWorkFlowBase = workFlowBaseService.findDefaultWorkFlowBaseByName(workFlowName);

                        BTAssert.notNull(defaultWorkFlowBase, defaultWorkFlowBase.getName() + " 默认流程未找到！");

                        final WorkFlowBase newWorkFlowBase = new WorkFlowBase();
                        newWorkFlowBase.setNickname(defaultWorkFlowBase.getName());

                        final WorkFlowBase savedWorkFlowBase = workFlowBaseService.addWorkFlowBase(newWorkFlowBase, defaultWorkFlowBase.getId(),
                                custNo);
                        BTAssert.notNull(savedWorkFlowBase, savedWorkFlowBase.getName() + " 创建子流程未成功！");

                        // 发布
                        engine.process().deploy(savedWorkFlowBase.getId());
                    }
                }
                else {
                    BTAssert.isTrue(BetterStringUtils.equals(subWorkFlowBaseLatest.getIsDisabled(), WorkFlowConstants.NOT_DISABLED),
                            subWorkFlowBaseLatest.getName() + " 流程已经被停用！请联系相关人员！");
                }
            }
        }

    }

    private Long findCustNo(final WorkFlowNode subWorkFlowNode, final WorkFlowInput anFlowInput) {
        final String operRole = subWorkFlowNode.getOperRole();
        AssertHelper.notEmpty(operRole);

        Long custNo = null;
        switch (operRole) { // CORE_USER 、PLATFORM_USER、FACTOR_USER、SUPPLIER_USER、SELLER_USER
        case "SUPPLIER_USER":
            custNo = anFlowInput.getSupplierCustNo();
            break;
        case "SELLER_USER":
            custNo = anFlowInput.getSellerCustNo();
            break;
        case "CORE_USER":
            custNo = anFlowInput.getCoreCustNo();
            break;
        case "PLATFORM_USER":
            custNo = anFlowInput.getPlatformCustNo();
            break;
        case "FACTOR_USER":
            custNo = anFlowInput.getFactorCustNo();
            break;
        }
        AssertHelper.notNull(custNo);
        return custNo;
    }

    /**
     * 启动流程
     *
     * @param flowInput
     * @return
     */
    public WorkFlowBusiness saveStart(final WorkFlowInput flowInput) {
        flowInput.checkStartParam();

        // 检查流程
        checkAndInitWorkFlowDefinition(flowInput);

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();

        final CustMechBase custMechBase = custMechBaseService.findBaseInfo(flowInput.getFlowCustNo());
        BTAssert.notNull(custMechBase, "没有找到主流程！");

        final CustMechBase initCustMechBase = custMechBaseService.findBaseInfo(flowInput.getStartCustNo());
        BTAssert.notNull(initCustMechBase, "没有找到启动流程公司！");

        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseLatestByName(flowInput.getFlowName(), flowInput.getFlowCustNo());
        BTAssert.notNull(workFlowBase, "没有找到流程定义！");

        final String handlerName = workFlowBase.getHandler();

        String businessId = "";
        String businessType = "";
        Map<String, Object> param = null;
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final IProcessHandler handler = SpringContextHolder.getBean(handlerName);
            if (handler != null) {
                final Map<String, Object> handleContext = new HashMap<>();
                handleContext.put("INPUT", flowInput.getParam().get("INPUT"));

                handler.processStart(handleContext);

                businessId = (String) handleContext.get("businessId");
                businessType = (String) handleContext.get("businessType");
                param = (Map<String, Object>) handleContext.get("param");
                flowInput.addAllParam(param);
            }
        }

        BTAssert.isTrue(BetterStringUtils.isNotBlank(businessId), "业务编号不允许为空！");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(businessType), "业务类型不允许为空！");

        final Order order = engine.startInstanceByName(flowInput.getFlowName(), flowInput.getFlowCustNo(), null,
                WorkFlowConstants.PREFIX_CUST_NO + flowInput.getFlowCustNo(), flowInput.getParam());
        BTAssert.notNull(order, "启动流程出现错误！");

        final WorkFlowBusiness workFlowBusiness = new WorkFlowBusiness();
        workFlowBusiness.setCustNo(initCustMechBase.getCustNo());
        workFlowBusiness.setCustName(initCustMechBase.getCustName());
        workFlowBusiness.setOrderId(order.getId());
        workFlowBusiness.setBaseId(workFlowBase.getId());
        workFlowBusiness.setBusinessId(businessId);
        workFlowBusiness.setBusinessType(businessType);

        final WorkFlowBusiness result = workFlowBusinessService.addWorkFlowBusiness(workFlowBusiness);

        final List<Task> tasks = queryService.getActiveTasks(new QueryFilter().setOrderId(order.getId()));
        Task task = null;
        if (Collections3.isEmpty(tasks)) {
            final List<Order> childOrders = queryService.getActiveOrders(new QueryFilter().setParentId(order.getId()));
            if (Collections3.isEmpty(childOrders)) {
                throw new BytterException("没有找到子流程实例");
            }
            if (childOrders.size() != 1) {
                throw new BytterException("初始子流程实例未决");
            }
            final List<Task> childTasks = queryService.getActiveTasks(new QueryFilter().setOrderId(childOrders.get(0).getId()));
            if (Collections3.isEmpty(childTasks)) {
                throw new BytterException("没有找到子流程可执行的初始节点");
            }
            if (childTasks.size() != 1) {
                throw new BytterException("子流程初始可执行节点未决");
            }
            task = childTasks.get(0);
        }
        else {
            if (tasks.size() != 1) {
                throw new BytterException("初始可执行节点未决");
            }
            task = tasks.get(0);
        }

        // 初始节点使用启动操作员办理 特别注意 如果初始节点在子流程上，需要子流程所在公司操作员办理
        final String[] actors = queryService.getTaskActorsByTaskId(task.getId());
        if (actors == null) {
            taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
        }
        else if (actors != null && actors.length == 1) {
            if (!BetterStringUtils.equals(actors[0], WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()))) {
                taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
                taskService.removeTaskActor(task.getId(), actors);
            }
        }

        // 启动流程，并处理第一个经办任务
        final List<Task> resultTasks = engine.executeTask(task.getId(), WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));

        return result;
    }

    /**
     *
     * @param flowInput
     * @return
     */
    public WorkFlowAudit savePassTask(final WorkFlowInput flowInput) {
        flowInput.checkPassParam();

        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();

        BTAssert.notNull(operId, "必须输入OperId");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final TaskModel taskModel = taskService.getTaskModel(task.getId());
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();
        final WorkFlowStep workFlowStep = taskModel.getWorkFlowStep();

        WorkFlowBusiness workFlowBusiness = null;
        if (BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1")) {
            final Order order = queryService.getOrder(task.getOrderId());
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getParentId());
        }
        else {
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(task.getOrderId());
        }
        BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

        final String handlerName = workFlowNode.getHandler();
        Map<String, Object> result = null;
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final INodeHandler handler = SpringContextHolder.getBean(handlerName);
            if (handler != null) {
                final Map<String, Object> param = new HashMap<>();
                param.put("INPUT", flowInput.getParam().get("INPUT"));
                param.put("BASE", workFlowBase);
                param.put("NODE", workFlowNode);
                param.put("STEP", workFlowStep);
                param.put("BUSINESS", workFlowBusiness);
                handler.processPass(param);
                result = (Map<String, Object>) param.get("result");
            }
        }
        // 初始节点使用启动操作员办理 特别注意 如果初始节点在子流程上，需要子流程所在公司操作员办理
        final String[] actors = queryService.getTaskActorsByTaskId(task.getId());
        if (actors == null) {
            taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
        }
        else if (actors != null && actors.length == 1) {
            if (BetterStringUtils.startsWith(actors[0], WorkFlowConstants.PREFIX_CUST_NO)) {
                taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
                taskService.removeTaskActor(task.getId(), actors);
            }
        }

        // 添加审批记录
        final WorkFlowAudit workFlowAudit = saveWorkFlowAudit(workFlowBase, workFlowNode, workFlowStep, flowInput, workFlowBusiness, task, "0");

        engine.executeTask(taskId, WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId), result);

        return workFlowAudit;
    }

    /**
     *
     * @param flowInput
     * @return
     */
    public WorkFlowAudit saveRejectTask(final WorkFlowInput flowInput) {
        flowInput.checkRejectTask();
        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();
        final String rejectNode = flowInput.getRejectNode();

        BTAssert.notNull(operId, "必须输入操作员编号");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        // 驳回指定节点
        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final TaskModel taskModel = taskService.getTaskModel(task.getId());
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();
        final WorkFlowStep workFlowStep = taskModel.getWorkFlowStep();

        WorkFlowBusiness workFlowBusiness = null;
        if (BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1")) {
            final Order order = queryService.getOrder(task.getOrderId());
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getParentId());
        }
        else {
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(task.getOrderId());
        }
        BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

        final String handlerName = workFlowNode.getHandler();
        Map<String, Object> result = null;
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final INodeHandler handler = SpringContextHolder.getBean(handlerName);
            if (handler != null) {
                final Map<String, Object> param = new HashMap<>();
                param.put("INPUT", flowInput.getParam().get("INPUT"));
                param.put("BASE", workFlowBase);
                param.put("NODE", workFlowNode);
                param.put("STEP", workFlowStep);
                param.put("BUSINESS", workFlowBusiness);
                handler.processReject(param);
                result = (Map<String, Object>) param.get("result");
            }
        }

        // 添加审批记录
        final WorkFlowAudit workFlowAudit = saveWorkFlowAudit(workFlowBase, workFlowNode, workFlowStep, flowInput, workFlowBusiness, task, "1");

        // 把所有节点找出来 当前用户节点驳回，其他节点 自动complete
        if (BetterStringUtils.equals(workFlowNode.getType(), "3") && workFlowStep != null
                && BetterStringUtils.equals(workFlowStep.getAuditType(), "1")) { // 确定是审批 并行节点
            final List<Task> activeTasks = queryService.getActiveTasks(new QueryFilter().setOrderId(task.getOrderId()));
            for (final Task activeTask : activeTasks) {
                final WorkFlowStep tempWorkFlowStep = taskService.getTaskModel(activeTask.getId()).getWorkFlowStep();
                if (tempWorkFlowStep.getId().equals(workFlowStep.getId()) && !BetterStringUtils.equals(task.getId(), activeTask.getId())) {
                    taskService.complete(activeTask.getId(), SnakerEngine.AUTO);
                }
            }
        }

        // 初始节点使用启动操作员办理 特别注意 如果初始节点在子流程上，需要子流程所在公司操作员办理
        final String[] actors = queryService.getTaskActorsByTaskId(task.getId());
        if (actors == null) {
            taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
        }
        else if (actors != null && actors.length == 1) {
            if (BetterStringUtils.startsWith(actors[0], WorkFlowConstants.PREFIX_CUST_NO)) {
                taskService.addTaskActor(task.getId(), (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
                taskService.removeTaskActor(task.getId(), actors);
            }
        }

        engine.executeAndJumpTask(taskId, WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId), result, rejectNode);

        return workFlowAudit;
    }

    /**
     *
     * @param flowInput
     * @return
     */
    public WorkFlowAudit saveHandleTask(final WorkFlowInput flowInput) {
        flowInput.checkPassParam();

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();

        BTAssert.notNull(operId, "必须输入OperId！");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务！");

        final Order order = queryService.getOrder(task.getOrderId());
        BTAssert.notNull(order, "没有找到相应的流程实例！");

        final Process process = processService.getProcessById(order.getProcessId());
        BTAssert.notNull(process, "没有找到流程定义流程定义！");

        final TaskModel taskModel = taskService.getTaskModel(task.getId());
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();
        BTAssert.notNull(workFlowBase, "没有找到流程基础定义！");
        BTAssert.notNull(workFlowNode, "没有找到流程节点定义！");

        BTAssert.isTrue(BetterStringUtils.equals(workFlowNode.getType(), "2"), "该节点不是经办节点！");

        // 处理 task 执行人
        final String[] actors = queryService.getTaskActorsByTaskId(taskId);
        if (actors == null || actors.length == 0
                || (actors.length == 1 && BetterStringUtils.startsWith(actors[0], WorkFlowConstants.PREFIX_CUST_NO))) {

            taskService.addTaskActor(taskId, (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId));

            taskService.removeTaskActor(taskId, actors);
        }

        final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(
                BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1") ? order.getParentId() : task.getOrderId());
        BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

        final String handlerName = workFlowNode.getHandler();
        Map<String, Object> result = null;
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final INodeHandler handler = SpringContextHolder.getBean(handlerName);
            if (handler != null) {
                final Map<String, Object> param = new HashMap<>();
                param.put("INPUT", flowInput.getParam().get("INPUT"));
                param.put("BASE", workFlowBase);
                param.put("NODE", workFlowNode);
                param.put("BUSINESS", workFlowBusiness);
                handler.processHandle(param);
                result = (Map<String, Object>) param.get("result");
            }
        }

        // 添加审批记录
        final WorkFlowAudit workFlowAudit = saveWorkFlowAudit(workFlowBase, workFlowNode, null, flowInput, workFlowBusiness, task, "2");

        engine.executeTask(taskId, WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId), result);

        return workFlowAudit;
    }

    /**
     *
     * @param workFlowInput
     * @return
     */
    public WorkFlowAudit saveCancelProcess(final WorkFlowInput flowInput) {
        flowInput.checkCancelProcess();

        final ITaskService taskService = engine.task();
        final IOrderService orderService = engine.order();
        final IQueryService queryService = engine.query();

        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final TaskModel taskModel = taskService.getTaskModel(task.getId());
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();
        final WorkFlowStep workFlowStep = taskModel.getWorkFlowStep();

        WorkFlowBusiness workFlowBusiness = null;
        final Order order = queryService.getOrder(task.getOrderId());
        if (BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1")) { // 如果是子流程，需要结束子流程和主流程
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getParentId());

            BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

            final WorkFlowBase mainWorkFlowBase = workFlowBaseService.findWorkFlowBaseById(workFlowBusiness.getBaseId());
            BTAssert.notNull(mainWorkFlowBase, "没有找到流程定义！");

            // 终止子流程
            final String handlerName = mainWorkFlowBase.getHandler();
            if (BetterStringUtils.isNotBlank(handlerName)) {
                final IProcessHandler handler = SpringContextHolder.getBean(handlerName);
                if (handler != null) {
                    final Map<String, Object> param = new HashMap<>();
                    param.put("INPUT", flowInput.getParam().get("INPUT"));
                    param.put("BASE", workFlowBase);
                    param.put("NODE", workFlowNode);
                    param.put("BUSINESS", workFlowBusiness);

                    handler.processCancel(param);
                }
            }

            // 终止子流程
            orderService.terminate(order.getId(), SnakerEngine.AUTO);

            // 终止主流程
            orderService.terminate(workFlowBusiness.getOrderId(), SnakerEngine.AUTO);
        }
        else {
            // 终止主流程
            workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());

            final String handlerName = workFlowBase.getHandler();
            if (BetterStringUtils.isNotBlank(handlerName)) {
                final IProcessHandler handler = SpringContextHolder.getBean(handlerName);
                if (handler != null) {
                    final Map<String, Object> param = new HashMap<>();
                    param.put("INPUT", flowInput.getParam().get("INPUT"));
                    param.put("BASE", workFlowBase);
                    param.put("NODE", workFlowNode);
                    param.put("BUSINESS", workFlowBusiness);

                    handler.processCancel(param);
                }
            }

            orderService.terminate(workFlowBusiness.getOrderId(), SnakerEngine.AUTO);
        }

        // 添加审批记录
        final WorkFlowAudit workFlowAudit = saveWorkFlowAudit(workFlowBase, workFlowNode, workFlowStep, flowInput, workFlowBusiness, task, "3");

        return workFlowAudit;
    }

    /**
     * 添加审核记录
     *
     * @param anWorkFlowBase
     * @param anWorkFlowNode
     * @param anWorkFlowStep
     * @param anWorkFlowInput
     * @param anWorkFlowBusiness
     * @param anTask
     * @param auditResult
     * @return
     */
    public WorkFlowAudit saveWorkFlowAudit(final WorkFlowBase anWorkFlowBase, final WorkFlowNode anWorkFlowNode, final WorkFlowStep anWorkFlowStep,
            final WorkFlowInput anWorkFlowInput, final WorkFlowBusiness anWorkFlowBusiness, final Task anTask, final String auditResult) {
        final WorkFlowAudit workFlowAudit = new WorkFlowAudit();
        workFlowAudit.setBaseId(anWorkFlowBase.getId());
        workFlowAudit.setNodeId(anWorkFlowNode.getId());
        workFlowAudit.setStepId(anWorkFlowStep != null ? anWorkFlowStep.getId() : null);
        workFlowAudit.setTaskId(anTask.getId());
        workFlowAudit.setOrderId(anTask.getOrderId());
        workFlowAudit.setOperId(anWorkFlowInput.getOperId());
        workFlowAudit.setOperName(anWorkFlowInput.getOperName());
        workFlowAudit.setCustNo(anWorkFlowBase.getCustNo());
        workFlowAudit.setCustName(anWorkFlowBase.getCustName());
        workFlowAudit.setBusinessId(anWorkFlowBusiness.getId());
        workFlowAudit.setAuditContent(anWorkFlowInput.getContent() == null ? "---" : anWorkFlowInput.getContent());
        workFlowAudit.setAuditDate(BetterDateUtils.getNumDate());
        workFlowAudit.setAuditTime(BetterDateUtils.getNumTime());
        workFlowAudit.setAuditResult(auditResult); // 作废
        workFlowAuditService.addWorkFlowAudit(workFlowAudit);

        return workFlowAudit;
    }

    /**
     *
     * @param anTaskId
     * @return
     */
    public List<SimpleDataEntity> queryRejectNodeList(final String anTaskId) {
        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();

        final Task task = queryService.getTask(anTaskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final TaskModel taskModel = taskService.getTaskModel(anTaskId);
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();

        final List<SimpleDataEntity> rejectNodeList = workFlowNodeService.queryRejectNodeList(workFlowBase, workFlowNode);

        return rejectNodeList;
    }

    /**
     * 经办节点保存数据
     *
     * @param flowInput
     */
    public void saveDataTask(final WorkFlowInput flowInput) {
        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();
        final Map<String, Object> param = (Map<String, Object>) flowInput.getParam().get("INPUT");

        BTAssert.notNull(operId, "必须输入OperId！");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务！");

        final Order order = queryService.getOrder(task.getOrderId());
        BTAssert.notNull(order, "没有找到相应的流程实例！");

        final Process process = processService.getProcessById(order.getProcessId());
        BTAssert.notNull(process, "没有找到流程定义流程定义！");

        final TaskModel taskModel = taskService.getTaskModel(task.getId());
        final WorkFlowBase workFlowBase = taskModel.getWorkFlowBase();
        final WorkFlowNode workFlowNode = taskModel.getWorkFlowNode();
        BTAssert.notNull(workFlowBase, "没有找到流程基础定义！");
        BTAssert.notNull(workFlowNode, "没有找到流程节点定义！");

        BTAssert.isTrue(BetterStringUtils.equals(workFlowNode.getType(), "2"), "该节点不是经办节点！");

        final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(
                BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1") ? order.getParentId() : task.getOrderId());
        BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

        final String handlerName = workFlowNode.getHandler();
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final INodeHandler handler = SpringContextHolder.getBean(handlerName);
            if (handler != null) {
                handler.processSave(param);
            }
        }
    }

    /**
     * 查询任务详情
     *
     * @param anTaskId
     * @param anOperId
     */
    public WorkFlowTask findTask(final String anTaskId, final Long anOperId) {
        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();
        final IProcessService processService = engine.process();

        final Task task = queryService.getTask(anTaskId);

        BTAssert.notNull(task, "没有找到相应的流程任务！");

        final String operator = task.getOperator();

        if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_OPER_ID)) { // 此节点操作员是一个操作员
            // 检查 operId 是否相等
            final Long tempOperId = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_OPER_ID));
            BTAssert.isTrue(anOperId.equals(tempOperId), "操作员不匹配！");
        }
        else if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_CUST_NO)) { // 此节点操作员是一个公司
            final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(anOperId);
            final List<Long> custNoList = custInfos.stream().map(custInfo -> {
                return custInfo.getCustNo();
            }).collect(Collectors.toList());
            final Long tempCustNo = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_CUST_NO));

            BTAssert.isTrue(custNoList.contains(tempCustNo), "当前操作员没有权限操作此任务！");
        }
        else {
            throw new BytterException("流程操作员不正确!");
        }

        // 返回流程任务
        final WorkFlowTask workFlowTask = new WorkFlowTask();

        final Order order = queryService.getOrder(task.getOrderId());
        BTAssert.notNull(order, "流程实例未找到！");

        final Process process = processService.getProcessById(order.getProcessId());
        BTAssert.notNull(process, "流程定义未找到！");

        final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());
        BTAssert.notNull(workFlowBusiness, "流程业务未找到！");

        workFlowTask.setId(task.getId());
        workFlowTask.setOperator(task.getOperator());
        workFlowTask.setProcessId(order.getProcessId());
        workFlowTask.setProcessName(SnakerHelper.getProcessName(order.getProcessId()));
        workFlowTask.setOrderId(task.getOrderId());
        workFlowTask.setParentId(order.getParentId());
        workFlowTask.setParentNodeName(order.getParentNodeName());
        workFlowTask.setParentTaskId(task.getParentTaskId());

        final TaskModel taskModel = taskService.getTaskModel(task.getId());

        workFlowTask.setWorkFlowBase(taskModel.getWorkFlowBase());
        workFlowTask.setWorkFlowNode(taskModel.getWorkFlowNode());
        workFlowTask.setWorkFlowStep(taskModel.getWorkFlowStep());
        workFlowTask.setWorkFlowBusiness(workFlowBusiness);

        return workFlowTask;
    }

    /**
     * 获取当前实例
     *
     * @param anOperId
     * @param anPageNo
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowOrder> queryCurrentOrder(final Long anOperId, final Integer anPageNo,
            final Integer anPageSize) {
        final IQueryService queryService = engine.query();
        final Page<Order> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);
        final List<String> operators = new ArrayList<>();

        final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(anOperId);

        operators.addAll(
                custInfos.stream().map(custInfo -> WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(custInfo.getId())).collect(Collectors.toList()));

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final List<Order> orders = queryService.getActiveOrders(page,
                new QueryFilter().setOperators(operators.toArray(new String[operators.size()])));

        final List<WorkFlowOrder> workFlowOrders = orders.stream().map(order -> {
            final WorkFlowOrder workFlowOrder = new WorkFlowOrder();
            workFlowOrder.setId(order.getId());
            workFlowOrder.setOrderNo(order.getOrderNo());
            workFlowOrder.setCreator(order.getCreator());
            workFlowOrder.setParentId(order.getParentId());
            workFlowOrder.setParentNodeName(order.getParentNodeName());
            workFlowOrder.setProcessId(order.getProcessId());
            workFlowOrder.setProcessName(SnakerHelper.getProcessName(order.getProcessId()));
            workFlowOrder.setCreateTime(order.getCreateTime());

            workFlowOrder.setWorkFlowBase(workFlowBaseService.findWorkFlowBaseByProcessId(order.getProcessId()));
            workFlowOrder.setWorkFlowBusiness(workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId()));
            return workFlowOrder;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowOrders, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * 查询历史实例
     *
     * @param anOperId
     * @param anPageNo
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowHistoryOrder> queryHistoryOrder(final Long anOperId, final Integer anPageNo,
            final Integer anPageSize) {
        final IQueryService queryService = engine.query();
        final Page<HistoryOrder> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        final List<String> operators = new ArrayList<>();

        final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(anOperId);

        operators.addAll(
                custInfos.stream().map(custInfo -> WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(custInfo.getId())).collect(Collectors.toList()));

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final List<HistoryOrder> historyOrders = queryService.getHistoryOrders(page,
                new QueryFilter().setOperators(operators.toArray(new String[operators.size()])));

        final List<WorkFlowHistoryOrder> workFlowHistoryOrders = historyOrders.stream().map(historyOrder -> {
            final WorkFlowHistoryOrder workFlowHistoryOrder = new WorkFlowHistoryOrder();
            workFlowHistoryOrder.setId(historyOrder.getId());
            workFlowHistoryOrder.setCreateTime(historyOrder.getCreateTime());
            workFlowHistoryOrder.setCreator(historyOrder.getCreator());
            workFlowHistoryOrder.setEndTime(historyOrder.getEndTime());
            workFlowHistoryOrder.setOrderNo(historyOrder.getOrderNo());
            workFlowHistoryOrder.setOrderState(historyOrder.getOrderState());
            workFlowHistoryOrder.setParentId(historyOrder.getParentId());
            workFlowHistoryOrder.setProcessId(historyOrder.getProcessId());
            workFlowHistoryOrder.setProcessName(SnakerHelper.getProcessName(historyOrder.getProcessId()));
            workFlowHistoryOrder.setVariable(historyOrder.getVariable());
            workFlowHistoryOrder.setPriority(historyOrder.getPriority());

            final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseByProcessId(historyOrder.getProcessId());
            workFlowHistoryOrder.setWorkFlowBase(workFlowBase);
            if (BetterStringUtils.equals(workFlowBase.getIsSubprocess(), WorkFlowConstants.IS_SUBPROCESS)) {
                workFlowHistoryOrder.setWorkFlowBusiness(workFlowBusinessService.findWorkFlowBusinessByOrderId(historyOrder.getParentId()));
            }
            return workFlowHistoryOrder;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowHistoryOrders, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * 查看待办工作项
     *
     * @param anOperId
     * @param anPageNo
     * @param anPageSize
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowWorkItem> queryWorkItem(final Long anOperId, final Integer anPageNo, final Integer anPageSize,
            final Map<String, Object> anParam) {
        final Page<WorkItem> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        final List<String> operators = new ArrayList<>();
        final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(anOperId);

        operators.addAll(
                custInfos.stream().map(custInfo -> WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(custInfo.getId())).collect(Collectors.toList()));

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();

        final QueryFilter queryFilter = new QueryFilter().setOperators(operators.toArray(new String[operators.size()]));
        if (anParam != null) {
            final String title = (String) anParam.get("title");
            final String startDate = (String) anParam.get("GTEauditDate");
            final String endDate = (String) anParam.get("LTEauditDate");

            if (BetterStringUtils.isNotBlank(title)) {
                queryFilter.setDisplayName(title);
            }
            if (BetterStringUtils.isNotBlank(startDate) && startDate.length() == 8) {
                queryFilter.setCreateTimeStart(BetterDateUtils.formatDispDate(startDate));
            }
            if (BetterStringUtils.isNotBlank(endDate) && endDate.length() == 8) {
                queryFilter.setCreateTimeEnd(BetterDateUtils.formatDispDate(endDate) + _23_59_59);
            }
        }
        final List<WorkItem> workItems = queryService.getWorkItems(page, queryFilter);

        final List<WorkFlowWorkItem> workFlowWorkItems = workItems.stream().map(workItem -> {
            final TaskModel taskModel = taskService.getTaskModel(workItem.getTaskId());
            WorkFlowBusiness workFlowBusiness = null;
            if (BetterStringUtils.equals(taskModel.getWorkFlowBase().getIsSubprocess(), "1")) {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getParentId());
            }
            else {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getOrderId());
            }
            final String[] actors = queryService.getTaskActorsByTaskId(workItem.getTaskId());
            final String[] actorNames = processActorNames(actors);

            final WorkFlowWorkItem workFlowWorkItem = new WorkFlowWorkItem(taskModel.getWorkFlowBase(), taskModel.getWorkFlowNode(),
                    taskModel.getWorkFlowStep(), workFlowBusiness, actors, actorNames);

            initWorkFLowWorkItem(workFlowWorkItem, workItem);
            return workFlowWorkItem;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowWorkItems, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * 查询已办工作项
     *
     * @param anOperId
     * @param anPageNo
     * @param anPageSize
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowWorkItem> queryHistoryWorkItem(final Long anOperId, final Integer anPageNo,
            final Integer anPageSize, final Map<String, Object> anParam) {
        final Page<WorkItem> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        final List<String> operators = new ArrayList<>();

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();

        final QueryFilter queryFilter = new QueryFilter().setOperators(operators.toArray(new String[operators.size()]));
        if (anParam != null) {
            final String title = (String) anParam.get("title");
            final String startDate = (String) anParam.get("GTEauditDate");
            final String endDate = (String) anParam.get("LTEauditDate");

            if (BetterStringUtils.isNotBlank(title)) {
                queryFilter.setDisplayName(title);
            }
            if (BetterStringUtils.isNotBlank(startDate) && startDate.length() == 8) {
                queryFilter.setCreateTimeStart(BetterDateUtils.formatDispDate(startDate));
            }
            if (BetterStringUtils.isNotBlank(endDate) && endDate.length() == 8) {
                queryFilter.setCreateTimeEnd(BetterDateUtils.formatDispDate(endDate) + _23_59_59);
            }
        }

        final List<WorkItem> workItems = queryService.getHistoryWorkItems(page, queryFilter);

        final List<WorkFlowWorkItem> workFlowWorkItems = workItems.stream().map(workItem -> {
            final TaskModel taskModel = taskService.getHistoryTaskModel(workItem.getTaskId());
            WorkFlowBusiness workFlowBusiness = null;
            if (BetterStringUtils.equals(taskModel.getWorkFlowBase().getIsSubprocess(), "1")) {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getParentId());
            }
            else {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getOrderId());
            }
            final String[] actors = new String[1];
            actors[0] = workItem.getOperator();
            final String[] actorNames = processActorNames(actors);

            final WorkFlowWorkItem workFlowWorkItem = new WorkFlowWorkItem(taskModel.getWorkFlowBase(), taskModel.getWorkFlowNode(),
                    taskModel.getWorkFlowStep(), workFlowBusiness, actors, actorNames);

            initWorkFLowWorkItem(workFlowWorkItem, workItem);
            return workFlowWorkItem;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowWorkItems, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * 查询当前任务
     *
     * @param anOperId
     * @param anPageNo
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowTask> queryCurrentTask(final Long anOperId, final Integer anPageNo, final Integer anPageSize,
            final Map<String, Object> anParam) {
        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Page<Task> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        final List<String> operators = new ArrayList<>();
        final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(anOperId);

        operators.addAll(
                custInfos.stream().map(custInfo -> WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(custInfo.getId())).collect(Collectors.toList()));

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final List<Task> tasks = queryService.getActiveTasks(page, new QueryFilter().setOperators(operators.toArray(new String[operators.size()])));

        final List<WorkFlowTask> workFlowTasks = tasks.stream().map(task -> {
            final WorkFlowTask workFlowTask = new WorkFlowTask();

            workFlowTask.setId(task.getId());
            workFlowTask.setOperator(task.getOperator());
            workFlowTask.setOrderId(task.getOrderId());
            workFlowTask.setCreateTime(task.getCreateTime());
            workFlowTask.setTaskName(task.getTaskName());
            workFlowTask.setTaskType(task.getTaskType());
            workFlowTask.setParentTaskId(task.getParentTaskId());

            final Order order = queryService.getOrder(task.getOrderId());
            BTAssert.notNull(order, "流程实例未找到！");

            final Process process = processService.getProcessById(order.getProcessId());
            BTAssert.notNull(process, "流程定义未找到！");

            workFlowTask.setProcessId(order.getProcessId());
            workFlowTask.setProcessName(SnakerHelper.getProcessName(order.getProcessId()));

            final TaskModel taskModel = taskService.getTaskModel(task.getId());

            workFlowTask.setWorkFlowBase(taskModel.getWorkFlowBase());
            workFlowTask.setWorkFlowNode(taskModel.getWorkFlowNode());
            workFlowTask.setWorkFlowStep(taskModel.getWorkFlowStep());

            final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());
            workFlowTask.setWorkFlowBusiness(workFlowBusiness);

            return workFlowTask;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowTasks, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * 查询历史任务
     *
     * @param anOperId
     * @param anPageNo
     * @param anParam
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowHistoryTask> queryHistoryTask(final Long anOperId, final Integer anPageNo,
            final Integer anPageSize, final Map<String, Object> anParam) {
        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Page<HistoryTask> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        final List<String> operators = new ArrayList<>();

        // 获取当前用户拥有的公司
        final String operId = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId);
        operators.add(operId);

        final List<HistoryTask> tasks = queryService.getHistoryTasks(page,
                new QueryFilter().setOperators(operators.toArray(new String[operators.size()])));

        final List<WorkFlowHistoryTask> workFlowTasks = tasks.stream().map(task -> {
            final WorkFlowHistoryTask workFlowTask = new WorkFlowHistoryTask();

            workFlowTask.setId(task.getId());
            workFlowTask.setOperator(task.getOperator());
            workFlowTask.setOrderId(task.getOrderId());
            workFlowTask.setCreateTime(task.getCreateTime());
            workFlowTask.setFinishTime(task.getFinishTime());
            workFlowTask.setTaskName(task.getTaskName());
            workFlowTask.setTaskType(task.getTaskType());
            workFlowTask.setParentTaskId(task.getParentTaskId());

            final HistoryOrder order = queryService.getHistOrder(task.getOrderId());
            BTAssert.notNull(order, "流程实例未找到！");

            final Process process = processService.getProcessById(order.getProcessId());
            BTAssert.notNull(process, "流程定义未找到！");

            workFlowTask.setProcessId(order.getProcessId());
            workFlowTask.setProcessName(SnakerHelper.getProcessName(order.getProcessId()));

            final TaskModel taskModel = taskService.getHistoryTaskModel(task.getId());

            workFlowTask.setWorkFlowBase(taskModel.getWorkFlowBase());
            workFlowTask.setWorkFlowNode(taskModel.getWorkFlowNode());
            workFlowTask.setWorkFlowStep(taskModel.getWorkFlowStep());

            final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());
            workFlowTask.setWorkFlowBusiness(workFlowBusiness);

            return workFlowTask;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowTasks, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());

    }

    /**
     * 查询审批记录
     *
     * @param anTaskId
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowAudit> queryWorkFlowAudit(final String anBusinessId, final int anFlag, final int anPageNum,
            final int anPageSize) {

        final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessById(anBusinessId);
        BTAssert.notNull(workFlowBusiness, "没有找到业务记录！");

        final Long baseId = workFlowBusiness.getBaseId();
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(baseId);
        BTAssert.notNull(workFlowBase, "没有找到流程信息！");

        final com.betterjr.mapper.pagehelper.Page<WorkFlowAudit> workFlowAudits = workFlowAuditService
                .queryWorkFlowAuditByBusinessId(workFlowBusiness.getId(), anFlag, anPageNum, anPageSize);

        workFlowAudits.forEach(workFlowAudit -> {
            workFlowAudit.setBaseName(workFlowBase.getNickname());
            workFlowAudit.setNodeName(workFlowNodeService.findWorkFlowNodeById(workFlowAudit.getNodeId()).getNickname());
            if (workFlowAudit.getStepId() != null) {
                final WorkFlowStep workFlowStep = workFlowStepService.findWorkFlowStepById(workFlowAudit.getStepId());
                if (workFlowStep != null) {
                    workFlowAudit.setStepName(workFlowStep.getNickname());
                }
            }
        });

        return workFlowAudits;
    }

    /**
     * 查询已办工作项
     *
     * @param anOperId
     * @param anPageNo
     * @param anPageSize
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowWorkItem> queryMonitorWorkItem(final Long anCustNo, final Integer anPageNo,
            final Integer anPageSize, final Map<String, Object> anParam) {
        final Page<WorkItem> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);

        // 获取当前用户拥有的公司

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final List<Process> processes = processService.getProcesss(new QueryFilter().setCustNo(anCustNo));

        if (Collections3.isEmpty(processes)) {
            return com.betterjr.mapper.pagehelper.Page.emptyPage();
        }

        final List<String> processIdList = processes.stream().map(process -> process.getId()).collect(Collectors.toList());
        final String[] processIds = processIdList.toArray(new String[processIdList.size()]);


        final QueryFilter queryFilter = new QueryFilter().setProcessIds(processIds);
        if (anParam != null) {
            final String title = (String) anParam.get("title");
            final String startDate = (String) anParam.get("GTEauditDate");
            final String endDate = (String) anParam.get("LTEauditDate");

            if (BetterStringUtils.isNotBlank(title)) {
                queryFilter.setDisplayName(title);
            }
            if (BetterStringUtils.isNotBlank(startDate) && startDate.length() == 8) {
                queryFilter.setCreateTimeStart(BetterDateUtils.formatDispDate(startDate));
            }
            if (BetterStringUtils.isNotBlank(endDate) && endDate.length() == 8) {
                queryFilter.setCreateTimeEnd(BetterDateUtils.formatDispDate(endDate) + _23_59_59);
            }
        }

        final List<WorkItem> workItems = queryService.getWorkItems(page, queryFilter);

        final List<WorkFlowWorkItem> workFlowWorkItems = workItems.stream().map(workItem -> {
            final TaskModel taskModel = taskService.getTaskModel(workItem.getTaskId());
            WorkFlowBusiness workFlowBusiness = null;
            if (BetterStringUtils.equals(taskModel.getWorkFlowBase().getIsSubprocess(), "1")) {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getParentId());
            }
            else {
                workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(workItem.getOrderId());
            }
            final String[] actors = queryService.getTaskActorsByTaskId(workItem.getTaskId());
            final String[] actorNames = processActorNames(actors);

            final WorkFlowWorkItem workFlowWorkItem = new WorkFlowWorkItem(taskModel.getWorkFlowBase(), taskModel.getWorkFlowNode(),
                    taskModel.getWorkFlowStep(), workFlowBusiness, actors, actorNames);

            initWorkFLowWorkItem(workFlowWorkItem, workItem);
            return workFlowWorkItem;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowWorkItems, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    /**
     * @param anActors
     * @return
     */
    private String[] processActorNames(final String[] anActors) {
        if (anActors == null) {
            return null;
        }
        final String[] actorNames = new String[anActors.length];

        for (int i = 0; i < anActors.length; i++) {
            if (BetterStringUtils.equals(anActors[i], SnakerEngine.ADMIN)) {
                actorNames[i] = "系统管理执行";
            }
            else if (BetterStringUtils.equals(anActors[i], SnakerEngine.AUTO)) {
                actorNames[i] = "系统自动执行";
            }
            else if (BetterStringUtils.startsWith(anActors[i], WorkFlowConstants.PREFIX_OPER_ID)) {
                final Long operId = Long.valueOf(BetterStringUtils.substring(anActors[i], WorkFlowConstants.PREFIX_OPER_ID.length()));
                final CustOperatorInfo operator = custOperatorService.findCustOperatorById(operId);
                BTAssert.notNull(operator, "没有找到操作员！");
                actorNames[i] = operator.getName();
            }
            else if (BetterStringUtils.startsWith(anActors[i], WorkFlowConstants.PREFIX_CUST_NO)) {
                final Long custNo = Long.valueOf(BetterStringUtils.substring(anActors[i], WorkFlowConstants.PREFIX_CUST_NO.length()));
                final CustMechBase custInfo = custMechBaseService.findBaseInfo(custNo);
                BTAssert.notNull(custInfo, "没有找到操作员！");
                actorNames[i] = custInfo.getCustName();
            }
            else {
                throw new BytterException("不能识别的操作员！");
            }
        }
        return actorNames;
    }

    /**
     * 查询当前公司拥有的所有活动流程
     *
     * @param anCustNo
     * @param anPageNo
     * @param anPageSize
     * @return
     */
    public com.betterjr.mapper.pagehelper.Page<WorkFlowOrder> queryMonitorTask(final Long anCustNo, final Integer anPageNo,
            final Integer anPageSize) {
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Page<Order> page = new Page<>();
        page.setPageNo(anPageNo);
        page.setPageSize(anPageSize);
        final List<Process> processes = processService.getProcesss(new QueryFilter().setCustNo(anCustNo));

        final List<String> processIdList = processes.stream().map(process -> process.getId()).collect(Collectors.toList());
        final String[] processIds = processIdList.toArray(new String[processIdList.size()]);
        final List<Order> orders = queryService.getActiveOrders(page, new QueryFilter().setProcessIds(processIds));

        final List<WorkFlowOrder> workFlowOrders = orders.stream().map(order -> {
            final WorkFlowOrder workFlowOrder = new WorkFlowOrder();
            workFlowOrder.setId(order.getId());
            workFlowOrder.setOrderNo(order.getOrderNo());
            workFlowOrder.setCreator(order.getCreator());
            workFlowOrder.setParentId(order.getParentId());
            workFlowOrder.setParentNodeName(order.getParentNodeName());
            workFlowOrder.setProcessId(order.getProcessId());
            workFlowOrder.setProcessName(SnakerHelper.getProcessName(order.getProcessId()));
            workFlowOrder.setCreateTime(order.getCreateTime());

            workFlowOrder.setWorkFlowBase(workFlowBaseService.findWorkFlowBaseByProcessId(order.getProcessId()));
            workFlowOrder.setWorkFlowBusiness(workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId()));
            return workFlowOrder;
        }).collect(Collectors.toList());

        return com.betterjr.mapper.pagehelper.Page.listToPage(workFlowOrders, page.getPageNo(), anPageSize,
                Long.valueOf(page.getTotalPages()).intValue(), page.getPageNo() * anPageSize, page.getTotalCount());
    }

    // 修改指定流程节点任务的执行人
    public Task saveChangeApprover(final String anTaskId, final Long anOperId) {
        // 当前用户是否
        BTAssert.isTrue(BetterStringUtils.isNotBlank(anTaskId), "任务编号不允许为空！");
        BTAssert.notNull(anOperId, "操作员编号不允许为空！");

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Task task = queryService.getTask(anTaskId);
        BTAssert.notNull(task, "没有找到指定任务！");

        final Order order = queryService.getOrder(task.getOrderId());
        BTAssert.notNull(order, "没有找到指定流程实例！");

        final Process process = processService.getProcessById(order.getProcessId());
        BTAssert.notNull(process, "没有找到指定流程！");

        final Long custNo = process.getCustNo();

        final List<CustOperatorInfo> operatorInfos = custOperatorService.queryOperatorByCustNo(custNo);
        boolean flag = false;
        for (final CustOperatorInfo operator : operatorInfos) {
            if (anOperId.equals(operator.getId())) {
                flag = true;
                break;
            }
        }
        BTAssert.isTrue(flag, "操作员不匹配！");

        final String[] actors = queryService.getTaskActorsByTaskId(anTaskId);

        BTAssert.isTrue(actors.length == 1, "没有找到现有操作员！");

        if (BetterStringUtils.equals(actors[0], WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId))) {
            return task;
        }

        taskService.addTaskActor(anTaskId, (new String[1])[0] = WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId));

        taskService.removeTaskActor(anTaskId, actors);

        return task;
    }

    /**
     * @param anBusinessId
     * @return
     */
    public Map<String, String> findWorkFlowJson(final String anProcessId, final String anOrderId) {

        final IProcessService processService = engine.process();
        final IQueryService queryService = engine.query();

        final org.snaker.engine.entity.Process process = processService.getProcessById(anProcessId);
        BTAssert.notNull(process, "没有找到流程信息！");

        final ProcessModel model = process.getModel();
        final Map<String, String> jsonMap = new HashMap<String, String>();
        if (model != null) {
            jsonMap.put("process", SnakerHelper.getModelJson(model));
        }

        if (BetterStringUtils.isNotEmpty(anOrderId)) {
            final HistoryOrder histOrder = queryService.getHistOrder(anOrderId);
            BTAssert.notNull(histOrder, "没有找到流程实例！");
            BTAssert.isTrue(BetterStringUtils.equals(anProcessId, histOrder.getProcessId()), "流程实例与流程不匹配");

            final List<Task> tasks = this.engine.query().getActiveTasks(new QueryFilter().setOrderId(anOrderId));
            final List<HistoryTask> historyTasks = this.engine.query().getHistoryTasks(new QueryFilter().setOrderId(anOrderId));
            jsonMap.put("state", SnakerHelper.getStateJson(model, tasks, historyTasks));
        }
        return jsonMap;
    }

    private void initWorkFLowWorkItem(final WorkFlowWorkItem workFlowWorkItem, final WorkItem workItem) {
        workFlowWorkItem.setProcessId(workItem.getProcessId());
        workFlowWorkItem.setOrderId(workItem.getOrderId());
        workFlowWorkItem.setTaskId(workItem.getTaskId());
        workFlowWorkItem.setProcessName(workItem.getProcessName());
        workFlowWorkItem.setCreator(workItem.getCreator());
        workFlowWorkItem.setOrderCreateTime(workItem.getOrderCreateTime());
        workFlowWorkItem.setOrderEndTime(workItem.getOrderEndTime());
        workFlowWorkItem.setOrderExpireTime(workItem.getOrderExpireTime());
        workFlowWorkItem.setOrderNo(workItem.getOrderNo());
        workFlowWorkItem.setTaskName(workItem.getTaskName());
        workFlowWorkItem.setTaskKey(workItem.getTaskKey());
        workFlowWorkItem.setPerformType(workItem.getPerformType());
        workFlowWorkItem.setTaskType(workItem.getTaskType());
        workFlowWorkItem.setTaskState(workItem.getTaskState());
        workFlowWorkItem.setTaskCreateTime(workItem.getTaskCreateTime());
        workFlowWorkItem.setTaskEndTime(workItem.getTaskEndTime());
        workFlowWorkItem.setTaskExpireTime(workItem.getTaskExpireTime());
        workFlowWorkItem.setOperator(workItem.getOperator());
    }
}
