// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.dubbo;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.config.annotation.Service;
import com.betterjr.common.mapper.JsonMapper;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.UserUtils;
import com.betterjr.common.web.AjaxObject;
import com.betterjr.modules.account.entity.CustOperatorInfo;
import com.betterjr.modules.workflow.IWorkFlowService;
import com.betterjr.modules.workflow.constants.WorkFlowInput;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.service.WorkFlowService;

/**
 * @author liuwl 流程服务
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
    public String webQueryCurrentTask(final int anPageNo, final int anPageSize, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");
        final Long operId = operator.getId();
        return AjaxObject.newOkWithPage("查询待办任务成功", workFlowService.queryWorkItem(operId, anPageNo, anPageSize, anParam)).toJson();
    }

    // 已办任务
    @Override
    public String webQueryHistoryTask(final int anPageNo, final int anPageSize, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");
        final Long operId = operator.getId();
        return AjaxObject.newOkWithPage("查询已办任务成功", workFlowService.queryHistoryWorkItem(operId, anPageNo, anPageSize, anParam)).toJson();
    }

    // 监控任务
    @Override
    public String webQueryMonitorTask(final long anCustNo, final int anPageNo, final int anPageSize, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");
        return AjaxObject.newOkWithPage("查询监控任务成功", workFlowService.queryMonitorWorkItem(anCustNo, anPageNo, anPageSize, anParam)).toJson();
    }

    // 加载节点
    @Override
    public String webFindTask(final String anTaskId) {
        return "";
    }

    // 审批通过
    @Override
    public String webPassWorkFlow(final String anTaskId, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");

        final int result = Integer.valueOf((String) anParam.get("result"));
        BTAssert.isTrue(result == 0, "审批类型不正确！");

        final Long operId = operator.getId();
        final String data = (String) anParam.get("data");
        final Map<String, Object> inputParam = JsonMapper.parserJson(data);
        final String content = (String) anParam.get("content");

        final WorkFlowInput flowInput = new WorkFlowInput(operId, anTaskId);
        flowInput.setOperName(operator.getName());
        flowInput.setContent(content);

        flowInput.addParam("INPUT", inputParam);
        return AjaxObject.newOk("审批通过成功！", workFlowService.savePassTask(flowInput)).toJson();
    }

    // 审批驳回
    @Override
    public String webRejectWorkFlow(final String anTaskId, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");

        final int result = Integer.valueOf((String) anParam.get("result"));
        BTAssert.isTrue(result == 1, "审批类型不正确！");

        final String rejectNode = (String) anParam.get("rejectNode");
        BTAssert.isTrue(BetterStringUtils.isNotBlank(rejectNode), "驳回节点不允许为空！");

        final Long operId = operator.getId();
        final String data = (String) anParam.get("data");
        final Map<String, Object> inputParam = JsonMapper.parserJson(data);
        final String content = (String) anParam.get("content");

        final WorkFlowInput flowInput = new WorkFlowInput(operId, anTaskId, rejectNode);
        flowInput.setOperName(operator.getName());
        flowInput.setContent(content);

        flowInput.addParam("INPUT", inputParam);
        return AjaxObject.newOk("审批驳回成功！", workFlowService.saveRejectTask(flowInput)).toJson();
    }

    // 经办提交
    @Override
    public String webHandleWorkFlow(final String anTaskId, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");

        final int result = Integer.valueOf((String) anParam.get("result"));
        BTAssert.isTrue(result == 2, "审批类型不正确！");

        final Long operId = operator.getId();
        final String data = (String) anParam.get("data");
        final Map<String, Object> inputParam = JsonMapper.parserJson(data);
        final String content = (String) anParam.get("content");

        final WorkFlowInput flowInput = new WorkFlowInput(operId, anTaskId);
        flowInput.setOperName(operator.getName());
        flowInput.setContent(content);

        flowInput.addParam("INPUT", inputParam);
        return AjaxObject.newOk("任务办理成功！", workFlowService.saveHandleTask(flowInput)).toJson();
    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.IWorkFlowService#webSaveWorkFlow(java.lang.String, java.util.Map)
     */
    @Override
    public String webSaveWorkFlow(final String anTaskId, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");

        final Long operId = operator.getId();
        final String data = (String) anParam.get("data");
        final Map<String, Object> param = JsonMapper.parserJson(data);
        final String content = (String) anParam.get("content");

        final WorkFlowInput flowInput = new WorkFlowInput(operId, anTaskId);
        flowInput.setOperName(operator.getName());
        flowInput.setContent(content);

        flowInput.addParam("INPUT", param);

        workFlowService.saveDataTask(flowInput);
        return AjaxObject.newOk("经办数据保存成功！").toJson();
    }


    // 作废提交
    @Override
    public String webCancelWorkFlow(final String anTaskId, final Map<String, Object> anParam) {
        final CustOperatorInfo operator = UserUtils.getOperatorInfo();
        BTAssert.notNull(operator, "不能获取当前登陆用户！");

        final int result = Integer.valueOf((String) anParam.get("result"));
        BTAssert.isTrue(result == 3, "审批类型不正确！");

        final Long operId = operator.getId();
        final String data = (String) anParam.get("data");
        final Map<String, Object> inputParam = JsonMapper.parserJson(data);
        final String content = (String) anParam.get("content");

        final WorkFlowInput flowInput = new WorkFlowInput(operId, anTaskId);
        flowInput.setOperName(operator.getName());
        flowInput.setContent(content);

        flowInput.addParam("INPUT", inputParam);
        return AjaxObject.newOk("作废流程成功！", workFlowService.saveCancelProcess(flowInput)).toJson();
    }

    // 审批记录
    @Override
    public String webQueryAudit(final String anBusinessId, final int anFlag, final int anPageNum, final int anPageSize) {
        return AjaxObject.newOkWithPage("审批记录查询成功！", workFlowService.queryWorkFlowAudit(anBusinessId, anFlag, anPageNum, anPageSize)).toJson();
    }

    // 查询当前可驳回节点列表 第一项为上一步
    @Override
    public String webQueryRejectNode(final String anTaskId) {
        return AjaxObject.newOk("驳回节点列表查询成功！", workFlowService.queryRejectNodeList(anTaskId)).toJson();
    }

    // 查询流程layout json数据
    @Override
    public String webFindWorkFlowJson(final String anProcessId, final String anOrderId) {
        return AjaxObject.newOk(workFlowService.findWorkFlowJson(anProcessId, anOrderId)).toJson();
    }

    @Override
    public String webChangeApprover(final String anTaskId, final Long anOperId) {
        return AjaxObject.newOk("分配操作员成功！", workFlowService.saveChangeApprover(anTaskId, anOperId)).toJson();
    }
}
