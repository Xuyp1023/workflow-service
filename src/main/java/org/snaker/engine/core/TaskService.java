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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snaker.engine.Assignment;
import org.snaker.engine.AssignmentHandler;
import org.snaker.engine.Completion;
import org.snaker.engine.ITaskService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.SnakerException;
import org.snaker.engine.TaskAccessStrategy;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.impl.GeneralAccessStrategy;
import org.snaker.engine.model.CustomModel;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TaskModel.PerformType;
import org.snaker.engine.model.TaskModel.TaskType;

/**
 * 任务执行业务类
 * @author yuqs
 * @since 1.0
 */
public class TaskService extends AccessService implements ITaskService {
    private static final String START = "start";

    //访问策略接口
    private TaskAccessStrategy strategy = null;
    /**
     * 完成指定任务
     */
    @Override
    public Task complete(final String taskId) {
        return complete(taskId, null, null);
    }

    /**
     * 完成指定任务
     */
    @Override
    public Task complete(final String taskId, final String operator) {
        return complete(taskId, operator, null);
    }

    /**
     * 完成指定任务
     * 该方法仅仅结束活动任务，并不能驱动流程继续执行
     * @see SnakerEngineImpl#executeTask(String, String, java.util.Map)
     */
    @Override
    public Task complete(final String taskId, final String operator, final Map<String, Object> args) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        task.setVariable(JsonHelper.toJson(args));
        if(!isAllowed(task, operator)) {
            throw new SnakerException("当前参与者[" + operator + "]不允许执行任务[taskId=" + taskId + "]");
        }
        final HistoryTask history = new HistoryTask(task);
        history.setFinishTime(DateHelper.getTime());
        history.setTaskState(STATE_FINISH);
        history.setOperator(operator);
        if(history.getActorIds() == null) {
            final List<TaskActor> actors = access().getTaskActorsByTaskId(task.getId());
            final String[] actorIds = new String[actors.size()];
            for(int i = 0; i < actors.size(); i++) {
                actorIds[i] = actors.get(i).getActorId();
            }
            history.setActorIds(actorIds);
        }
        access().saveHistory(history);
        access().deleteTask(task);
        final Completion completion = getCompletion();
        if(completion != null) {
            completion.complete(history);
        }
        return task;
    }

    /**
     * 更新任务对象的finish_Time、operator、expire_Time、version、variable
     * @param task 任务对象
     */
    @Override
    public void updateTask(final Task task) {
        access().updateTask(task);
    }

    /**
     * 任务历史记录方法
     * @param execution 执行对象
     * @param model 自定义节点模型
     * @return 历史任务对象
     */
    @Override
    public HistoryTask history(final Execution execution, final CustomModel model) {
        final HistoryTask historyTask = new HistoryTask();
        historyTask.setId(StringHelper.getPrimaryKey());
        historyTask.setOrderId(execution.getOrder().getId());
        final String currentTime = DateHelper.getTime();
        historyTask.setCreateTime(currentTime);
        historyTask.setFinishTime(currentTime);
        historyTask.setDisplayName(model.getDisplayName());
        historyTask.setTaskName(model.getName());
        historyTask.setTaskState(STATE_FINISH);
        historyTask.setTaskType(TaskType.Record.ordinal());
        historyTask.setParentTaskId(execution.getTask() == null ?
                START : execution.getTask().getId());
        historyTask.setVariable(JsonHelper.toJson(execution.getArgs()));
        access().saveHistory(historyTask);
        return historyTask;
    }

    /**
     * 提取指定任务，设置完成时间及操作人，状态不改变
     */
    @Override
    public Task take(final String taskId, final String operator) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if(!isAllowed(task, operator)) {
            throw new SnakerException("当前参与者[" + operator + "]不允许提取任务[taskId=" + taskId + "]");
        }
        task.setOperator(operator);
        task.setFinishTime(DateHelper.getTime());
        access().updateTask(task);
        return task;
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public Task resume(final String taskId, final String operator) {
        final HistoryTask histTask = access().getHistTask(taskId);
        AssertHelper.notNull(histTask, "指定的历史任务[id=" + taskId + "]不存在");
        boolean isAllowed = true;
        if(StringHelper.isNotEmpty(histTask.getOperator())) {
            isAllowed = histTask.getOperator().equals(operator);
        }
        if(isAllowed) {
            final Task task = histTask.undoTask();
            task.setId(StringHelper.getPrimaryKey());
            task.setCreateTime(DateHelper.getTime());
            access().saveTask(task);
            assignTask(task.getId(), task.getOperator());
            return task;
        } else {
            throw new SnakerException("当前参与者[" + operator + "]不允许唤醒历史任务[taskId=" + taskId + "]");
        }
    }

    /**
     * 向指定任务添加参与者
     */
    @Override
    public void addTaskActor(final String taskId, final String... actors) {
        addTaskActor(taskId, null, actors);
    }

    /**
     * 向指定任务添加参与者
     * 该方法根据performType类型判断是否需要创建新的活动任务
     */
    @Override
    public void addTaskActor(final String taskId, Integer performType, final String... actors) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if(!task.isMajor()) {
            return;
        }
        if(performType == null) {
            performType = task.getPerformType();
        }
        if(performType == null) {
            performType = 0;
        }
        switch(performType) {
        case 0:
            assignTask(task.getId(), actors);
            final Map<String, Object> data = task.getVariableMap();
            final String oldActor = (String)data.get(Task.KEY_ACTOR);
            data.put(Task.KEY_ACTOR, oldActor + "," + StringHelper.getStringByArray(actors));
            task.setVariable(JsonHelper.toJson(data));
            access().updateTask(task);
            break;
        case 1:
            try {
                for(final String actor : actors) {
                    final Task newTask = (Task)task.clone();
                    newTask.setId(StringHelper.getPrimaryKey());
                    newTask.setCreateTime(DateHelper.getTime());
                    newTask.setOperator(actor);
                    final Map<String, Object> taskData = task.getVariableMap();
                    taskData.put(Task.KEY_ACTOR, actor);
                    task.setVariable(JsonHelper.toJson(taskData));
                    access().saveTask(newTask);
                    assignTask(newTask.getId(), actor);
                }
            } catch(final CloneNotSupportedException ex) {
                throw new SnakerException("任务对象不支持复制", ex.getCause());
            }
            break;
        default :
            break;
        }
    }

    /**
     * 向指定任务移除参与者
     */
    @Override
    public void removeTaskActor(final String taskId, final String... actors) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if(actors == null || actors.length == 0) {
            return;
        }
        if(task.isMajor()) {
            access().removeTaskActor(task.getId(), actors);
            final Map<String, Object> taskData = task.getVariableMap();
            final String actorStr = (String)taskData.get(Task.KEY_ACTOR);
            if(StringHelper.isNotEmpty(actorStr)) {
                final String[] actorArray = actorStr.split(",");
                final StringBuilder newActor = new StringBuilder(actorStr.length());
                boolean isMatch;
                for(final String actor : actorArray) {
                    isMatch = false;
                    if(StringHelper.isEmpty(actor)) {
                        continue;
                    }
                    for(final String removeActor : actors) {
                        if(actor.equals(removeActor)) {
                            isMatch = true;
                            break;
                        }
                    }
                    if(isMatch) {
                        continue;
                    }
                    newActor.append(actor).append(",");
                }
                newActor.deleteCharAt(newActor.length() - 1);
                taskData.put(Task.KEY_ACTOR, newActor.toString());
                task.setVariable(JsonHelper.toJson(taskData));
                access().updateTask(task);
            }
        }
    }

    /**
     * 撤回指定的任务
     */
    @Override
    public Task withdrawTask(final String taskId, final String operator) {
        final HistoryTask hist = access().getHistTask(taskId);
        AssertHelper.notNull(hist, "指定的历史任务[id=" + taskId + "]不存在");
        List<Task> tasks;
        if(hist.isPerformAny()) {
            tasks = access().getNextActiveTasks(hist.getId());
        } else {
            tasks = access().getNextActiveTasks(hist.getOrderId(),
                    hist.getTaskName(), hist.getParentTaskId());
        }
        if(tasks == null || tasks.isEmpty()) {
            throw new SnakerException("后续活动任务已完成或不存在，无法撤回.");
        }
        for(final Task task : tasks) {
            access().deleteTask(task);
        }

        final Task task = hist.undoTask();
        task.setId(StringHelper.getPrimaryKey());
        task.setCreateTime(DateHelper.getTime());
        access().saveTask(task);
        assignTask(task.getId(), task.getOperator());
        return task;
    }

    /**
     * 驳回任务
     */
    @Override
    public Task rejectTask(final ProcessModel model, final Task currentTask) {
        final String parentTaskId = currentTask.getParentTaskId();
        if(StringHelper.isEmpty(parentTaskId) || parentTaskId.equals(START)) {
            throw new SnakerException("上一步任务ID为空，无法驳回至上一步处理");
        }
        final NodeModel current = model.getNode(currentTask.getTaskName());
        final HistoryTask history = access().getHistTask(parentTaskId);
        final NodeModel parent = model.getNode(history.getTaskName());
        if(!NodeModel.canRejected(current, parent)) {
            throw new SnakerException("无法驳回至上一步处理，请确认上一步骤并非fork、join、subrocess以及会签任务");
        }

        final Task task = history.undoTask();
        task.setId(StringHelper.getPrimaryKey());
        task.setCreateTime(DateHelper.getTime());
        task.setOperator(history.getOperator());
        access().saveTask(task);
        assignTask(task.getId(), task.getOperator());
        return task;
    }

    /**
     * 对指定的任务分配参与者。参与者可以为用户、部门、角色
     * @param taskId 任务id
     * @param actorIds 参与者id集合
     */
    private void assignTask(final String taskId, final String... actorIds) {
        if(actorIds == null || actorIds.length == 0) {
            return;
        }
        for(final String actorId : actorIds) {
            //修复当actorId为null的bug
            if(StringHelper.isEmpty(actorId)) {
                continue;
            }
            final TaskActor taskActor = new TaskActor();
            taskActor.setTaskId(taskId);
            taskActor.setActorId(actorId);
            access().saveTaskActor(taskActor);
        }
    }

    /**
     * 根据已有任务、任务类型、参与者创建新的任务
     * 适用于转派，动态协办处理
     */
    @Override
    public List<Task> createNewTask(final String taskId, final int taskType, final String... actors) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        final List<Task> tasks = new ArrayList<Task>();
        try {
            final Task newTask = (Task)task.clone();
            newTask.setTaskType(taskType);
            newTask.setCreateTime(DateHelper.getTime());
            newTask.setParentTaskId(taskId);
            tasks.add(saveTask(newTask, actors));
        } catch (final CloneNotSupportedException e) {
            throw new SnakerException("任务对象不支持复制", e.getCause());
        }
        return tasks;
    }

    /**
     * 获取任务模型
     * @param taskId 任务id
     * @return TaskModel
     */
    @Override
    public TaskModel getTaskModel(final String taskId) {
        final Task task = access().getTask(taskId);
        AssertHelper.notNull(task);
        final Order order = access().getOrder(task.getOrderId());
        AssertHelper.notNull(order);
        final Process process = ServiceContext.getEngine().process().getProcessById(order.getProcessId());
        final ProcessModel model = process.getModel();
        final NodeModel nodeModel = model.getNode(task.getTaskName());
        AssertHelper.notNull(nodeModel, "任务id无法找到节点模型.");
        if(nodeModel instanceof TaskModel) {
            return (TaskModel)nodeModel;
        } else {
            throw new IllegalArgumentException("任务id找到的节点模型不匹配");
        }
    }

    /**
     * 获取任务模型
     * @param taskId 任务id
     * @return TaskModel
     */
    @Override
    public TaskModel getHistoryTaskModel(final String taskId) {
        final HistoryTask task = access().getHistTask(taskId);
        AssertHelper.notNull(task);
        final HistoryOrder order = access().getHistOrder(task.getOrderId());
        AssertHelper.notNull(order);
        final Process process = ServiceContext.getEngine().process().getProcessById(order.getProcessId());
        final ProcessModel model = process.getModel();
        final NodeModel nodeModel = model.getNode(task.getTaskName());
        AssertHelper.notNull(nodeModel, "任务id无法找到节点模型.");
        if(nodeModel instanceof TaskModel) {
            return (TaskModel)nodeModel;
        } else {
            throw new IllegalArgumentException("任务id找到的节点模型不匹配");
        }
    }

    /**
     * 由DBAccess实现类创建task，并根据model类型决定是否分配参与者
     * @param taskModel 模型
     * @param execution 执行对象
     * @return List<Task> 任务列表
     */
    @Override
    public List<Task> createTask(final TaskModel taskModel, final Execution execution) {
        final List<Task> tasks = new ArrayList<Task>();

        Map<String, Object> args = execution.getArgs();
        if(args == null) {
            args = new HashMap<String, Object>();
        }
        final Date expireDate = DateHelper.processTime(args, taskModel.getExpireTime());
        final Date remindDate = DateHelper.processTime(args, taskModel.getReminderTime());
        final String form = (String)args.get(taskModel.getForm());
        final String actionUrl = StringHelper.isEmpty(form) ? taskModel.getForm() : form;

        final String[] actors = getTaskActors(taskModel, execution);
        args.put(Task.KEY_ACTOR, StringHelper.getStringByArray(actors));
        Task task = createTaskBase(taskModel, execution);
        task.setActionUrl(actionUrl);
        task.setExpireDate(expireDate);
        task.setExpireTime(DateHelper.parseTime(expireDate));
        task.setVariable(JsonHelper.toJson(args));

        if(taskModel.isPerformAny()) {
            //任务执行方式为参与者中任何一个执行即可驱动流程继续流转，该方法只产生一个task
            task = saveTask(task, actors);
            task.setRemindDate(remindDate);
            tasks.add(task);
        } else if(taskModel.isPerformAll()){
            //任务执行方式为参与者中每个都要执行完才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
            for(final String actor : actors) {
                Task singleTask;
                try {
                    singleTask = (Task) task.clone();
                } catch (final CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = saveTask(singleTask, actor);
                singleTask.setRemindDate(remindDate);
                tasks.add(singleTask);
            }
        }
        return tasks;
    }

    /**
     * 根据模型、执行对象、任务类型构建基本的task对象
     * @param model 模型
     * @param execution 执行对象
     * @return Task任务对象
     */
    private Task createTaskBase(final TaskModel model, final Execution execution) {
        final Task task = new Task();
        task.setOrderId(execution.getOrder().getId());
        task.setTaskName(model.getName());
        task.setDisplayName(model.getDisplayName());
        task.setCreateTime(DateHelper.getTime());
        if(model.isMajor()) {
            task.setTaskType(TaskType.Major.ordinal());
        } else {
            task.setTaskType(TaskType.Aidant.ordinal());
        }
        task.setParentTaskId(execution.getTask() == null ?
                START : execution.getTask().getId());
        task.setModel(model);
        return task;
    }

    /**
     * 由DBAccess实现类持久化task对象
     */
    private Task saveTask(final Task task, final String... actors) {
        task.setId(StringHelper.getPrimaryKey());
        task.setPerformType(PerformType.ANY.ordinal());
        access().saveTask(task);
        assignTask(task.getId(), actors);
        task.setActorIds(actors);
        return task;
    }

    /**
     * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
     * @param model 模型
     * @param execution 执行对象
     * @return 参与者数组
     */
    private String[] getTaskActors(final TaskModel model, final Execution execution) {
        Object assigneeObject = null;
        final AssignmentHandler handler = model.getAssignmentHandlerObject();
        if(StringHelper.isNotEmpty(model.getAssignee())) {
            assigneeObject = execution.getArgs().get(model.getAssignee());
        } else if(handler != null) {
            if(handler instanceof Assignment) {
                assigneeObject = ((Assignment)handler).assign(model, execution);
            } else {
                assigneeObject = handler.assign(execution);
            }
        }
        return getTaskActors(assigneeObject == null ? model.getAssignee() : assigneeObject);
    }

    /**
     * 根据taskmodel指定的assignee属性，从args中取值
     * 将取到的值处理为String[]类型。
     * @param actors 参与者对象
     * @return 参与者数组
     */
    private String[] getTaskActors(final Object actors) {
        if(actors == null) {
            return null;
        }
        String[] results;
        if(actors instanceof String) {
            //如果值为字符串类型，则使用逗号,分隔
            return ((String)actors).split(",");
        } else if(actors instanceof List){
            //jackson会把stirng[]转成arraylist，此处增加arraylist的逻辑判断,by 红豆冰沙2014.11.21
            final List<?> list = (List)actors;
            results = new String[list.size()];
            for(int i = 0; i < list.size(); i++) {
                results[i] = (String)list.get(i);
            }
            return results;
        } else if(actors instanceof Long) {
            //如果为Long类型，则返回1个元素的String[]
            results = new String[1];
            results[0] = String.valueOf(actors);
            return results;
        } else if(actors instanceof Integer) {
            //如果为Integer类型，则返回1个元素的String[]
            results = new String[1];
            results[0] = String.valueOf(actors);
            return results;
        } else if(actors instanceof String[]) {
            //如果为String[]类型，则直接返回
            return (String[])actors;
        } else {
            //其它类型，抛出不支持的类型异常
            throw new SnakerException("任务参与者对象[" + actors + "]类型不支持."
                    + "合法参数示例:Long,Integer,new String[]{},'10000,20000',List<String>");
        }
    }

    /**
     * 判断当前操作人operator是否允许执行taskId指定的任务
     */
    @Override
    public boolean isAllowed(final Task task, final String operator) {
        if(StringHelper.isNotEmpty(operator)) {
            if(SnakerEngine.ADMIN.equalsIgnoreCase(operator)
                    || SnakerEngine.AUTO.equalsIgnoreCase(operator)) {
                return true;
            }
            if(StringHelper.isNotEmpty(task.getOperator())) {
                return operator.equals(task.getOperator());
            }
        }
        final List<TaskActor> actors = access().getTaskActorsByTaskId(task.getId());
        if(actors == null || actors.isEmpty()) {
            return true;
        }
        return !StringHelper.isEmpty(operator)
                && getStrategy().isAllowed(operator, actors);
    }

    public void setStrategy(final TaskAccessStrategy strategy) {
        this.strategy = strategy;
    }

    public TaskAccessStrategy getStrategy() {
        if(strategy != null) {
            return strategy;
        }
        strategy = ServiceContext.find(TaskAccessStrategy.class);
        if(strategy == null) {
            ServiceContext.put(TaskAccessStrategy.class.getName(), GeneralAccessStrategy.class);
            strategy = ServiceContext.find(TaskAccessStrategy.class);
        }
        return strategy;
    }
}
