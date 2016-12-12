// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年12月5日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.snaker.util;

import org.snaker.engine.SnakerEngine;
import org.snaker.engine.core.ServiceContext;
import org.snaker.engine.entity.Process;

/**
 * @author liuwl
 *
 */
public class SnakerHelper {
    /**
     * 获取 processName
     * @param anProcessId
     * @return
     */
    public static String getProcessName(final String anProcessId) {
        final SnakerEngine engine = ServiceContext.getEngine();
        final Process process = engine.process().getProcessById(anProcessId);
        if(process == null) {
            return anProcessId;
        }
        return process.getDisplayName();
    }
}
