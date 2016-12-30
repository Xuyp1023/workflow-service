// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年12月9日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.handlers.demo;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.betterjr.modules.workflow.handler.IProcessHandler;

/**
 * @author liuwl
 *
 */
@Service("demoProcessHandler")
public class DemoProcessHandler implements IProcessHandler {

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.IProcessHandler#processCancel(java.util.Map)
     */
    @Override
    public void processCancel(final Map<String, Object> anContext) {
        System.out.println("作废流程");

    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.IProcessHandler#processEnd(java.util.Map)
     */
    @Override
    public void processEnd(final Map<String, Object> anContext) {
        System.out.println("流程结束");
    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.IProcessHandler#processStart(java.util.Map)
     */
    @Override
    public void processStart(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

}
