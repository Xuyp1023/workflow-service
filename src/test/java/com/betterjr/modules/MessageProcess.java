// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月29日, liuwl, creation
// ============================================================================
package com.betterjr.modules;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author liuwl
 *
 */
public class MessageProcess implements Runnable {
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(100);

    public void put(final String xx){
        queue.offer(xx);
    }

    public MessageProcess() {
        new Thread(this).start();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while(true){
            try {
                final String value = queue.take(); //如果queue为空，则当前线程会堵塞，直到有新数据加入
                System.out.println("消费了：" + value);
            }
            catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
