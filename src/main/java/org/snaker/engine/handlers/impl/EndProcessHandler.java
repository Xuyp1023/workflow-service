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
package org.snaker.engine.handlers.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snaker.engine.SnakerEngine;
import org.snaker.engine.SnakerException;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.core.Execution;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.handlers.IHandler;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.SubProcessModel;

import com.betterjr.common.service.SpringContextHolder;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.handler.IProcessHandler;
import com.betterjr.modules.workflow.service.WorkFlowBusinessService;

/**
 * 结束流程实例的处理器
 *
 * @author yuqs
 * @since 1.0
 */
public class EndProcessHandler implements IHandler {
    /**
     * 结束当前流程实例，如果存在父流程，则触发父流程继续执行
     */
    @Override
    public void handle(final Execution execution) {
        final ProcessModel processModel = execution.getModel();

        final WorkFlowBase workFlowBase = processModel.getWorkFlowBase();
        final String handlerName = workFlowBase.getHandler();
        if (BetterStringUtils.isNotBlank(handlerName)) {
            final IProcessHandler handler = SpringContextHolder.getBean(handlerName);
            final WorkFlowBusinessService workFlowBusinessService = SpringContextHolder.getBean(WorkFlowBusinessService.class);
            if (handler != null && workFlowBusinessService != null) {
                final Order order = execution.getOrder();

                WorkFlowBusiness workFlowBusiness = null;
                if (BetterStringUtils.equals(workFlowBase.getIsSubprocess(), "1")) {
                    workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getParentId());
                }
                else {
                    workFlowBusiness = workFlowBusinessService.findWorkFlowBusinessByOrderId(order.getId());
                }

                final Map<String, Object> param = new HashMap<>();
                param.put("BASE", workFlowBase);
                param.put("BUSINESS", workFlowBusiness);
                handler.processEnd(param);
            }
        }

        final SnakerEngine engine = execution.getEngine();
        final Order order = execution.getOrder();
        final List<Task> tasks = engine.query().getActiveTasks(new QueryFilter().setOrderId(order.getId()));
        for (final Task task : tasks) {
            if (task.isMajor()) {
                throw new SnakerException("存在未完成的主办任务,请确认.");
            }
            engine.task().complete(task.getId(), SnakerEngine.AUTO);
        }
        /**
         * 结束当前流程实例
         */
        engine.order().complete(order.getId());

        /**
         * 如果存在父流程，则重新构造Execution执行对象，交给父流程的SubProcessModel模型execute
         */
        if (StringHelper.isNotEmpty(order.getParentId())) {
            final Order parentOrder = engine.query().getOrder(order.getParentId());
            if (parentOrder == null) {
                return;
            }
            final Process process = engine.process().getProcessById(parentOrder.getProcessId());
            final ProcessModel pm = process.getModel();
            if (pm == null) {
                return;
            }
            final SubProcessModel spm = (SubProcessModel) pm.getNode(order.getParentNodeName());
            final Execution newExecution = new Execution(engine, process, parentOrder, execution.getArgs());
            newExecution.setChildOrderId(order.getId());
            newExecution.setTask(execution.getTask());
            spm.execute(newExecution);
            /**
             * SubProcessModel执行结果的tasks合并到当前执行对象execution的tasks列表中
             */
            execution.addTasks(newExecution.getTasks());
        }

    }
}
