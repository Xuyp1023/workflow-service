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
package org.snaker.engine;

import java.io.InputStream;
import java.util.List;

import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Process;

/**
 * 流程定义业务类
 * @author yuqs
 * @since 1.0
 */
public interface IProcessService {
    /**
     * 检查流程定义对象
     * @param process 流程定义对象
     * @param idOrName 流程定义id/name
     */
    void check(Process process, String idOrName);

    /**
     * 保存流程定义
     * @param process 流程定义对象
     */
    void saveProcess(Process process);

    /**
     * 更新流程定义的类别
     * @param id 流程定义id
     * @param type 类别
     * @since 1.5
     */
    void updateType(String id, String type);

    /**
     * 根据主键ID获取流程定义对象
     * @param id 流程定义id
     * @return Process 流程定义对象
     */
    Process getProcessById(String id);

    /**
     * 根据流程name、operOrg获取流程定义对象
     * @param name 流程名称
     * @param custNo 机构名称
     * @return 流程定义对象
     */
    Process getProcessByName(String name, Long custNo);

    /**
     * 根据流程name、openOrg、version获取流程定义对象
     * @param name 流程定义名称
     * @param custNo 机构名称
     * @param version 版本号
     * @return Process 流程定义对象
     */
    Process getProcessByVersion(String name, Long custNo, Integer version);

    /**
     * 根据给定的参数列表args查询process
     * @param filter 查询过滤器
     * @return List<Process> 流程定义对象集合
     */
    List<Process> getProcesss(QueryFilter filter);

    /**
     * 根据给定的参数列表args分页查询process
     * @param page 分页对象
     * @param filter 查询过滤器
     * @return List<Process> 流程定义对象集合
     */
    List<Process> getProcesss(Page<Process> page, QueryFilter filter);

    /**
     * 根据 Better 流程定义发布流程
     * @param baseId 基础流程编号
     * @return String 流程定义id
     */
    String deploy(Long baseId);

    /**
     * 根據InputStream輸入流，部署流程定义
     * @param input 流程定义输入流
     * @param custNo 所属机构
     * @return String 流程定义id
     */
    String deploy(InputStream input, Long custNo);

    /**
     * 根据InputStream输入流，以及当前流程所属机构，部署流程定义
     * @param input 流程定义输入流
     * @param creator 创建人
     * @param custNo 流程所属机构
     * @return String 流程定义id
     */
    String deploy(InputStream input, Long custNo, String creator);

    /**
     * 根據InputStream輸入流，部署流程定义
     * @param id 流程定义id
     * @param input 流程定义输入流
     */
    void redeploy(String id, InputStream input);

    /**
     * 卸载指定的流程定义，只更新状态
     * @param id 流程定义id
     */
    void undeploy(String id);

    /**
     * 谨慎使用.数据恢复非常痛苦，你懂得~~
     * 级联删除指定流程定义的所有数据：
     * 1.wf_process
     * 2.wf_order,wf_hist_order
     * 3.wf_task,wf_hist_task
     * 4.wf_task_actor,wf_hist_task_actor
     * 5.wf_cc_order
     * @param id
     */
    void cascadeRemove(String id);
}
