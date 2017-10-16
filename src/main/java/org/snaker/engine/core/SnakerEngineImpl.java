/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.DBAccess;
import org.snaker.engine.IManagerService;
import org.snaker.engine.IOrderService;
import org.snaker.engine.IProcessService;
import org.snaker.engine.IQueryService;
import org.snaker.engine.ITaskService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.transaction.TransactionInterceptor;
import org.snaker.engine.cache.CacheManager;
import org.snaker.engine.cache.CacheManagerAware;
import org.snaker.engine.cache.memory.MemoryCacheManager;
import org.snaker.engine.cfg.Configuration;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.StartModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TransitionModel;

/**
 * 基本的流程引擎实现类
 * @author yuqs
 * @since 1.0
 */
public class SnakerEngineImpl implements SnakerEngine {
    private static final Logger log = LoggerFactory.getLogger(SnakerEngineImpl.class);
    /**
     * Snaker配置对象
     */
    protected Configuration configuration;
    /**
     * 流程定义业务类
     */
    protected IProcessService processService;
    /**
     * 流程实例业务类
     */
    protected IOrderService orderService;
    /**
     * 任务业务类
     */
    protected ITaskService taskService;
    /**
     * 查询业务类
     */
    protected IQueryService queryService;
    /**
     * 管理业务类
     */
    protected IManagerService managerService;

    /**
     * 根据serviceContext上下文，查找processService、orderService、taskService服务
     */
    @Override
    public SnakerEngine configure(final Configuration config) {
        this.configuration = config;
        processService = ServiceContext.find(IProcessService.class);
        queryService = ServiceContext.find(IQueryService.class);
        orderService = ServiceContext.find(IOrderService.class);
        taskService = ServiceContext.find(ITaskService.class);
        managerService = ServiceContext.find(IManagerService.class);

        /*
         * 无spring环境，DBAccess的实现类通过服务上下文获取
         */
        if (!this.configuration.isCMB()) {
            final DBAccess access = ServiceContext.find(DBAccess.class);
            AssertHelper.notNull(access);
            final TransactionInterceptor interceptor = ServiceContext.find(TransactionInterceptor.class);
            // 如果初始化配置时提供了访问对象，就对DBAccess进行初始化
            final Object accessObject = this.configuration.getAccessDBObject();
            if (accessObject != null) {
                if (interceptor != null) {
                    interceptor.initialize(accessObject);
                }
                access.initialize(accessObject);
            }
            setDBAccess(access);
            access.runScript();
        }
        CacheManager cacheManager = ServiceContext.find(CacheManager.class);
        if (cacheManager == null) {
            // 默认使用内存缓存管理器
            cacheManager = new MemoryCacheManager();
        }
        final List<CacheManagerAware> cacheServices = ServiceContext.findList(CacheManagerAware.class);
        for (final CacheManagerAware cacheService : cacheServices) {
            cacheService.setCacheManager(cacheManager);
        }
        return this;
    }

    /**
     * 注入dbAccess
     * @param access db访问对象
     */
    protected void setDBAccess(final DBAccess access) {
        final List<AccessService> services = ServiceContext.findList(AccessService.class);
        for (final AccessService service : services) {
            service.setAccess(access);
        }
    }

    /**
     * 获取流程定义服务
     */
    @Override
    public IProcessService process() {
        AssertHelper.notNull(processService);
        return processService;
    }

    /**
     * 获取查询服务
     */
    @Override
    public IQueryService query() {
        AssertHelper.notNull(queryService);
        return queryService;
    }

    /**
     * 获取实例服务
     * @since 1.2.2
     */
    @Override
    public IOrderService order() {
        AssertHelper.notNull(orderService);
        return orderService;
    }

    /**
     * 获取任务服务
     * @since 1.2.2
     */
    @Override
    public ITaskService task() {
        AssertHelper.notNull(taskService);
        return taskService;
    }

    /**
     * 获取管理服务
     * @since 1.4
     */
    @Override
    public IManagerService manager() {
        AssertHelper.notNull(managerService);
        return managerService;
    }

    /**
     * 根据流程定义ID启动流程实例
     */
    @Override
    public Order startInstanceById(final String id) {
        return startInstanceById(id, null, null);
    }

    /**
     * 根据流程定义ID，操作人ID启动流程实例
     */
    @Override
    public Order startInstanceById(final String id, final String operator) {
        return startInstanceById(id, operator, null);
    }

    /**
     * 根据流程定义ID，操作人ID，参数列表启动流程实例
     */
    @Override
    public Order startInstanceById(final String id, final String operator, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<String, Object>();
        }
        final Process process = process().getProcessById(id);
        process().check(process, id);
        return startProcess(process, operator, args);
    }

    /**
     * 根据流程名称启动流程实例
     * @since 1.3
     */
    @Override
    public Order startInstanceByName(final String name, final Long custNo) {
        return startInstanceByName(name, custNo, null, null, null);
    }

    /**
     * 根据流程名称、版本号启动流程实例
     * @since 1.3
     */
    @Override
    public Order startInstanceByName(final String name, final Long custNo, final Integer version) {
        return startInstanceByName(name, custNo, version, null, null);
    }

    /**
     * 根据流程名称、版本号、操作人启动流程实例
     * @since 1.3
     */
    @Override
    public Order startInstanceByName(final String name, final Long custNo, final Integer version,
            final String operator) {
        return startInstanceByName(name, custNo, version, operator, null);
    }

    /**
     * 根据流程名称、版本号、操作人、参数列表启动流程实例
     * @since 1.3
     */
    @Override
    public Order startInstanceByName(final String name, final Long custNo, final Integer version, final String operator,
            Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<String, Object>();
        }
        final Process process = process().getProcessByVersion(name, custNo, version);
        process().check(process, name);
        return startProcess(process, operator, args);
    }

    private Order startProcess(final Process process, final String operator, final Map<String, Object> args) {
        final Execution execution = execute(process, operator, args, null, null);
        if (process.getModel() != null) {
            final StartModel start = process.getModel().getStart();
            AssertHelper.notNull(start,
                    "流程定义[name=" + process.getName() + ", version=" + process.getVersion() + "]没有开始节点");
            start.execute(execution);
        }

        return execution.getOrder();
    }

    /**
     * 根据父执行对象启动子流程实例（用于启动子流程）
     */
    @Override
    public Order startInstanceByExecution(final Execution execution) {
        final Process process = execution.getProcess();
        final StartModel start = process.getModel().getStart();
        AssertHelper.notNull(start, "流程定义[id=" + process.getId() + "]没有开始节点");

        final Execution current = execute(process, execution.getOperator(), execution.getArgs(),
                execution.getParentOrder().getId(), execution.getParentNodeName());
        start.execute(current);
        return current.getOrder();
    }

    /**
     * 创建流程实例，并返回执行对象
     * @param process 流程定义
     * @param operator 操作人
     * @param args 参数列表
     * @param parentId 父流程实例id
     * @param parentNodeName 启动子流程的父流程节点名称
     * @return Execution
     */
    private Execution execute(final Process process, final String operator, final Map<String, Object> args,
            final String parentId, final String parentNodeName) {
        final Order order = order().createOrder(process, operator, args, parentId, parentNodeName);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + order);
        }
        final Execution current = new Execution(this, process, order, args);
        current.setOperator(operator);
        return current;
    }

    /**
     * 根据任务主键ID执行任务
     */
    @Override
    public List<Task> executeTask(final String taskId) {
        return executeTask(taskId, null);
    }

    /**
     * 根据任务主键ID，操作人ID执行任务
     */
    @Override
    public List<Task> executeTask(final String taskId, final String operator) {
        return executeTask(taskId, operator, null);
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务
     */
    @Override
    public List<Task> executeTask(final String taskId, final String operator, final Map<String, Object> args) {
        // 完成任务，并且构造执行对象
        final Execution execution = execute(taskId, operator, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        final ProcessModel model = execution.getProcess().getModel();
        if (model != null) {
            final NodeModel nodeModel = model.getNode(execution.getTask().getTaskName());
            // 将执行对象交给该任务对应的节点模型执行
            nodeModel.execute(execution);
        }
        return execution.getTasks();
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * 1、nodeName为null时，则驳回至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     */
    @Override
    public List<Task> executeAndJumpTask(final String taskId, final String operator, final Map<String, Object> args,
            final String nodeName) {
        final Execution execution = execute(taskId, operator, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        final ProcessModel model = execution.getProcess().getModel();
        AssertHelper.notNull(model, "当前任务未找到流程定义模型");
        if (StringHelper.isEmpty(nodeName)) {
            final Task newTask = task().rejectTask(model, execution.getTask());
            execution.addTask(newTask);
        } else {
            final NodeModel nodeModel = model.getNode(nodeName);
            AssertHelper.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");
            // 动态创建转移对象，由转移对象执行execution实例
            final TransitionModel tm = new TransitionModel();
            tm.setTarget(nodeModel);
            tm.setEnabled(true);
            tm.execute(execution);
        }

        return execution.getTasks();
    }

    /**
     * 根据流程实例ID，操作人ID，参数列表按照节点模型model创建新的自由任务
     */
    @Override
    public List<Task> createFreeTask(final String orderId, final String operator, final Map<String, Object> args,
            final TaskModel model) {
        final Order order = query().getOrder(orderId);
        AssertHelper.notNull(order, "指定的流程实例[id=" + orderId + "]已完成或不存在");
        order.setLastUpdator(operator);
        order.setLastUpdateTime(DateHelper.getTime());
        final Process process = process().getProcessById(order.getProcessId());
        final Execution execution = new Execution(this, process, order, args);
        execution.setOperator(operator);
        return task().createTask(model, execution);
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表完成任务，并且构造执行对象
     * @param taskId 任务id
     * @param operator 操作人
     * @param args 参数列表
     * @return Execution
     */
    protected Execution execute(final String taskId, final String operator, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<String, Object>();
        }
        final Task task = task().complete(taskId, operator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        final Order order = query().getOrder(task.getOrderId());
        AssertHelper.notNull(order, "指定的流程实例[id=" + task.getOrderId() + "]已完成或不存在");
        order.setLastUpdator(operator);
        order.setLastUpdateTime(DateHelper.getTime());
        order().updateOrder(order);
        // 协办任务完成不产生执行对象
        if (!task.isMajor()) {
            return null;
        }
        final Map<String, Object> orderMaps = order.getVariableMap();
        if (orderMaps != null) {
            for (final Map.Entry<String, Object> entry : orderMaps.entrySet()) {
                if (args.containsKey(entry.getKey())) {
                    continue;
                }
                args.put(entry.getKey(), entry.getValue());
            }
        }
        final Process process = process().getProcessById(order.getProcessId());
        final Execution execution = new Execution(this, process, order, args);
        execution.setOperator(operator);
        execution.setTask(task);
        return execution;
    }

    public void setProcessService(final IProcessService processService) {
        this.processService = processService;
    }

    public void setOrderService(final IOrderService orderService) {
        this.orderService = orderService;
    }

    public void setTaskService(final ITaskService taskService) {
        this.taskService = taskService;
    }

    public void setQueryService(final IQueryService queryService) {
        this.queryService = queryService;
    }

    public void setManagerService(final IManagerService managerService) {
        this.managerService = managerService;
    }
}
