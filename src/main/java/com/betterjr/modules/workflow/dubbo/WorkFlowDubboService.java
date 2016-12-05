// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.dubbo;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.config.annotation.Service;
import com.betterjr.modules.workflow.IWorkFlowService;
import com.betterjr.modules.workflow.constants.WorkFlowInput;
import com.betterjr.modules.workflow.data.WorkFlowTask;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.service.WorkFlowService;

/**
 * @author liuwl
 * 流程服务
 */
@Service(interfaceClass = IWorkFlowService.class)
public class WorkFlowDubboService implements IWorkFlowService {
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowDubboService.class);

    @Inject
    private WorkFlowService workFlowService;

    // 启动流程
    @Override
    public WorkFlowBusiness startWorkFlow(final WorkFlowInput workFlowInput) {

        return workFlowService.saveStart(workFlowInput);
    }

    // 待办任务
    @Override
    public String webQueryTask(final Long anOperId, final Integer anPageNo) {
        final List<WorkFlowTask> tasks = workFlowService.queryCurrentTask(anOperId, anPageNo);
        return "";
    }

    // 已办任务
    @Override
    public String webQueryHistoryTask() {
        return "";
    }

    // 加载节点
    @Override
    public String webLoadTask() {
        return "";
    }

    // 审批通过
    @Override
    public String webPassWorkFlow() {
        return "";
    }

    // 审批驳回
    @Override
    public String webRejectWofkFlow() {
        return "";
    }

    // 审批记录
    @Override
    public String webQueryAudit() {
        return "";
    }

    // 查询当前可驳回节点列表 第一项为上一步
    @Override
    public String queryRejectNode() {
        return "";
    }

}
