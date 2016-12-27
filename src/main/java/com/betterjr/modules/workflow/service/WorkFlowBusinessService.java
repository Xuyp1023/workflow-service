// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.dao.WorkFlowBusinessMapper;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowBusinessService extends BaseService<WorkFlowBusinessMapper, WorkFlowBusiness> {

    /**
     * 添加流程业务
     *
     * @param anWorkFlowBusiness
     */
    public WorkFlowBusiness addWorkFlowBusiness(final WorkFlowBusiness anWorkFlowBusiness) {
        BTAssert.notNull(anWorkFlowBusiness, "流程业务数据不允许为空！");

        anWorkFlowBusiness.initAddValue();
        this.insert(anWorkFlowBusiness);
        return anWorkFlowBusiness;
    }

    /**
     * 根据流程实例查找 流程业务数据
     * @param anOrderId
     * @return
     */
    public WorkFlowBusiness findWorkFlowBusinessByOrderId(final String anOrderId) {
        BTAssert.isTrue(BetterStringUtils.isNotBlank(anOrderId), "流程实例编号不允许为空");

        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("orderId", anOrderId);

        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 根据业务编号查找 流程业务数据
     * @param anBusinessId
     */
    public List<WorkFlowBusiness> findWorkFlowBusinessById(final String anBusinessId) {
        BTAssert.isTrue(BetterStringUtils.isNotBlank(anBusinessId), "流程业务编号不允许为空");

        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("businessId", anBusinessId);

        return this.selectByProperty(conditionMap);
    }
}
