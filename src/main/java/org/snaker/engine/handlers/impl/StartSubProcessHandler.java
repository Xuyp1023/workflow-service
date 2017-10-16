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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.snaker.engine.SnakerEngine;
import org.snaker.engine.SnakerException;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.core.Execution;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.handlers.IHandler;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.model.SubProcessModel;

import com.betterjr.common.exception.BytterException;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.constants.WorkFlowInput;

/**
 * 启动子流程的处理器
 *
 * @author yuqs
 * @since 1.0
 */
public class StartSubProcessHandler implements IHandler {
    private final SubProcessModel model;
    /**
     * 是否以future方式执行启动子流程任务
     */
    private boolean isFutureRunning = false;

    public StartSubProcessHandler(final SubProcessModel model) {
        this.model = model;
    }

    public StartSubProcessHandler(final SubProcessModel model, final boolean isFutureRunning) {
        this.model = model;
        this.isFutureRunning = isFutureRunning;
    }

    /**
     * 子流程执行的处理
     */
    @Override
    public void handle(final Execution execution) {
        // 根据子流程模型名称获取子流程定义对象
        final SnakerEngine engine = execution.getEngine();

        final Map<String, Object> param = execution.getArgs();
        final Long custNo = findCustNo(model, param);

        execution.setOperator(WorkFlowConstants.PREFIX_CUST_NO + String.valueOf(custNo));

        // TODO 这里需要解决输入 具体公司的问题 不能按version
        final Process process = engine.process().getProcessByName(model.getProcessName(), custNo);

        final Execution child = execution.createSubExecution(execution, process, model.getName());
        Order order = null;
        if (isFutureRunning) {
            // 创建单个线程执行器来执行启动子流程的任务
            final ExecutorService es = Executors.newSingleThreadExecutor();
            // 提交执行任务，并返回future
            final Future<Order> future = es.submit(new ExecuteTask(execution, process, model.getName()));
            try {
                es.shutdown();
                order = future.get();
            }
            catch (final InterruptedException e) {
                throw new SnakerException("创建子流程线程被强制终止执行", e.getCause());
            }
            catch (final ExecutionException e) {
                throw new SnakerException("创建子流程线程执行异常.", e.getCause());
            }
        } else {
            order = engine.startInstanceByExecution(child);
        }
        AssertHelper.notNull(order, "子流程创建失败");
        execution.addTasks(engine.query().getActiveTasks(new QueryFilter().setOrderId(order.getId())));
    }

    private Long findCustNo(final SubProcessModel model, final Map<String, Object> args) {
        final String operRole = model.getOperRole();
        AssertHelper.notEmpty(operRole);

        Long custNo = null;
        switch (operRole) { // CORE_USER 、PLATFORM_USER、FACTOR_USER、SUPPLIER_USER、SELLER_USER
        case "CORE_USER":
            custNo = getCustNo(args, WorkFlowInput.CORE_CUSTNO);
            break;
        case "PLATFORM_USER":
            custNo = getCustNo(args, WorkFlowInput.PLATFORM_CUSTNO);
            break;
        case "FACTOR_USER":
            custNo = getCustNo(args, WorkFlowInput.FACTOR_CUSTNO);
            break;
        case "SUPPLIER_USER":
            custNo = getCustNo(args, WorkFlowInput.SUPPLIER_CUSTNO);
            break;
        case "SELLER_USER":
            custNo = getCustNo(args, WorkFlowInput.SELLER_CUSTNO);
            break;
        }
        AssertHelper.notNull(custNo);
        return custNo;
    }

    /**
     *
     * @param anArgs
     * @param anCoreCustno
     * @return
     */
    private Long getCustNo(final Map<String, Object> anArgs, final String anCustType) {
        final Object object = anArgs.get(anCustType);
        BTAssert.notNull(object, "没有找到相应的公司编号");

        if (object instanceof Long) {
            return (Long) object;
        } else if (object instanceof Integer) {
            final Integer temp = (Integer) object;
            return temp.longValue();
        } else {
            throw new BytterException("公司编号数值类型不正确");
        }
    }

    /**
     * Future模式的任务执行。通过call返回任务结果集
     *
     * @author yuqs
     * @since 1.0
     */
    class ExecuteTask implements Callable<Order> {
        private final SnakerEngine engine;
        private final Execution child;

        /**
         * 构造函数
         *
         * @param execution
         * @param process
         * @param parentNodeName
         */
        public ExecuteTask(final Execution execution, final Process process, final String parentNodeName) {
            this.engine = execution.getEngine();
            child = execution.createSubExecution(execution, process, parentNodeName);
        }

        @Override
        public Order call() throws Exception {
            return engine.startInstanceByExecution(child);
        }
    }
}
