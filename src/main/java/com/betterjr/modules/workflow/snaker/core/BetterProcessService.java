// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.core;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.SnakerException;
import org.snaker.engine.core.ProcessService;
import org.snaker.engine.entity.Process;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.ProcessModel;

import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.service.WorkFlowApproverService;
import com.betterjr.modules.workflow.service.WorkFlowBaseService;
import com.betterjr.modules.workflow.service.WorkFlowMoneyService;
import com.betterjr.modules.workflow.service.WorkFlowNodeService;
import com.betterjr.modules.workflow.service.WorkFlowStepService;
import com.betterjr.modules.workflow.snaker.parser.BetterModelParser;

/**
 * @author liuwl
 *
 */
public class BetterProcessService extends ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(BetterProcessService.class);

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    @Inject
    private WorkFlowStepService workFlowStepService;

    @Inject
    private WorkFlowMoneyService workFlowMoneyService;

    @Inject
    private WorkFlowApproverService workFlowApproverService;

    /**
     * 根据 Better 流程定义发布流程
     *
     * @param anBaseId
     *            基础流程编号
     * @return String 流程定义id
     */
    @Override
    public String deploy(final Long anBaseId) {
        try {
            final WorkFlowBase workFlowBase = workFlowBaseService.checkWorkFlowBaseByPublish(anBaseId);

            final Integer version = access().getLatestProcessVersion(workFlowBase.getName(), workFlowBase.getCustNo());
            final ProcessModel model = BetterModelParser.parse(anBaseId);
            final Process entity = new Process();
            entity.setId(StringHelper.getPrimaryKey());
            if (version == null || version < 0) {
                entity.setVersion(0);
            }
            else {
                entity.setVersion(version + 1);
            }
            entity.setState(STATE_ACTIVE);
            entity.setModel(model);
            entity.setCreateTime(DateHelper.getTime());
            entity.setCustNo(workFlowBase.getCustNo());
            saveProcess(entity);
            cache(entity);
            final String processId = entity.getId();
            workFlowBaseService.savePublishWorkFlow(anBaseId, processId);
            return processId;
        }
        catch (final Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new SnakerException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void undeploy(final String id) {
        super.undeploy(id);
        workFlowBaseService.saveDisableWorkFlow(id); // 从流程定义停用即可
    }
}
