// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.snaker.engine.core.Execution;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TransitionModel;
import org.snaker.engine.spring.SpringSnakerEngine;

import com.betterjr.common.utils.BTAssert;

/**
 * @author liuwl
 *
 */
public class BetterSpringSnakerEngine extends SpringSnakerEngine {

    public List<Task> executeAndJumpTask(final String anTaskId, final String anOperator,
            final Map<String, Object> anArgs, final List<String> parallelNodes, final String anNodeName) {
        BTAssert.isTrue(StringUtils.isNotBlank(anNodeName), "驳回节点名不允许为空！");
        // XXX 在当前系统不存在驳回上一步的概念

        final Execution execution = execute(anTaskId, anOperator, anArgs);
        if (execution == null) {
            return Collections.emptyList();
        }
        final ProcessModel model = execution.getProcess().getModel();
        AssertHelper.notNull(model, "当前任务未找到流程定义模型");
        if (StringHelper.isEmpty(anNodeName)) {
            final Task newTask = task().rejectTask(model, execution.getTask());
            execution.addTask(newTask);
        } else {
            for (final String nodeName : parallelNodes) {
                // 把所有并行节点找出来 全部驳回
                final NodeModel nodeModel = model.getNode(nodeName);
                AssertHelper.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");
                // 动态创建转移对象，由转移对象执行execution实例
                final TransitionModel tm = new TransitionModel();
                tm.setTarget(nodeModel);
                tm.setEnabled(true);
                tm.execute(execution);
            }
        }

        return execution.getTasks();
    }

}
