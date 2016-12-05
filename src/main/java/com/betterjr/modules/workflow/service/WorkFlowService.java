// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.account.dubbo.interfaces.ICustOperatorService;
import com.betterjr.modules.account.entity.CustInfo;
import com.betterjr.modules.customer.ICustMechBaseService;
import com.betterjr.modules.customer.entity.CustMechBase;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.constants.WorkFlowInput;
import com.betterjr.modules.workflow.data.WorkFlowHistoryOrder;
import com.betterjr.modules.workflow.data.WorkFlowOrder;
import com.betterjr.modules.workflow.data.WorkFlowTask;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.snaker.util.SnakerHelper;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowService {

    @Reference(interfaceClass = ICustMechBaseService.class)
    private ICustMechBaseService custMechBaseService;

    @Reference(interfaceClass = ICustOperatorService.class)
    private ICustOperatorService custOperatorService;

    @Inject
    private SnakerEngine engine;

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowAuditService workFlowAuditService;

    @Inject
    private WorkFlowBusinessService workFlowBusinessService;

    /**
     * 启动流程
     *
     * @param flowInput
     * @return
     */
    public WorkFlowBusiness saveStart(final WorkFlowInput flowInput) {
        flowInput.checkStartParam();

        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();

        final CustMechBase custMechBase = custMechBaseService.findBaseInfo(flowInput.getFlowCustNo());
        BTAssert.notNull(custMechBase, "没有找到启动流程公司！");

        final Order order = engine.startInstanceByName(flowInput.getFlowName(), flowInput.getFlowCustNo(), null,
                WorkFlowConstants.PREFIX_CUST_NO + flowInput.getFlowCustNo(), flowInput.getParam());
        BTAssert.notNull(order, "启动流程出现错误！");

        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseByProcessId(order.getProcessId());
        BTAssert.notNull(workFlowBase, "没有找到流程定义！");

        final WorkFlowBusiness workFlowBusiness = new WorkFlowBusiness();
        workFlowBusiness.setOrderId(order.getId());
        workFlowBusiness.setBaseId(workFlowBase.getId());
        workFlowBusiness.setBusinessId(flowInput.getBusinessId());
        workFlowBusiness.setBusinessType(flowInput.getBusinessType());

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
        // task.setOperator(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
        taskService.take(task.getId(), WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));

        // 启动流程，并处理第一个经办任务
        final List<Task> resultTasks = engine.executeTask(task.getId(), WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));

        return result;
    }

    /**
     *
     * @param flowInput
     * @return
     */
    public List<Task> savePassTask(final WorkFlowInput flowInput) {
        flowInput.checkPassParam();

        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();
        BTAssert.notNull(operId, "必须输入OperId");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final String operator = task.getOperator();

        if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_OPER_ID)) { // 此节点操作员是一个操作员
            // 检查 operId 是否相等
            final Long tempOperId = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_OPER_ID));
            BTAssert.isTrue(operId.equals(tempOperId), "操作员不匹配！");
        }
        else if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_CUST_NO)) { // 此节点操作员是一个公司
            final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(operId);
            final List<Long> custNoList = custInfos.stream().map(custInfo -> {
                return custInfo.getCustNo();
            }).collect(Collectors.toList());
            final Long tempCustNo = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_CUST_NO));

            BTAssert.isTrue(custNoList.contains(tempCustNo), "当前操作员没有权限操作此任务！");
            // 如果可以执行，先将任务执行者转为当前操作员
            taskService.take(task.getId(), WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId));
        }
        else {
            throw new BytterException("流程操作员不正确!");
        }

        final List<Task> tasks = engine.executeTask(taskId, WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId));

        return tasks;
    }

    /**
     *
     * @param flowInput
     * @return
     */
    public List<Task> saveRejectTask(final WorkFlowInput flowInput) {
        flowInput.checkRejectTask();
        // 驳回上一步
        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();
        BTAssert.notNull(operId, "必须输入操作员编号");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        // 驳回指定节点
        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final String operator = task.getOperator();

        if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_OPER_ID)) { // 此节点操作员是一个操作员
            // 检查 operId 是否相等
            final Long tempOperId = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_OPER_ID));
            BTAssert.isTrue(operId.equals(tempOperId), "操作员不匹配！");
        }
        else if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_CUST_NO)) { // 此节点操作员是一个公司
            final Collection<CustInfo> custInfos = custMechBaseService.queryCustInfoByOperId(operId);
            final List<Long> custNoList = custInfos.stream().map(custInfo -> {
                return custInfo.getCustNo();
            }).collect(Collectors.toList());
            final Long tempCustNo = Long.valueOf(BetterStringUtils.removeStart(operator, WorkFlowConstants.PREFIX_CUST_NO));

            BTAssert.isTrue(custNoList.contains(tempCustNo), "当前操作员没有权限操作此任务！");
            // 如果可以执行，先将任务执行者转为当前操作员
            taskService.take(task.getId(), WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId));
        }
        else {
            throw new BytterException("流程操作员不正确!");
        }

        final List<Task> tasks = engine.executeAndJumpTask(taskId, WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(operId), null, null);

        return tasks;
    }

    /**
     *
     * @param anTaskId
     * @param anOperId
     */
    public WorkFlowTask findTask(final String anTaskId, final Long anOperId) {
        final IQueryService queryService = engine.query();
        final ITaskService taskService = engine.task();
        final IOrderService orderService = engine.order();
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
        final ProcessModel processModel = process.getModel();

        // TODO 将流程相关的各种定义点也准备好 如果有步骤的，将步骤也准备好
        workFlowTask.setWorkFlowBase(processModel.getWorkFlowBase());
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
    public List<WorkFlowOrder> queryCurrentOrder(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<Order> page = new Page<>();
        page.setPageNo(anPageNo);
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

        return workFlowOrders;
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public List<WorkFlowHistoryOrder> queryHistoryOrder(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<HistoryOrder> page = new Page<>();
        page.setPageNo(anPageNo);
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

        return workFlowHistoryOrders;
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public List<WorkFlowTask> queryCurrentTask(final Long anOperId, final Integer anPageNo) {
        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();
        final IProcessService processService = engine.process();

        final Page<Task> page = new Page<>();
        page.setPageNo(anPageNo);

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
            final ProcessModel processModel = process.getModel();

            workFlowTask.setWorkFlowBase(processModel.getWorkFlowBase());
            workFlowTask.setWorkFlowNode(taskModel.getWorkFlowNode());
            workFlowTask.setWorkFlowStep(taskModel.getWorkFlowStep());

            final WorkFlowBusiness workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());
            workFlowTask.setWorkFlowBusiness(workFlowBusiness);

            return workFlowTask;
        }).collect(Collectors.toList());
        return workFlowTasks;
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public void queryHistoryTask(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<HistoryTask> page = new Page<>();
        page.setPageNo(anPageNo);
        final List<HistoryTask> tasks = queryService.getHistoryTasks(page, new QueryFilter().setOperator(String.valueOf(anOperId)));
    }

    public void queryMonitorTask(final Integer anPageNo) {

    }

    public void saveChangeApprover() {

    }
}
