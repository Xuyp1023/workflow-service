/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine.model;

import org.snaker.engine.core.Execution;
import org.snaker.engine.handlers.impl.MergeBranchHandler;

/**
 * 合并定义join元素
 * @author yuqs
 * @since 1.0
 */
public class JoinModel extends NodeModel {
    /**
     * 
     */
    private static final long serialVersionUID = 5296621319088076775L;

    @Override
    public void exec(Execution execution) {
        fire(new MergeBranchHandler(this), execution);
        if (execution.isMerged()) runOutTransition(execution);
    }
}
