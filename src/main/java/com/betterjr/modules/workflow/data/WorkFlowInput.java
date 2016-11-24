// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.betterjr.common.utils.BTAssert;

/**
 * 流程运行输入项
 * @author liuwl
 *
 */
public class WorkFlowInput {

    private String flowName; // 流程名称
    private Long flowCustNo; // 流程所属公司

    public static final String FACTOR_CUSTNO = "factorCustNo";
    public static final String SUPPLIER_CUSTNO = "supplierCustNo";
    public static final String CORE_CUSTNO = "coreCustNo";
    public static final String SELLER_CUSTNO = "sellerCustNo";
    public static final String PLATFORM_CUSTNO = "platformCustNo";
    public static final String BUSINESS_ID = "businessId";
    public static final String BUSINESS_TYPE = "businessType";
    public static final String CREATOR = "creator";

    //// 参与者公司编号
    private Long factorCustNo;
    private Long supplierCustNo;
    private Long coreCustNo;
    private Long sellerCustNo;
    private Long platformCustNo;

    private final Map<String, Object> param;

    private Long businessId;
    private String businessType;

    private final Long operId;

    private String taskId;

    /**
     * 启动流程时使用
     * @param anOperId
     * @param anWorkFlowName
     * @param anWorkFLowCustNo
     * @param anBusinessId
     * @param anBusinessType
     */
    public WorkFlowInput(final Long anOperId, final String anWorkFlowName, final Long anWorkFLowCustNo, final Long anBusinessId,
            final String anBusinessType) {
        BTAssert.isTrue(anBusinessType.length() == 2, "业务类型必须为2个字符");
        this.flowName = anWorkFlowName;
        this.flowCustNo = anWorkFLowCustNo;
        this.businessId = anBusinessId;
        this.businessType = anBusinessType;
        this.operId = anOperId;
        param = new HashMap<>();
    }

    /**
     * 执行任务时使用
     * @param anOperId
     * @param anTaskId
     */
    public WorkFlowInput(final Long anOperId, final String anTaskId) {
        this.operId = anOperId;
        this.taskId = anTaskId;
        param = new HashMap<>();
    }

    /**
     * 驳回时使用
     * @param anOperId
     * @param anTaskId
     * @param anRejectNode
     */
    public WorkFlowInput(final Long anOperId, final String anTaskId, final String anRejectNode) {
        this.operId = anOperId;
        this.taskId = anTaskId;
        param = new HashMap<>();
    }

    /**
     * 添加其它其它参数
     * @param anKey
     * @param anValue
     */
    public void addParam(final String anKey, final Object anValue) {
        checkKey(anKey);
        param.put(anKey, anValue);
    }

    /**
     * @param anKey
     */
    private void checkKey(final String anKey) {
        final List<String> keys = Arrays.asList(FACTOR_CUSTNO, SUPPLIER_CUSTNO, CORE_CUSTNO, SELLER_CUSTNO, PLATFORM_CUSTNO, BUSINESS_ID,
                BUSINESS_TYPE, CREATOR);
        BTAssert.isTrue(keys.contains(anKey) == false, "不允许使用当前key值作参数");
    }

    public Map<String, Object> getParam() {
        if (factorCustNo != null) {
            param.put(FACTOR_CUSTNO, factorCustNo);
        }
        if (supplierCustNo != null) {
            param.put(SUPPLIER_CUSTNO, supplierCustNo);
        }
        if (coreCustNo != null) {
            param.put(CORE_CUSTNO, coreCustNo);
        }
        if (sellerCustNo != null) {
            param.put(SELLER_CUSTNO, sellerCustNo);
        }
        if (platformCustNo != null) {
            param.put(PLATFORM_CUSTNO, platformCustNo);
        }
        if (businessId != null) {
            param.put(BUSINESS_ID, businessId);
        }
        if (businessType != null) {
            param.put(BUSINESS_TYPE, businessType);
        }
        if (operId != null) {
            param.put(CREATOR, operId);
        }
        return param;
    }

    public String getFlowName() {
        return flowName;
    }

    public Long getFlowCustNo() {
        return flowCustNo;
    }

    public Long getFactorCustNo() {
        return factorCustNo;
    }

    public void setFactorCustNo(final Long anFactorCustNo) {
        factorCustNo = anFactorCustNo;
    }

    public Long getSupplierCustNo() {
        return supplierCustNo;
    }

    public void setSupplierCustNo(final Long anSupplierCustNo) {
        supplierCustNo = anSupplierCustNo;
    }

    public Long getCoreCustNo() {
        return coreCustNo;
    }

    public void setCoreCustNo(final Long anCoreCustNo) {
        coreCustNo = anCoreCustNo;
    }

    public Long getSellerCustNo() {
        return sellerCustNo;
    }

    public void setSellerCustNo(final Long anSellerCustNo) {
        sellerCustNo = anSellerCustNo;
    }

    public Long getPlatformCustNo() {
        return platformCustNo;
    }

    public void setPlatformCustNo(final Long anPlatformCustNo) {
        platformCustNo = anPlatformCustNo;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public Long getOperId() {
        return operId;
    }

    public String getTaskId() {
        return taskId;
    }

}
