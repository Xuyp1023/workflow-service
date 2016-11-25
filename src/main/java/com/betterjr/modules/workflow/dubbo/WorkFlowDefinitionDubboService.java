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

import com.alibaba.dubbo.config.annotation.Service;
import com.betterjr.common.web.AjaxObject;
import com.betterjr.modules.rule.service.RuleServiceDubboFilterInvoker;
import com.betterjr.modules.workflow.IWorkFlowDefinitionService;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowStep;
import com.betterjr.modules.workflow.service.WorkFlowBaseService;
import com.betterjr.modules.workflow.service.WorkFlowNodeService;
import com.betterjr.modules.workflow.service.WorkFlowStepService;
import com.betterjr.modules.workflow.snaker.core.BetterSpringSnakerEngine;

/**
 * @author liuwl 流程定义服务
 */
@Service(interfaceClass = IWorkFlowDefinitionService.class)
public class WorkFlowDefinitionDubboService implements IWorkFlowDefinitionService {
    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowStepService workFlowStepService;

    @Inject
    private BetterSpringSnakerEngine engine;

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webQueryWorkFlowBase(java.lang.Long)
     */
    @Override
    public String webQueryWorkFlowBase(final Long anCustNo, final int anFlag, final int anPageNum, final int anPageSize) {
        return AjaxObject.newOkWithPage("查询流程定义列表成功！", workFlowBaseService.queryWorkFlowBaseByCustNo(anCustNo, anFlag, anPageNum, anPageSize))
                .toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webFindWorkFlowBase(java.lang.Long)
     */
    @Override
    public String webFindWorkFlowBase(final Long anBaseId) {
        return AjaxObject.newOk("查询流程定义详情成功！", workFlowBaseService.findWorkFlowBaseById(anBaseId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webQueryWorkFlowNode(java.lang.Long)
     */
    @Override
    public String webQueryWorkFlowNode(final Long anBaseId) {
        return AjaxObject.newOk("查询流程节点列表成功!", workFlowNodeService.queryWorkFlowNode(anBaseId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webFindWorkFlowNode(java.lang.Long)
     */
    @Override
    public String webFindWorkFlowNode(final Long anNodeId) {
        return AjaxObject.newOk("查询流程节点详情成功！", workFlowNodeService.findWorkFlowNodeById(anNodeId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webQueryWorkFlowStep(java.lang.Long)
     */
    @Override
    public String webQueryWorkFlowStep(final Long anNodeId) {
        return AjaxObject.newOk("查询流程步骤列表成功！", workFlowStepService.queryWorkFlowStepByNodeId(anNodeId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webFindWorkFlowStep(java.lang.Long)
     */
    @Override
    public String webFindWorkFlowStep(final Long anStepId) {
        return AjaxObject.newOk("查询流程步骤详情成功！", workFlowStepService.findWorkFlowStepById(anStepId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webAddWorkFlowBase(java.util.Map)
     */
    @Override
    public String webAddWorkFlowBase(final Map<String, Object> anParam, final Long anDefaultBaseId, final Long anCustNo) {
        final WorkFlowBase workFlowBase = RuleServiceDubboFilterInvoker.getInputObj();
        return AjaxObject.newOk("添加流程成功！", workFlowBaseService.addWorkFlowBase(workFlowBase, anDefaultBaseId, anCustNo)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveWorkFlowBase(java.util.Map)
     */
    @Override
    public String webSaveWorkFlowBase(final Map<String, Object> anParam, final Long anBaseId) {
        final WorkFlowBase workFlowBase = RuleServiceDubboFilterInvoker.getInputObj();
        return AjaxObject.newOk("修改流程成功！", workFlowBaseService.saveWorkFlowBase(anBaseId, workFlowBase)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webAddWorkFlowStep(java.util.Map)
     */
    @Override
    public String webAddWorkFlowStep(final Map<String, Object> anParam, final Long anBaseId, final Long anNodeId) {
        final WorkFlowStep workFlowStep = RuleServiceDubboFilterInvoker.getInputObj();
        return AjaxObject.newOk("添加步骤成功！", workFlowStepService.addWorkFlowStep(anBaseId, anNodeId, workFlowStep)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveWorkFlowStep(java.util.Map)
     */
    @Override
    public String webSaveWorkFlowStep(final Map<String, Object> anParam, final Long anBaseId, final Long anNodeId, final Long anStepId) {
        final WorkFlowStep workFlowStep = RuleServiceDubboFilterInvoker.getInputObj();
        return AjaxObject.newOk("修改步骤成功!", workFlowStepService.saveWorkFlowStep(anBaseId, anNodeId, anStepId, workFlowStep)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webDelWorkFlowStep(java.lang.Long)
     */
    @Override
    public String webDelWorkFlowStep(final Long anBaseId, final Long anNodeId, final Long anStepId) {
        workFlowStepService.saveDelWorkFlowStep(anBaseId, anNodeId, anStepId);
        return AjaxObject.newOk("删除步骤成功！").toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveWorkFlowStepDefine(java.util.Map)
     */
    @Override
    public String webSaveWorkFlowStepDefine(final Map<String, Object> anParam, final Long anBaseId, final Long anNodeId, final Long anStepId) {
        workFlowStepService.saveStepDefinition(anBaseId, anNodeId, anStepId, anParam);
        return AjaxObject.newOk("保存流程步骤定义成功!").toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveDisableWorkFlow(java.lang.Long)
     */
    @Override
    public String webSaveDisableWorkFlow(final Long anBaseId) {
        return AjaxObject.newOk("停用流程成功！", workFlowBaseService.saveDisableWorkFlow(anBaseId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveEnableWorkFlow(java.lang.Long)
     */
    @Override
    public String webSaveEnableWorkFlow(final Long anBaseId) {
        return AjaxObject.newOk("启用流程成功！", workFlowBaseService.saveEnableWorkFlow(anBaseId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveDisableWorkFlowNode(java.lang.Long)
     */
    @Override
    public String webSaveDisableWorkFlowNode(final Long anBaseId, final Long anNodeId) {
        return AjaxObject.newOk("停用流程节点成功！", workFlowNodeService.saveDiableWorkFlowNode(anBaseId, anNodeId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSaveEnableWorkFlowNode(java.lang.Long)
     */
    @Override
    public String webSaveEnableWorkFlowNode(final Long anBaseId, final Long anNodeId) {
        return AjaxObject.newOk("启用流程节点成功！", workFlowNodeService.saveEnableWorkFlowNode(anBaseId, anNodeId)).toJson();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.workflow.IWorkFlowDefinitionService#webSavePublishWorkFlow(java.lang.Long)
     */
    @Override
    public String webSavePublishWorkFlow(final Long anBaseId) {
        return AjaxObject.newOk("发布流程成功!", engine.process().deploy(anBaseId)).toJson();
    }

}
