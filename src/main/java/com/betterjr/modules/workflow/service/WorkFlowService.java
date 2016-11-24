// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.List;

import javax.inject.Inject;

import org.snaker.engine.IQueryService;
import org.snaker.engine.ITaskService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Task;
import org.springframework.stereotype.Service;

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.data.WorkFlowInput;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowService {

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
        final ITaskService taskService = engine.task();
        final IQueryService queryService = engine.query();

        final Order order = engine.startInstanceByName(flowInput.getFlowName(), flowInput.getFlowCustNo(), null,
                String.valueOf(flowInput.getOperId()), flowInput.getParam());
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
        task.setOperator(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(flowInput.getOperId()));
        taskService.updateTask(task);

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
        final Long operId = flowInput.getOperId();
        final String taskId = flowInput.getTaskId();
        BTAssert.notNull(operId, "必须输入OperId");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(taskId), "必须输入任务编号");

        final IQueryService queryService = engine.query();

        final Task task = queryService.getTask(taskId);
        BTAssert.notNull(task, "没有找到相应任务");

        final String operator = task.getOperator();

        if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_OPER_ID)) { // 此节点操作员是一个操作员
            // 检查 operId 是否相等
        }
        if (BetterStringUtils.startsWith(operator, WorkFlowConstants.PREFIX_CUST_NO)) { // 此节点操作员是一个公司
            // TODO 检查当前用户是否属于此公司
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
        // 驳回上一步

        // 驳回指定节点

        return null;
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public void queryCurrentOrder(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<Order> page = new Page<>();
        page.setPageNo(anPageNo);
        final List<Order> orders = queryService.getActiveOrders(page, new QueryFilter().setOperator(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId)));
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public void queryHistoryOrder(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<HistoryOrder> page = new Page<>();
        page.setPageNo(anPageNo);
        final List<HistoryOrder> orders = queryService.getHistoryOrders(page, new QueryFilter().setOperator(WorkFlowConstants.PREFIX_OPER_ID + String.valueOf(anOperId)));
    }

    /**
     *
     * @param anOperId
     * @param anPageNo
     */
    public void queryCurrentTask(final Long anOperId, final Integer anPageNo) {
        final IQueryService queryService = engine.query();
        final Page<Task> page = new Page<>();
        page.setPageNo(anPageNo);
        final List<Task> tasks = queryService.getActiveTasks(page, new QueryFilter().setOperator(String.valueOf(anOperId)));
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
