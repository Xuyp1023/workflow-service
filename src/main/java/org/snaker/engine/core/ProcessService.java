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
package org.snaker.engine.core;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.IProcessService;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.cache.Cache;
import org.snaker.engine.cache.CacheManager;
import org.snaker.engine.cache.CacheManagerAware;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.Process;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.StringHelper;

import com.betterjr.modules.workflow.snaker.parser.BetterModelParser;
import com.mchange.util.AssertException;

/**
 * 流程定义业务类
 * @author yuqs
 * @since 1.0
 */
public class ProcessService extends AccessService implements IProcessService, CacheManagerAware {
    private static final Logger log = LoggerFactory.getLogger(ProcessService.class);
    private static final String DEFAULT_SEPARATOR = ".";
    /**
     * 流程定义对象cache名称
     */
    private static final String CACHE_ENTITY = "snaker.process.entity";
    /**
     * 流程id、name的cache名称
     */
    private static final String CACHE_NAME = "snaker.process.name";
    /**
     * cache manager
     */
    private CacheManager cacheManager;
    /**
     * 实体cache(key=name,value=entity对象)
     */
    private Cache<String, Process> entityCache;
    /**
     * 名称cache(key=id,value=name对象)
     */
    private Cache<String, String> nameCache;

    @Override
    public void check(final Process process, final String idOrName) {
        AssertHelper.notNull(process, "指定的流程定义[id/name=" + idOrName + "]不存在");
        if(process.getState() != null && process.getState() == 0) {
            throw new IllegalArgumentException("指定的流程定义[id/name=" + idOrName +
                    ",custNo" + process.getCustNo() + ",version=" + process.getVersion() + "]为非活动状态");
        }
    }

    /**
     * 保存process实体对象
     */
    @Override
    public void saveProcess(final Process process) {
        access().saveProcess(process);
    }

    /**
     * 更新process的类别
     */
    @Override
    public void updateType(final String id, final String type) {
        final Process entity = getProcessById(id);
        entity.setType(type);
        access().updateProcessType(id, type);
        cache(entity);
    }

    /**
     * 根据id获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessById(final String id) {
        AssertHelper.notEmpty(id);
        Process entity = null;
        String processName;
        final Cache<String, String> nameCache = ensureAvailableNameCache();
        final Cache<String, Process> entityCache = ensureAvailableEntityCache();
        if(nameCache != null && entityCache != null) {
            processName = nameCache.get(id);
            if(StringHelper.isNotEmpty(processName)) {
                entity = entityCache.get(processName);
            }
        }
        if(entity != null) {
            if(log.isDebugEnabled()) {
                log.debug("obtain process[id={}] from cache.", id);
            }
            return entity;
        }
        entity = access().getProcess(id);
        if(entity != null) {
            if(log.isDebugEnabled()) {
                log.debug("obtain process[id={}] from database.", id);
            }
            cache(entity);
        }
        return entity;
    }

    /**
     * 根据 name 和 operOrg获取 process对象
     * @param name
     * @param custNo
     * @return
     */
    @Override
    public Process getProcessByName(final String name, final Long custNo) {
        return getProcessByVersion(name, custNo, null);
    }

    /**
     * 根据name获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessByVersion(final String name, final Long custNo, Integer version) {
        AssertHelper.notEmpty(name);
        AssertHelper.notNull(custNo);
        if(version == null) {
            version = access().getLatestProcessVersion(name, custNo);
        }
        if(version == null) {
            version = 0;
        }
        Process entity = null;
        final String processName = name + DEFAULT_SEPARATOR + custNo + DEFAULT_SEPARATOR + version;
        final Cache<String, Process> entityCache = ensureAvailableEntityCache();
        if(entityCache != null) {
            entity = entityCache.get(processName);
        }
        if(entity != null) {
            if(log.isDebugEnabled()) {
                log.debug("obtain process[name={}] from cache.", processName);
            }
            return entity;
        }

        final List<Process> processs = access().getProcesss(null, new QueryFilter().setName(name).setCustNo(custNo).setVersion(version));
        if(processs != null && !processs.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("obtain process[name={}] from database.", processName);
            }
            entity = processs.get(0);
            cache(entity);
        }
        return entity;
    }

    /**
     * 根据 Better 流程定义发布流程
     * @param baseId 基础流程编号
     * @return String 流程定义id
     */
    @Override
    public String deploy(final Long baseId) {
        throw new AssertException("必须重写！");
    }

    /**
     * 根据流程定义xml的输入流解析为字节数组，保存至数据库中，并且put到缓存中
     * @param input 定义输入流
     * @param custNo 机构
     */
    @Override
    public String deploy(final InputStream input, final Long custNo) {
        throw new AssertException("不可使用！");
        //        return deploy(input, custNo, null);
    }

    /**
     * 根据流程定义xml的输入流解析为字节数组，保存至数据库中，并且put到缓存中
     * @param input 定义输入流
     * @param custNo 机构
     * @param creator 创建人
     */

    @Override
    public String deploy(final InputStream input, final Long custNo, final String creator) {
        throw new AssertException("不可使用！");
        /*
        AssertHelper.notNull(input);
        AssertHelper.notNull(custNo);
        try {
            final byte[] bytes = StreamHelper.readBytes(input);
            final ProcessModel model = ModelParser.parse(bytes);
            final Integer version = access().getLatestProcessVersion(model.getName(), custNo);
            final Process entity = new Process();
            entity.setId(StringHelper.getPrimaryKey());
            if(version == null || version < 0) {
                entity.setVersion(0);
            } else {
                entity.setVersion(version + 1);
            }
            entity.setState(STATE_ACTIVE);
            entity.setModel(model);
            entity.setBytes(bytes);
            entity.setCreateTime(DateHelper.getTime());
            entity.setCreator(creator);
            entity.setCustNo(custNo);
            saveProcess(entity);
            cache(entity);
            return entity.getId();
        } catch(final Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new SnakerException(e.getMessage(), e.getCause());
        }*/
    }

    /**
     * 根据流程定义id、xml的输入流解析为字节数组，保存至数据库中，并且重新put到缓存中
     * @param input 定义输入流
     */
    @Override
    public void redeploy(final String id, final InputStream input) {
        throw new AssertException("不可使用！");
        /*AssertHelper.notNull(input);
        final Process entity = access().getProcess(id);
        AssertHelper.notNull(entity);
        try {
            final byte[] bytes = StreamHelper.readBytes(input);
            final ProcessModel model = ModelParser.parse(bytes);
            final String oldProcessName = entity.getName();
            entity.setModel(model);
            entity.setBytes(bytes);
            access().updateProcess(entity);
            if(!oldProcessName.equalsIgnoreCase(entity.getName())) {
                final Cache<String, Process> entityCache = ensureAvailableEntityCache();
                if(entityCache != null) {
                    entityCache.remove(oldProcessName + DEFAULT_SEPARATOR + entity.getCustNo() + DEFAULT_SEPARATOR + entity.getVersion());
                }
            }
            cache(entity);
        } catch(final Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new SnakerException(e.getMessage(), e.getCause());
        }*/
    }

    /**
     * 根据processId卸载流程
     */
    @Override
    public void undeploy(final String id) {
        final Process entity = access().getProcess(id);
        entity.setState(STATE_FINISH);
        access().updateProcess(entity);
        cache(entity);
    }

    /**
     * 级联删除指定流程定义的所有数据
     */
    @Override
    public void cascadeRemove(final String id) {
        final Process entity = access().getProcess(id);
        final List<HistoryOrder> historyOrders = access().getHistoryOrders(null, new QueryFilter().setProcessId(id));

        for(final HistoryOrder historyOrder : historyOrders) {
            ServiceContext.getEngine().order().cascadeRemove(historyOrder.getId());
        }
        access().deleteProcess(entity);
        clear(entity);
    }

    /**
     * 查询流程定义
     */
    @Override
    public List<Process> getProcesss(QueryFilter filter) {
        if(filter == null) {
            filter = new QueryFilter();
        }
        return access().getProcesss(null, filter);
    }

    /**
     * 分页查询流程定义
     */
    @Override
    public List<Process> getProcesss(final Page<Process> page, final QueryFilter filter) {
        AssertHelper.notNull(filter);
        return access().getProcesss(page, filter);
    }

    /**
     * 缓存实体
     * @param entity 流程定义对象
     */
    protected void cache(final Process entity) {
        final Cache<String, String> nameCache = ensureAvailableNameCache();
        final Cache<String, Process> entityCache = ensureAvailableEntityCache();
        if(entity.getModel() == null) {
            entity.setModel(BetterModelParser.parse(entity.getId()));
        }
        final String processName = entity.getName() + DEFAULT_SEPARATOR  + entity.getCustNo() + DEFAULT_SEPARATOR + entity.getVersion();
        if(nameCache != null && entityCache != null) {
            if(log.isDebugEnabled()) {
                log.debug("cache process id is[{}],name is[{}]", entity.getId(), processName);
            }
            entityCache.put(processName, entity);
            nameCache.put(entity.getId(), processName);
        } else {
            if(log.isDebugEnabled()) {
                log.debug("no cache implementation class");
            }
        }
    }

    /**
     * 清除实体
     * @param entity 流程定义对象
     */
    protected void clear(final Process entity) {
        final Cache<String, String> nameCache = ensureAvailableNameCache();
        final Cache<String, Process> entityCache = ensureAvailableEntityCache();
        final String processName = entity.getName() + DEFAULT_SEPARATOR  + entity.getCustNo() + DEFAULT_SEPARATOR + entity.getVersion();
        if(nameCache != null && entityCache != null) {
            nameCache.remove(entity.getId());
            entityCache.remove(processName);
        }
    }

    @Override
    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected Cache<String, Process> ensureAvailableEntityCache() {
        Cache<String, Process> entityCache = ensureEntityCache();
        if(entityCache == null && this.cacheManager != null) {
            entityCache = this.cacheManager.getCache(CACHE_ENTITY);
        }
        return entityCache;
    }

    protected Cache<String, String> ensureAvailableNameCache() {
        Cache<String, String> nameCache = ensureNameCache();
        if(nameCache == null && this.cacheManager != null) {
            nameCache = this.cacheManager.getCache(CACHE_NAME);
        }
        return nameCache;
    }

    public Cache<String, Process> ensureEntityCache() {
        return entityCache;
    }

    public void setEntityCache(final Cache<String, Process> entityCache) {
        this.entityCache = entityCache;
    }

    public Cache<String, String> ensureNameCache() {
        return nameCache;
    }

    public void setNameCache(final Cache<String, String> nameCache) {
        this.nameCache = nameCache;
    }
}
