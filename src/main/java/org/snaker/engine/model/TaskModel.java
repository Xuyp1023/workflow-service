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
package org.snaker.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.snaker.engine.AssignmentHandler;
import org.snaker.engine.core.Execution;
import org.snaker.engine.handlers.impl.MergeActorHandler;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.ClassHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.scheduling.JobCallback;

import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowNode;
import com.betterjr.modules.workflow.entity.WorkFlowStep;

/**
 * 任务定义task元素
 *
 * @author yuqs
 * @since 1.0
 */
public class TaskModel extends WorkModel {
    /**
     *
     */
    private static final long serialVersionUID = 1775545243233990922L;
    /**
     * 类型：普通任务
     */
    public static final String PERFORMTYPE_ANY = "ANY";
    /**
     * 类型：参与者fork任务
     */
    public static final String PERFORMTYPE_ALL = "ALL";
    /**
     * 类型：主办任务
     */
    public static final String TASKTYPE_MAJOR = "Major";
    /**
     * 类型：协办任务
     */
    public static final String TASKTYPE_AIDANT = "Aidant";

    /**
     * 与 流程定义绑定
     */
    private WorkFlowBase workFlowBase;
    private WorkFlowNode workFlowNode;
    private WorkFlowStep workFlowStep;

    public WorkFlowBase getWorkFlowBase() {
        return workFlowBase;
    }

    public void setWorkFlowBase(final WorkFlowBase anWorkFlowBase) {
        workFlowBase = anWorkFlowBase;
    }

    public WorkFlowNode getWorkFlowNode() {
        return workFlowNode;
    }

    public void setWorkFlowNode(final WorkFlowNode anWorkFlowNode) {
        workFlowNode = anWorkFlowNode;
    }

    public WorkFlowStep getWorkFlowStep() {
        return workFlowStep;
    }

    public void setWorkFlowStep(final WorkFlowStep anWorkFlowStep) {
        workFlowStep = anWorkFlowStep;
    }

    /**
     * 参与类型
     */
    public enum PerformType {
        ANY, ALL;
    }

    /**
     * 任务类型(Major:主办的,Aidant:协助的,Record:仅仅作为记录的)
     */
    public enum TaskType {
        Major, Aidant, Record;
    }

    /**
     * 参与者变量名称
     */
    private String assignee;
    /**
     * 参与方式 any：任何一个参与者处理完即执行下一步 all：所有参与者都完成，才可执行下一步
     */
    private String performType = PERFORMTYPE_ANY;
    /**
     * 任务类型 major：主办任务 aidant：协办任务
     */
    private String taskType = TASKTYPE_MAJOR;
    /**
     * 期望完成时间
     */
    private String expireTime;
    /**
     * 提醒时间
     */
    private String reminderTime;
    /**
     * 提醒间隔(分钟)
     */
    private String reminderRepeat;
    /**
     * 是否自动执行
     */
    private String autoExecute;
    /**
     * 任务执行后回调类
     */
    private String callback;
    /**
     * 分配参与者处理类型
     */
    private String assignmentHandler;
    /**
     * 任务执行后回调对象
     */
    private JobCallback callbackObject;
    /**
     * 分配参与者处理对象
     */
    private AssignmentHandler assignmentHandlerObject;
    /**
     * 拥有权重
     */
    private boolean hasWeight;
    /**
     * 权重值
     */
    private Integer weight;
    /**
     * 字段模型集合
     */
    private List<FieldModel> fields = null;

    @Override
    protected void exec(final Execution execution) {
        if (performType == null || performType.equalsIgnoreCase(PERFORMTYPE_ANY)) {
            /**
             * any方式，直接执行输出变迁
             */
            runOutTransition(execution);
        }
        else {
            /**
             * all方式，需要判断是否已全部合并 由于all方式分配任务，是每人一个任务 那么此时需要判断之前分配的所有任务都执行完成后，才可执行下一步，否则不处理
             */
            fire(new MergeActorHandler(getName()), execution);
            if (execution.isMerged()) {
                runOutTransition(execution);
            }
        }
    }

    public boolean isPerformAny() {
        return PERFORMTYPE_ANY.equalsIgnoreCase(this.performType);
    }

    public boolean isPerformAll() {
        return PERFORMTYPE_ALL.equalsIgnoreCase(this.performType);
    }

    public boolean isMajor() {
        return TASKTYPE_MAJOR.equalsIgnoreCase(this.taskType);
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(final String expireTime) {
        this.expireTime = expireTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(final String taskType) {
        this.taskType = (StringHelper.isEmpty(taskType) ? TASKTYPE_MAJOR : taskType);
    }

    public String getPerformType() {
        return performType;
    }

    public void setPerformType(final String performType) {
        this.performType = (StringHelper.isEmpty(performType) ? PERFORMTYPE_ANY : performType);
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(final String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getReminderRepeat() {
        return reminderRepeat;
    }

    public void setReminderRepeat(final String reminderRepeat) {
        this.reminderRepeat = reminderRepeat;
    }

    public String getAutoExecute() {
        return autoExecute;
    }

    public void setAutoExecute(final String autoExecute) {
        this.autoExecute = autoExecute;
    }

    public AssignmentHandler getAssignmentHandlerObject() {
        return assignmentHandlerObject;
    }

    public void setAssignmentHandler(final String assignmentHandlerStr) {
        if (StringHelper.isNotEmpty(assignmentHandlerStr)) {
            this.assignmentHandler = assignmentHandlerStr;
            assignmentHandlerObject = (AssignmentHandler) ClassHelper.newInstance(assignmentHandlerStr);
            AssertHelper.notNull(assignmentHandlerObject, "分配参与者处理类实例化失败");
        }
    }

    public String getAssignmentHandler() {
        return assignmentHandler;
    }

    public String getCallback() {
        return callback;
    }

    public JobCallback getCallbackObject() {
        return callbackObject;
    }

    public void setCallback(final String callbackStr) {
        if (StringHelper.isNotEmpty(callbackStr)) {
            this.callback = callbackStr;
            callbackObject = (JobCallback) ClassHelper.newInstance(callbackStr);
            AssertHelper.notNull(callbackObject, "回调处理类实例化失败");
        }
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public void setFields(final List<FieldModel> fields) {
        this.fields = fields;
    }

    public boolean isHasWeight() {
        return hasWeight;
    }

    public void setHasWeight(final boolean anHasWeight) {
        hasWeight = anHasWeight;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(final Integer anWeight) {
        weight = anWeight;
    }

    /**
     * 获取后续任务模型集合（方便预处理）
     *
     * @return 模型集合
     * @deprecated
     */
    @Deprecated
    public List<TaskModel> getNextTaskModels() {
        final List<TaskModel> models = new ArrayList<TaskModel>();
        for (final TransitionModel tm : this.getOutputs()) {
            addNextModels(models, tm, TaskModel.class);
        }
        return models;
    }
}
