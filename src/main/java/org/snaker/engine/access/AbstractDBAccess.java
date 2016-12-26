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
package org.snaker.engine.access;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.DBAccess;
import org.snaker.engine.SnakerException;
import org.snaker.engine.access.dialect.Dialect;
import org.snaker.engine.access.jdbc.JdbcHelper;
import org.snaker.engine.core.ServiceContext;
import org.snaker.engine.entity.CCOrder;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.HistoryTaskActor;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Surrogate;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.helper.ClassHelper;
import org.snaker.engine.helper.ConfigHelper;
import org.snaker.engine.helper.StringHelper;

/**
 * 抽象数据库访问类
 * 封装SQL语句的构造
 * @author yuqs
 * @since 1.0
 */
public abstract class AbstractDBAccess implements DBAccess {
    private static final Logger log = LoggerFactory.getLogger(AbstractDBAccess.class);
    protected static final String KEY_SQL = "SQL";
    protected static final String KEY_ARGS = "ARGS";
    protected static final String KEY_TYPE = "TYPE";
    protected static final String KEY_ENTITY = "ENTITY";

    protected static final String KEY_SU = "SU";
    protected static final String SAVE = "SAVE";
    protected static final String UPDATE = "UPDATE";

    protected static final String PROCESS_INSERT = "insert into wf_process (id,name,display_Name,type,instance_Url,state,version,create_Time,creator,cust_No) values (?,?,?,?,?,?,?,?,?,?)";
    protected static final String PROCESS_UPDATE = "update wf_process set name=?, display_Name=?,state=?,instance_Url=?,create_Time=?,creator=? where id=? ";
    protected static final String PROCESS_DELETE = "delete from wf_process where id = ?";
    protected static final String PROCESS_UPDATE_BLOB = "update wf_process set content=? where id=?";
    protected static final String PROCESS_UPDATE_TYPE = "update wf_process set type=? where id=?";

    protected static final String ORDER_INSERT = "insert into wf_order (id,process_Id,creator,create_Time,parent_Id,parent_Node_Name,expire_Time,last_Update_Time,last_Updator,order_No,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String ORDER_UPDATE = "update wf_order set last_Updator=?, last_Update_Time=?, variable = ?, expire_Time=?, version = version + 1 where id=? and version = ?";
    protected static final String ORDER_DELETE = "delete from wf_order where id = ?";
    protected static final String ORDER_HISTORY_INSERT = "insert into wf_hist_order (id,process_Id,order_State,creator,create_Time,end_Time,parent_Id,expire_Time,order_No,variable) values (?,?,?,?,?,?,?,?,?,?)";
    protected static final String ORDER_HISTORY_UPDATE = "update wf_hist_order set order_State = ?, end_Time = ?, variable = ? where id = ? ";
    protected static final String ORDER_HISTORY_DELETE = "delete from wf_hist_order where id = ?";

    protected static final String CCORDER_INSERT = "insert into wf_cc_order (order_Id, actor_Id, creator, create_Time, status) values (?, ?, ?, ?, ?)";
    protected static final String CCORDER_UPDATE = "update wf_cc_order set status = ?, finish_Time = ? where order_Id = ? and actor_Id = ?";
    protected static final String CCORDER_DELETE = "delete from wf_cc_order where order_Id = ? and actor_Id = ?";

    protected static final String TASK_INSERT = "insert into wf_task (id,order_Id,task_Name,display_Name,task_Type,perform_Type,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable,version) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String TASK_UPDATE = "update wf_task set finish_Time=?, operator=?, variable=?, expire_Time=?, action_Url=?, version = version + 1 where id=? and version = ?";
    protected static final String TASK_DELETE = "delete from wf_task where id = ?";
    protected static final String TASK_HISTORY_INSERT = "insert into wf_hist_task (id,order_Id,task_Name,display_Name,task_Type,perform_Type,task_State,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    protected static final String TASK_HISTORY_DELETE = "delete from wf_hist_task where id = ?";

    protected static final String TASK_ACTOR_INSERT = "insert into wf_task_actor (task_Id, actor_Id) values (?, ?)";
    protected static final String TASK_ACTOR_DELETE = "delete from wf_task_actor where task_Id = ?";
    protected static final String TASK_ACTOR_REDUCE = "delete from wf_task_actor where task_Id = ? and actor_Id = ?";
    protected static final String TASK_ACTOR_HISTORY_INSERT = "insert into wf_hist_task_actor (task_Id, actor_Id) values (?, ?)";
    protected static final String TASK_ACTOR_HISTORY_DELETE = "delete from wf_hist_task_actor where task_Id = ?";

    protected static final String QUERY_VERSION = "select max(version) from wf_process ";
    protected static final String QUERY_PROCESS = "select id,name,display_Name,type,instance_Url,state, content, version,create_Time,creator,cust_No from wf_process ";
    protected static final String QUERY_ORDER = "select o.id,o.process_Id,o.creator,o.create_Time,o.parent_Id,o.parent_Node_Name,o.expire_Time,o.last_Update_Time,o.last_Updator,o.priority,o.order_No,o.variable, o.version from wf_order o ";
    protected static final String QUERY_TASK = "select id,order_Id,task_Name,display_Name,task_Type,perform_Type,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable, version from wf_task ";
    protected static final String QUERY_TASK_ACTOR = "select task_Id, actor_Id from wf_task_actor ";
    protected static final String QUERY_CCORDER = "select order_Id, actor_Id, creator, create_Time, finish_Time, status from wf_cc_order ";

    protected static final String QUERY_HIST_ORDER = "select o.id,o.process_Id,o.order_State,o.priority,o.creator,o.create_Time,o.end_Time,o.parent_Id,o.expire_Time,o.order_No,o.variable from wf_hist_order o ";
    protected static final String QUERY_HIST_TASK = "select id,order_Id,task_Name,display_Name,task_Type,perform_Type,task_State,operator,create_Time,finish_Time,expire_Time,action_Url,parent_Task_Id,variable from wf_hist_task ";
    protected static final String QUERY_HIST_TASK_ACTOR = "select task_Id, actor_Id from wf_hist_task_actor ";

    /**委托代理CRUD*/
    protected static final String SURROGATE_INSERT = "insert into wf_surrogate (id, process_Name, operator, surrogate, odate, sdate, edate, state) values (?,?,?,?,?,?,?,?)";
    protected static final String SURROGATE_UPDATE = "update wf_surrogate set process_Name=?, surrogate=?, odate=?, sdate=?, edate=?, state=? where id = ?";
    protected static final String SURROGATE_DELETE = "delete from wf_surrogate where id = ?";
    protected static final String SURROGATE_QUERY = "select id, process_Name, operator, surrogate, odate, sdate, edate, state from wf_surrogate";

    protected Dialect dialect;

    /**
     * 是否为ORM框架，用以标识对象直接持久化
     * @return boolean
     */
    public abstract boolean isORM();
    /**
     * 保存或更新对象
     * isORM为true，则参数map只存放对象
     * isORM为false，则参数map需要放入SQL、ARGS、TYPE
     * @param map 需要持久化的数据
     */
    public abstract void saveOrUpdate(Map<String, Object> map);

    @Override
    public void initialize(final Object accessObject) {

    }

    /**
     * isORM为false，需要构造map传递给实现类
     * @param sql 需要执行的sql语句
     * @param args sql语句中的参数列表
     * @param type sql语句中的参数类型
     * @return 构造的map
     */
    private Map<String, Object> buildMap(final String sql, final Object[] args, final int[] type) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_SQL, sql);
        map.put(KEY_ARGS, args);
        map.put(KEY_TYPE, type);
        return map;
    }

    /**
     * isORM为true，只存放对象传递给orm框架
     * @param entity 实体对象
     * @param su 保存或更新的标识
     * @return 构造的map
     */
    private Map<String, Object> buildMap(final Object entity, final String su) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(KEY_ENTITY, entity);
        map.put(KEY_SU, su);
        return map;
    }

    /**
     * 获取数据库方言
     * 根据数据库连接的DatabaseMetaData获取数据库厂商，自动适配具体的方言
     * 当数据库类型未提供支持时无法自动获取方言，建议通过配置完成
     * @return 方言对象
     */
    protected Dialect getDialect() {
        if(dialect != null) {
            return dialect;
        }
        dialect = ServiceContext.getContext().find(Dialect.class);
        if(dialect == null) {
            try {
                dialect = JdbcHelper.getDialect(getConnection());
            } catch (final Exception e) {
                log.error("Unable to find the available dialect.Please configure dialect to snaker.xml");
            }
        }
        return dialect;
    }

    /**
     * 由于process中涉及blob字段，未对各种框架统一，所以process操作交给具体的实现类处理
     */
    @Override
    public void saveProcess(final Process process) {
        if(isORM()) {
            saveOrUpdate(buildMap(process, SAVE));
        } else {
            final Object[] args = new Object[]{process.getId(), process.getName(), process.getDisplayName(), process.getType(),
                    process.getInstanceUrl(), process.getState(), process.getVersion(), process.getCreateTime(), process.getCreator(), process.getCustNo()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.BIGINT};
            saveOrUpdate(buildMap(PROCESS_INSERT, args, type));
        }
    }
    /**
     * 由于process中涉及blob字段，未对各种框架统一，所以process操作交给具体的实现类处理
     */
    @Override
    public void updateProcess(final Process process) {
        if(isORM()) {
            saveOrUpdate(buildMap(process, UPDATE));
        } else {
            final Object[] args = new Object[]{process.getName(), process.getDisplayName(), process.getState(),
                    process.getInstanceUrl(), process.getCreateTime(), process.getCreator(), process.getId()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_UPDATE, args, type));
        }
    }

    @Override
    public void deleteProcess(final Process process) {
        if(!isORM()) {
            final Object[] args = new Object[]{process.getId()};
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_DELETE, args, type));
        }
    }

    @Override
    public void updateProcessType(final String id, final String type) {
        if(isORM()) {
            final Process process = getProcess(id);
            process.setType(type);
            saveOrUpdate(buildMap(process, UPDATE));
        } else {
            final Object[] args = new Object[]{type, id};
            final int[] types = new int[]{Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(PROCESS_UPDATE_TYPE, args, types));
        }
    }

    @Override
    public void saveTask(final Task task) {
        if(isORM()) {
            saveOrUpdate(buildMap(task, SAVE));
        } else {
            final Object[] args = new Object[]{task.getId(), task.getOrderId(), task.getTaskName(), task.getDisplayName(), task.getTaskType(),
                    task.getPerformType(), task.getOperator(), task.getCreateTime(), task.getFinishTime(),
                    task.getExpireTime(), task.getActionUrl(), task.getParentTaskId(), task.getVariable(), task.getVersion()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(TASK_INSERT, args, type));
        }
    }

    @Override
    public void saveOrder(final Order order) {
        if(isORM()) {
            saveOrUpdate(buildMap(order, SAVE));
        } else {
            final Object[] args = new Object[]{order.getId(), order.getProcessId(), order.getCreator(), order.getCreateTime(), order.getParentId(),
                    order.getParentNodeName(), order.getExpireTime(), order.getLastUpdateTime(), order.getLastUpdator(), order.getOrderNo(),
                    order.getVariable(), order.getVersion()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(ORDER_INSERT, args, type));
        }
    }

    @Override
    public void saveCCOrder(final CCOrder ccorder) {
        if(isORM()) {
            saveOrUpdate(buildMap(ccorder, SAVE));
        } else {
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(CCORDER_INSERT, new Object[]{ccorder.getOrderId(), ccorder.getActorId(),
                    ccorder.getCreator(), ccorder.getCreateTime(), ccorder.getStatus()}, type));
        }
    }

    @Override
    public void saveTaskActor(final TaskActor taskActor) {
        if(isORM()) {
            saveOrUpdate(buildMap(taskActor, SAVE));
        } else {
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_INSERT, new Object[]{taskActor.getTaskId(), taskActor.getActorId() }, type));
        }
    }

    @Override
    public void updateTask(final Task task) {
        if(isORM()) {
            saveOrUpdate(buildMap(task, UPDATE));
        } else {
            final Object[] args = new Object[]{task.getFinishTime(), task.getOperator(), task.getVariable(), task.getExpireTime(), task.getActionUrl(), task.getId(), task.getVersion() };
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,  Types.VARCHAR, Types.VARCHAR,  Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(TASK_UPDATE, args, type));
        }
    }

    @Override
    public void updateOrder(final Order order) {
        if(isORM()) {
            saveOrUpdate(buildMap(order, UPDATE));
        } else {
            final Object[] args = new Object[]{order.getLastUpdator(), order.getLastUpdateTime(), order.getVariable(), order.getExpireTime(), order.getId(), order.getVersion() };
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(ORDER_UPDATE, args, type));
        }
    }

    @Override
    public void updateCCOrder(final CCOrder ccorder) {
        if(isORM()) {
            saveOrUpdate(buildMap(ccorder, UPDATE));
        } else {
            final Object[] args = new Object[]{ccorder.getStatus(), ccorder.getFinishTime(), ccorder.getOrderId(), ccorder.getActorId() };
            final int[] type = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(CCORDER_UPDATE, args, type));
        }
    }

    @Override
    public void deleteTask(final Task task) {
        if(!isORM()) {
            final Object[] args = new Object[]{task.getId()};
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_DELETE, args, type));
            saveOrUpdate(buildMap(TASK_DELETE, args, type));
        }
    }

    @Override
    public void deleteOrder(final Order order) {
        if(!isORM()) {
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(ORDER_DELETE, new Object[]{order.getId()}, type));
        }
    }

    @Override
    public void deleteCCOrder(final CCOrder ccorder) {
        if(!isORM()) {
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(CCORDER_DELETE, new Object[]{ccorder.getOrderId(), ccorder.getActorId()}, type));
        }
    }

    @Override
    public void removeTaskActor(final String taskId, final String... actors) {
        if(!isORM()) {
            for(final String actorId : actors) {
                final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
                saveOrUpdate(buildMap(TASK_ACTOR_REDUCE, new Object[]{taskId, actorId}, type));
            }
        }
    }

    @Override
    public void saveHistory(final HistoryOrder order) {
        if(isORM()) {
            saveOrUpdate(buildMap(order, SAVE));
        } else {
            final Object[] args = new Object[]{order.getId(), order.getProcessId(), order.getOrderState(), order.getCreator(),
                    order.getCreateTime(), order.getEndTime(), order.getParentId(), order.getExpireTime(), order.getOrderNo(), order.getVariable()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(ORDER_HISTORY_INSERT, args, type));
        }
    }

    @Override
    public void updateHistory(final HistoryOrder order) {
        if(isORM()) {
            saveOrUpdate(buildMap(order, UPDATE));
        } else {
            final Object[] args = new Object[]{order.getOrderState(), order.getEndTime(), order.getVariable(), order.getId()};
            final int[] type = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(ORDER_HISTORY_UPDATE, args, type));
        }
    }

    @Override
    public void deleteHistoryOrder(final HistoryOrder historyOrder) {
        if(!isORM()) {
            final Object[] args = new Object[]{historyOrder.getId()};
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(ORDER_HISTORY_DELETE, args, type));
        }
    }

    @Override
    public void saveHistory(final HistoryTask task) {
        if(isORM()) {
            saveOrUpdate(buildMap(task, SAVE));
            if(task.getActorIds() != null) {
                for(final String actorId : task.getActorIds()) {
                    if(StringHelper.isEmpty(actorId)) {
                        continue;
                    }
                    final HistoryTaskActor hist = new HistoryTaskActor();
                    hist.setActorId(actorId);
                    hist.setTaskId(task.getId());
                    saveOrUpdate(buildMap(hist, SAVE));
                }
            }
        } else {
            final Object[] args = new Object[]{task.getId(), task.getOrderId(), task.getTaskName(), task.getDisplayName(), task.getTaskType(),
                    task.getPerformType(), task.getTaskState(), task.getOperator(), task.getCreateTime(), task.getFinishTime(),
                    task.getExpireTime(), task.getActionUrl(), task.getParentTaskId(), task.getVariable()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
                    Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_HISTORY_INSERT, args, type));
            if(task.getActorIds() != null) {
                for(final String actorId : task.getActorIds()) {
                    if(StringHelper.isEmpty(actorId)) {
                        continue;
                    }
                    saveOrUpdate(buildMap(TASK_ACTOR_HISTORY_INSERT, new Object[]{task.getId(), actorId}, new int[]{Types.VARCHAR, Types.VARCHAR}));
                }
            }
        }
    }

    @Override
    public void deleteHistoryTask(final HistoryTask historyTask) {
        if(!isORM()) {
            final Object[] args = new Object[]{historyTask.getId()};
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(TASK_ACTOR_HISTORY_DELETE, args, type));
            saveOrUpdate(buildMap(TASK_HISTORY_DELETE, args, type));
        }
    }

    @Override
    public void updateOrderVariable(final Order order) {
        updateOrder(order);
        final HistoryOrder hist = getHistOrder(order.getId());
        hist.setVariable(order.getVariable());
        updateHistory(hist);
    }

    @Override
    public void saveSurrogate(final Surrogate surrogate) {
        if(isORM()) {
            saveOrUpdate(buildMap(surrogate, SAVE));
        } else {
            final Object[] args = new Object[]{surrogate.getId(), surrogate.getProcessName(), surrogate.getOperator(),
                    surrogate.getSurrogate(), surrogate.getOdate(), surrogate.getSdate(), surrogate.getEdate(),
                    surrogate.getState()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.INTEGER};
            saveOrUpdate(buildMap(SURROGATE_INSERT, args, type));
        }
    }

    @Override
    public void updateSurrogate(final Surrogate surrogate) {
        if(isORM()) {
            saveOrUpdate(buildMap(surrogate, UPDATE));
        } else {
            final Object[] args = new Object[]{surrogate.getProcessName(), surrogate.getSurrogate(), surrogate.getOdate(),
                    surrogate.getSdate(), surrogate.getEdate(), surrogate.getState(), surrogate.getId()};
            final int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,Types.VARCHAR, Types.VARCHAR,
                    Types.INTEGER, Types.VARCHAR};
            saveOrUpdate(buildMap(SURROGATE_UPDATE, args, type));
        }
    }

    @Override
    public void deleteSurrogate(final Surrogate surrogate) {
        if(!isORM()) {
            final Object[] args = new Object[]{surrogate.getId()};
            final int[] type = new int[]{Types.VARCHAR};
            saveOrUpdate(buildMap(SURROGATE_DELETE, args, type));
        }
    }

    @Override
    public Surrogate getSurrogate(final String id) {
        final String where = " where id = ?";
        return queryObject(Surrogate.class, SURROGATE_QUERY + where, id);
    }

    @Override
    public List<Surrogate> getSurrogate(final Page<Surrogate> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(SURROGATE_QUERY);
        sql.append(" where 1=1 and state = 1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and process_Name in(");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and operator in (");
            for(final String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getOperateTime())) {
            sql.append(" and sdate <= ? and edate >= ? ");
            paramList.add(filter.getOperateTime());
            paramList.add(filter.getOperateTime());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("sdate");
        }
        return queryList(page, filter, Surrogate.class, sql.toString(), paramList.toArray());
    }

    @Override
    public Task getTask(final String taskId) {
        final String where = " where id = ?";
        return queryObject(Task.class, QUERY_TASK + where, taskId);
    }

    @Override
    public List<Task> getNextActiveTasks(final String parentTaskId) {
        final String where = " where parent_Task_Id = ?";
        return queryList(Task.class, QUERY_TASK + where, parentTaskId);
    }

    @Override
    public List<Task> getNextActiveTasks(final String orderId, final String taskName, final String parentTaskId) {
        final String sql = QUERY_TASK + " where parent_task_id in ( select ht.id from wf_hist_task ht where ht.order_id=? and ht.task_name=? and ht.parent_task_id=? )";
        return queryList(Task.class, sql, orderId, taskName, parentTaskId);
    }

    @Override
    public HistoryTask getHistTask(final String taskId) {
        final String where = " where id = ?";
        return queryObject(HistoryTask.class, QUERY_HIST_TASK + where, taskId);
    }

    @Override
    public HistoryOrder getHistOrder(final String orderId) {
        final String where = " where id = ?";
        return queryObject(HistoryOrder.class, QUERY_HIST_ORDER + where, orderId);
    }

    @Override
    public List<TaskActor> getTaskActorsByTaskId(final String taskId) {
        final String where = " where task_Id = ?";
        return queryList(TaskActor.class, QUERY_TASK_ACTOR + where, taskId);
    }

    @Override
    public List<HistoryTaskActor> getHistTaskActorsByTaskId(final String taskId) {
        final String where = " where task_Id = ?";
        return queryList(HistoryTaskActor.class, QUERY_HIST_TASK_ACTOR + where, taskId);
    }

    @Override
    public Order getOrder(final String orderId) {
        final String where = " where id = ?";
        return queryObject(Order.class, QUERY_ORDER + where, orderId);
    }

    @Override
    public List<CCOrder> getCCOrder(final String orderId, final String... actorIds) {
        final StringBuilder where = new StringBuilder(QUERY_CCORDER);
        where.append(" where 1 = 1 ");

        if(StringHelper.isNotEmpty(orderId)) {
            where.append(" and order_Id = ?");
        }
        if(actorIds != null && actorIds.length > 0) {
            where.append(" and actor_Id in (");
            where.append(StringUtils.repeat("?,", actorIds.length));
            where.deleteCharAt(where.length() - 1);
            where.append(") ");
        }
        return queryList(CCOrder.class, where.toString(), ArrayUtils.add(actorIds, 0, orderId));
    }

    @Override
    public Process getProcess(final String id) {
        final String where = " where id = ?";
        return queryObject(Process.class, QUERY_PROCESS + where, id);
    }

    @Override
    public List<Process> getProcesss(final Page<Process> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(QUERY_PROCESS);
        sql.append(" where 1=1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and name in(");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if (filter.getCustNo() != null) {
            sql.append(" and cust_No = ? ");
            paramList.add(filter.getCustNo());
        }
        if(filter.getVersion() != null) {
            sql.append(" and version = ? ");
            paramList.add(filter.getVersion());
        }
        if(filter.getState() != null) {
            sql.append(" and state = ? ");
            paramList.add(filter.getState());
        }
        if(StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and display_Name like ? ");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if(StringHelper.isNotEmpty(filter.getProcessType())) {
            sql.append(" and type = ? ");
            paramList.add(filter.getProcessType());
        }
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and creator in(");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.ASC);
            filter.setOrderBy("name");
        }

        return queryList(page, filter, Process.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<Order> getActiveOrders(final Page<Order> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(QUERY_ORDER);
        sql.append(" left join wf_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and o.creator in(");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and p.name in(");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_Id = ? ");
            paramList.add(filter.getProcessId());
        }
        // XXX processId 和 processIds应2选1
        if (filter.getProcessIds() != null && filter.getProcessIds().length > 0) {
            sql.append(" and o.process_Id in(");
            for (int i = 0; i < filter.getProcessIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getProcessIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_Name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if(StringHelper.isNotEmpty(filter.getProcessType())) {
            sql.append(" and p.type = ? ");
            paramList.add(filter.getProcessType());
        }
        if(StringHelper.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_Id = ? ");
            paramList.add(filter.getParentId());
        }
        if(filter.getExcludedIds() != null && filter.getExcludedIds().length > 0) {
            sql.append(" and o.id not in(");
            for(int i = 0; i < filter.getExcludedIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getExcludedIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and o.create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and o.create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(StringHelper.isNotEmpty(filter.getOrderNo())) {
            sql.append(" and o.order_No = ? ");
            paramList.add(filter.getOrderNo());
        }

        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("o.create_Time");
        }
        return queryList(page, filter, Order.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<Task> getActiveTasks(final Page<Task> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(QUERY_TASK);
        final boolean isFetchActor = filter.getOperators() != null && filter.getOperators().length > 0;
        if(isFetchActor) {
            sql.append(" left join wf_task_actor ta on ta.task_id = id ");
        }
        sql.append(" where 1=1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(StringHelper.isNotEmpty(filter.getOrderId())) {
            sql.append(" and order_Id = ? ");
            paramList.add(filter.getOrderId());
        }
        if(filter.getExcludedIds() != null && filter.getExcludedIds().length > 0) {
            sql.append(" and id not in(");
            for(int i = 0; i < filter.getExcludedIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getExcludedIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(isFetchActor) {
            sql.append(" and ta.actor_Id in (");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and task_Name in (");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("create_Time");
        }
        return queryList(page, filter, Task.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HistoryOrder> getHistoryOrders(final Page<HistoryOrder> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(QUERY_HIST_ORDER);
        sql.append(" left join wf_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and o.creator in(");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and p.name in(");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_Id = ? ");
            paramList.add(filter.getProcessId());
        }
        if(StringHelper.isNotEmpty(filter.getProcessType())) {
            sql.append(" and p.type = ? ");
            paramList.add(filter.getProcessType());
        }
        if(StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_Name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if(StringHelper.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_Id = ? ");
            paramList.add(filter.getParentId());
        }
        if(filter.getState() != null) {
            sql.append(" and o.order_State = ? ");
            paramList.add(filter.getState());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and o.create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and o.create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(StringHelper.isNotEmpty(filter.getOrderNo())) {
            sql.append(" and o.order_No = ? ");
            paramList.add(filter.getOrderNo());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("o.create_Time");
        }
        return queryList(page, filter, HistoryOrder.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HistoryTask> getHistoryTasks(final Page<HistoryTask> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder(QUERY_HIST_TASK);
        final boolean isFetchActor = filter.getOperators() != null && filter.getOperators().length > 0;
        // TODO
        /*        if(isFetchActor) {
            sql.append(" left join wf_hist_task_actor ta on ta.task_id = id ");
        }
         */
        sql.append(" where 1=1 ");
        final List<Object> paramList = new ArrayList<Object>();
        if(StringHelper.isNotEmpty(filter.getOrderId())) {
            sql.append(" and order_Id = ? ");
            paramList.add(filter.getOrderId());
        }
        if(isFetchActor) {
            sql.append(" and operator in (");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and task_Name in (");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("finish_Time");
        }
        return queryList(page, filter, HistoryTask.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<WorkItem> getWorkItems(final Page<WorkItem> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" select distinct o.process_Id, t.order_Id, t.id as id, t.id as task_Id, p.display_Name as process_Name, p.instance_Url, o.parent_Id, o.creator, ");
        sql.append(" o.create_Time as order_Create_Time, o.expire_Time as order_Expire_Time, o.order_No, o.variable as order_Variable, ");
        sql.append(" t.display_Name as task_Name, t.task_Name as task_Key, t.task_Type, t.perform_Type, t.operator, t.action_Url, ");
        sql.append(" t.create_Time as task_Create_Time, t.finish_Time as task_End_Time, t.expire_Time as task_Expire_Time, t.variable as task_Variable ");
        sql.append(" from wf_task t ");
        sql.append(" left join wf_order o on t.order_id = o.id ");
        sql.append(" left join wf_task_actor ta on ta.task_id=t.id ");
        sql.append(" left join wf_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");

        /**
         * 查询条件构造sql的where条件
         */
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and ta.actor_Id in (");
            for(final String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }

        if(StringHelper.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_Id = ?");
            paramList.add(filter.getProcessId());
        }
        // XXX processId 和 processIds应2选1
        if (filter.getProcessIds() != null && filter.getProcessIds().length > 0) {
            sql.append(" and o.process_Id in(");
            for (int i = 0; i < filter.getProcessIds().length; i++) {
                sql.append("?,");
                paramList.add(filter.getProcessIds()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_Name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if(StringHelper.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_Id = ? ");
            paramList.add(filter.getParentId());
        }
        if(StringHelper.isNotEmpty(filter.getOrderId())) {
            sql.append(" and t.order_id = ? ");
            paramList.add(filter.getOrderId());
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and t.task_Name in (");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getTaskType() != null) {
            sql.append(" and t.task_Type = ? ");
            paramList.add(filter.getTaskType());
        }
        if(filter.getPerformType() != null) {
            sql.append(" and t.perform_Type = ? ");
            paramList.add(filter.getPerformType());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and t.create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and t.create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("t.create_Time");
        }

        return queryList(page, filter, WorkItem.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<HistoryOrder> getCCWorks(final Page<HistoryOrder> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" select id,process_Id,order_State,priority,cc.creator,cc.create_Time,end_Time,parent_Id,expire_Time,order_No,variable ");
        sql.append(" from wf_cc_order cc ");
        sql.append(" left join wf_hist_order o on cc.order_id = o.id ");
        sql.append(" where 1=1 ");

        /**
         * 查询条件构造sql的where条件
         */
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and cc.actor_Id in(");
            for(int i = 0; i < filter.getOperators().length; i++) {
                sql.append("?,");
                paramList.add(filter.getOperators()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getState() != null) {
            sql.append(" and cc.status = ? ");
            paramList.add(filter.getState());
        }
        if(StringHelper.isNotEmpty(filter.getProcessId())) {
            sql.append(" and process_Id = ? ");
            paramList.add(filter.getProcessId());
        }
        if(StringHelper.isNotEmpty(filter.getParentId())) {
            sql.append(" and parent_Id = ? ");
            paramList.add(filter.getParentId());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and cc.create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and cc.create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }
        if(StringHelper.isNotEmpty(filter.getOrderNo())) {
            sql.append(" and order_No = ? ");
            paramList.add(filter.getOrderNo());
        }
        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("cc.create_Time");
        }
        return queryList(page, filter, HistoryOrder.class, sql.toString(), paramList.toArray());
    }

    @Override
    public List<WorkItem> getHistoryWorkItems(final Page<WorkItem> page, final QueryFilter filter) {
        final StringBuilder sql = new StringBuilder();
        sql.append(" select distinct o.process_Id, t.order_Id, t.id as id, t.id as task_Id, p.display_Name as process_Name, p.instance_Url, o.parent_Id, o.creator, ");
        sql.append(" o.create_Time as order_Create_Time, o.expire_Time as order_Expire_Time, o.order_No, o.variable as order_Variable, ");
        sql.append(" t.display_Name as task_Name, t.task_Name as task_Key, t.task_Type, t.perform_Type,t.operator, t.action_Url, ");
        sql.append(" t.create_Time as task_Create_Time, t.finish_Time as task_End_Time, t.expire_Time as task_Expire_Time, t.variable as task_Variable ");
        sql.append(" from wf_hist_task t ");
        sql.append(" left join wf_hist_order o on t.order_id = o.id ");
        sql.append(" left join wf_hist_task_actor ta on ta.task_id=t.id ");
        sql.append(" left join wf_process p on p.id = o.process_id ");
        sql.append(" where 1=1 ");
        /**
         * 查询条件构造sql的where条件
         */
        final List<Object> paramList = new ArrayList<Object>();
        if(filter.getOperators() != null && filter.getOperators().length > 0) {
            sql.append(" and ta.actor_Id in (");
            for(final String actor : filter.getOperators()) {
                sql.append("?,");
                paramList.add(actor);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }

        if(StringHelper.isNotEmpty(filter.getProcessId())) {
            sql.append(" and o.process_Id = ?");
            paramList.add(filter.getProcessId());
        }
        if(StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and p.display_Name like ?");
            paramList.add("%" + filter.getDisplayName() + "%");
        }
        if(StringHelper.isNotEmpty(filter.getParentId())) {
            sql.append(" and o.parent_Id = ? ");
            paramList.add(filter.getParentId());
        }
        if(StringHelper.isNotEmpty(filter.getOrderId())) {
            sql.append(" and t.order_id = ? ");
            paramList.add(filter.getOrderId());
        }
        if(filter.getNames() != null && filter.getNames().length > 0) {
            sql.append(" and t.task_Name in (");
            for(int i = 0; i < filter.getNames().length; i++) {
                sql.append("?,");
                paramList.add(filter.getNames()[i]);
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") ");
        }
        if(filter.getTaskType() != null) {
            sql.append(" and t.task_Type = ? ");
            paramList.add(filter.getTaskType());
        }
        if(filter.getPerformType() != null) {
            sql.append(" and t.perform_Type = ? ");
            paramList.add(filter.getPerformType());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeStart())) {
            sql.append(" and t.create_Time >= ? ");
            paramList.add(filter.getCreateTimeStart());
        }
        if(StringHelper.isNotEmpty(filter.getCreateTimeEnd())) {
            sql.append(" and t.create_Time <= ? ");
            paramList.add(filter.getCreateTimeEnd());
        }

        if(!filter.isOrderBySetted()) {
            filter.setOrder(QueryFilter.DESC);
            filter.setOrderBy("t.create_Time");
        }
        return queryList(page, filter, WorkItem.class, sql.toString(), paramList.toArray());
    }

    @Override
    public <T> List<T> queryList(final Page<T> page, final QueryFilter filter, final Class<T> clazz, final String sql, final Object... args) {
        final String orderby = StringHelper.buildPageOrder(filter.getOrder(), filter.getOrderBy());
        String querySQL = sql + orderby;
        if(page == null) {
            return queryList(clazz, querySQL, args);
        }
        final String countSQL = "select count(1) from (" + sql + ") c ";
        querySQL = getDialect().getPageSql(querySQL, page);
        if(log.isDebugEnabled()) {
            log.debug("查询分页countSQL=\n" + countSQL);
            log.debug("查询分页querySQL=\n" + querySQL);
        }
        try {
            final Object count = queryCount(countSQL, args);
            List<T> list = queryList(clazz, querySQL, args);
            if(list == null) {
                list = Collections.emptyList();
            }
            page.setResult(list);
            page.setTotalCount(ClassHelper.castLong(count));
            return list;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 运行脚本
     */
    @Override
    public void runScript() {
        final String autoStr = ConfigHelper.getProperty("schema.auto");
        if(autoStr == null || !autoStr.equalsIgnoreCase("true")) {
            return;
        }
        Connection conn = null;
        try {
            conn = getConnection();
            if(JdbcHelper.isExec(conn)) {
                log.info("script has completed execution.skip this step");
                return;
            }
            final String databaseType = JdbcHelper.getDatabaseType(conn);
            final String schema = "db/core/schema-" + databaseType + ".sql";
            final ScriptRunner runner = new ScriptRunner(conn, true);
            runner.runScript(schema);
        } catch (final Exception e) {
            throw new SnakerException(e);
        } finally {
            try {
                JdbcHelper.close(conn);
            } catch (final SQLException e) {
                //ignore
            }
        }
    }

    /**
     * 分页查询时，符合条件的总记录数
     * @param sql sql语句
     * @param args 参数数组
     * @return 总记录数
     */
    protected abstract Object queryCount(String sql, Object... args);

    /**
     * 获取数据库连接
     * @return Connection
     */
    protected abstract Connection getConnection() throws SQLException;
}
