// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年12月6日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.handlers.demo;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.betterjr.modules.workflow.handler.INodeHandler;

/**
 * @author liuwl
 *
 */
@Service("demoHandleFormHandler")
public class HandleFormHandler implements INodeHandler {

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.INodeHandler#processPass(java.util.Map)
     */
    @Override
    public void processPass(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.INodeHandler#processReject(java.util.Map)
     */
    @Override
    public void processReject(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.INodeHandler#processSave(java.util.Map)
     */
    @Override
    public void processSave(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.INodeHandler#processHandle(java.util.Map)
     */
    @Override
    public void processHandle(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

}
