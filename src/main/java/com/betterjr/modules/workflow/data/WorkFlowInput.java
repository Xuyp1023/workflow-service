// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.data;

/**
 * @author liuwl
 *
 */
public class WorkFlowInput {

    private String flowName;    // 流程名称
    private Long flowCustNo;    // 流程所属公司

    //// 参与者公司编号
    private Long factorCustNo;
    private Long supplierCustNo;
    private Long coreCustNo;
    private Long sellerCustNo;
    private Long platformCustNo;

    /**
     * 流程启动时检查
     * @return
     */
    public boolean checkStart() {
        return true;
    }


    public String getFlowName() {
        return flowName;
    }
    public void setFlowName(final String anFlowName) {
        flowName = anFlowName;
    }
    public Long getFlowCustNo() {
        return flowCustNo;
    }
    public void setFlowCustNo(final Long anFlowCustNo) {
        flowCustNo = anFlowCustNo;
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
}
