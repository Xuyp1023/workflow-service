// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月16日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.constant;

/**
 * @author liuwl
 *
 */
public interface WorkFlowConstants {
    //最新版本
    String IS_LAST = "1";
    String NOT_LAST = "0";

    //默认流程:开发阶段预置
    String IS_DEFAULT = "1";
    String NOT_DEFAULT = "0";

    //发布状态
    String IS_PUBLISHED = "1";
    String NOT_PUBLISHED = "0";

    //禁用状态
    String IS_DISABLED = "1";
    String NOT_DISABLED = "0";

    // 起始
    String NODE_TYPE_START = "0";
    // 结束
    String NODE_TYPE_END = "1";
    //经办节点
    String NODE_TYPE_OPER = "2";
    //审批节点
    String NODE_TYPE_APP = "3";
    //子流程
    String NODE_TYPE_SUB = "4";


    // 节点类型
    String PARENT_TYPE_NODE = "0";
    // 步骤类型
    String PARENT_TYPE_STEP = "1";


    String AUDIT_TYPE_PARALLEL = "1";
    String AUDIT_TYPE_SERIAL = "0";

    String IS_MONEY_TRUE = "1";
    String IS_MONEY_FALSE = "0";
}
