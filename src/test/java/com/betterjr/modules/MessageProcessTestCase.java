// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月29日, liuwl, creation
// ============================================================================
package com.betterjr.modules;

import org.junit.Test;

/**
 * @author liuwl
 *
 */
public class MessageProcessTestCase {
    @Test
    public void testProcess() {
        final MessageProcess messageProcess = new MessageProcess();

        messageProcess.put("abc1");
        messageProcess.put("abc2");
        messageProcess.put("abc3");

        messageProcess.put("a");
        messageProcess.put("b");
        messageProcess.put("c");
        messageProcess.put("d");
        messageProcess.put("e");


    }
}
