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
package org.snaker.engine.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TaskModel.TaskType;

/**
 * 任务实体类
 * @author yuqs
 * @since 1.0
 */
public class Task implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -189094546633914087L;
    public static final String KEY_ACTOR = "S-ACTOR";
    /**
     * 主键ID
     */
    private String id;
    /**
     * 版本
     */
    private Integer version = 0;
    /**
     * 流程实例ID
     */
    private String orderId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务显示名称
     */
    private String displayName;
    /**
     * 参与方式（0：普通任务；1：参与者会签任务）
     */
    private Integer performType;
    /**
     * 任务类型（0：主办任务；1：协办任务）
     */
    private Integer taskType;
    /**
     * 任务处理者ID
     */
    private String operator;
    /**
     * 任务创建时间
     */
    private String createTime;
    /**
     * 任务完成时间
     */
    private String finishTime;
    /**
     * 期望任务完成时间
     */
    private String expireTime;
    /**
     * 期望的完成时间date类型
     */
    private Date expireDate;
    /**
     * 提醒时间date类型
     */
    private Date remindDate;
    /**
     * 任务关联的表单url
     */
    private String actionUrl;
    /**
     * 任务参与者列表
     */
    private String[] actorIds;
    /**
     * 父任务Id
     */
    private String parentTaskId;
    /**
     * 任务附属变量
     */
    private String variable;
    /**
     * 保持模型对象
     */
    private TaskModel model;

    public Task() {

    }

    public Task(final String id) {
        this.id = id;
    }

    public boolean isMajor() {
        return this.taskType == TaskType.Major.ordinal();
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(final String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(final String variable) {
        this.variable = variable;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(final Integer taskType) {
        this.taskType = taskType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final String createTime) {
        this.createTime = createTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(final String finishTime) {
        this.finishTime = finishTime;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(final String expireTime) {
        this.expireTime = expireTime;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(final String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    public String[] getActorIds() {
        if(actorIds == null) {
            final String actorStr = (String)getVariableMap().get(KEY_ACTOR);
            if(actorStr != null) {
                actorIds = actorStr.split(",");
            }
        }
        return actorIds;
    }

    public void setActorIds(final String[] actorIds) {
        this.actorIds = actorIds;
    }

    public Integer getPerformType() {
        return performType;
    }

    public void setPerformType(final Integer performType) {
        this.performType = performType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(final Date expireDate) {
        this.expireDate = expireDate;
    }

    public Date getRemindDate() {
        return remindDate;
    }

    public void setRemindDate(final Date remindDate) {
        this.remindDate = remindDate;
    }

    public TaskModel getModel() {
        return model;
    }

    public void setModel(final TaskModel model) {
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getVariableMap() {
        final Map<String, Object> map = JsonHelper.fromJson(this.variable, Map.class);
        if(map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Task(id=").append(this.id);
        sb.append(",orderId=").append(this.orderId);
        sb.append(",taskName=").append(this.taskName);
        sb.append(",displayName=").append(this.displayName);
        sb.append(",taskType=").append(this.taskType);
        sb.append(",createTime=").append(this.createTime);
        sb.append(",performType=").append(this.performType).append(")");
        return sb.toString();
    }
}
