// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月22日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.handler;

/**
 * @author liuwl
 *
 */
public interface WorkFlowHandler {
    void pre();
    void process();
    void post();
}
