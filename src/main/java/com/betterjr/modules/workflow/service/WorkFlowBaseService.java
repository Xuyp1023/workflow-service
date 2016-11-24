// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.common.utils.UserUtils;
import com.betterjr.modules.cert.entity.CustCertRule;
import com.betterjr.modules.workflow.constant.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowBaseMapper;
import com.betterjr.modules.workflow.entity.WorkFlowBase;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowBaseService extends BaseService<WorkFlowBaseMapper, WorkFlowBase> {

    @Inject
    private WorkFlowNodeService workFlowNodeService;

    /**
     * 查询基础流程列表(未创建的)
     *
     * @return
     */
    public List<SimpleDataEntity> queryDefaultWorkFlow() {
        final CustCertRule custCertRule = Collections3.getFirst(UserUtils.getCertInfo().getCertRuleList());
        BTAssert.notNull(custCertRule, "没有找到相应的证书角色！");
        return queryDefaultWorkFlow(custCertRule.getRule());
    }

    /**
     * 查询基础流程列表(未创建的)
     *
     * @return
     */
    public List<SimpleDataEntity> queryDefaultWorkFlow(final String anOperRole) {
        // 取当前公司operRole
        BTAssert.isTrue(BetterStringUtils.isNotBlank(anOperRole), "机构ROLE不允许为空");

        // 通过operRole 和 isDefault 属性查询当前机构角色所拥有的 基础流程
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("operRole", anOperRole);
        conditionMap.put("isDefault", WorkFlowConstants.IS_DEFAULT);

        final List<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap);

        // 返回 未创建的 默认流程
        // TODO
        return workFlowBases.stream().map(base -> new SimpleDataEntity(String.valueOf(base.getId()), base.getName())).collect(Collectors.toList());
    }

    /**
     *
     * @param anName
     * @param anCustNo
     * @return
     */
    public WorkFlowBase findWorkFlowBaseLatestByName(final String anName, final Long anCustNo) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("custNo", anCustNo);
        conditionMap.put("isLatest", WorkFlowConstants.IS_LATEST);

        final Collection<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap);

        return Collections3.getFirst(workFlowBases);
    }

    /**
     * 根据流程名称和公司编号查找流程定义
     *
     * @param anName
     *            流程名称
     * @param anCustNo
     *            公司编号
     * @return
     */
    public WorkFlowBase findWorkFlowBaseLastByName(final String anName, final Long anCustNo) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("custNo", anCustNo);

        final Collection<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap, "version DESC");

        return Collections3.getFirst(workFlowBases);
    }

    /**
     * 根据流程名称查找模板流程
     *
     * @param anName
     *            流程名称
     * @param anCustNo
     *            公司编号
     * @return
     */
    public WorkFlowBase findDefaultWorkFlowBaseByName(final String anName) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("isDefault", WorkFlowConstants.IS_DEFAULT);

        final Collection<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap);

        return Collections3.getFirst(workFlowBases);
    }

    /**
     * 通过 name custNo version查找流程定义
     *
     * @param anName
     * @param anCustNo
     * @param anL
     * @return
     */
    public WorkFlowBase findWorkFlowBaseByVersion(final String anName, final Long anCustNo, final long anVersion) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("custNo", anCustNo);
        conditionMap.put("version", anVersion);

        final Collection<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap);

        return Collections3.getFirst(workFlowBases);
    }

    /**
     * 通过 id 查找流程定义
     *
     * @param anId
     * @return
     */
    public WorkFlowBase findWorkFlowBaseById(final Long anId) {
        return this.selectByPrimaryKey(anId);
    }

    /**
     * 通过 processId 查找流程定义
     *
     * @param anProcessId
     * @return
     */
    public WorkFlowBase findWorkFlowBaseByProcessId(final String anProcessId) {
        BTAssert.isTrue(BetterStringUtils.isNotBlank(anProcessId), "processId 不允许为空！");
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("processId", anProcessId);

        return Collections3.getFirst(this.selectByProperty(conditionMap));
    }

    /**
     * 添加一个基础流程
     *
     * @param anWorkFlowBase
     *            基础流程实例
     * @return
     */
    public WorkFlowBase addWorkFlowBase(final WorkFlowBase anWorkFlowBase, final Long anDefaultBaseId, final Long anCustNo) {
        BTAssert.notNull(anDefaultBaseId, "基础流程编号不允许为空！");
        BTAssert.notNull(anCustNo, "公司编号不允许为空！");

        checkWorkFlowBase(anWorkFlowBase);

        final WorkFlowBase workFlowBaseDefault = findWorkFlowBaseById(anDefaultBaseId);
        BTAssert.notNull(workFlowBaseDefault, "模板流程没有找到！");

        anWorkFlowBase.setName(workFlowBaseDefault.getName());// 这个不允许改变 保持与默认模板一致
        anWorkFlowBase.setCustNo(anCustNo);
        anWorkFlowBase.setIsDefault(WorkFlowConstants.NOT_DEFAULT);
        anWorkFlowBase.setIsDisabled(WorkFlowConstants.NOT_DISABLED);
        anWorkFlowBase.setIsLatest(WorkFlowConstants.NOT_LATEST);

        // 检查是否已经存在已有版本
        final WorkFlowBase workFlowBaseLast = findWorkFlowBaseLastByName(anWorkFlowBase.getName(), anCustNo);
        // 如果有表明此流程已经存在自定义版本
        if (workFlowBaseLast == null) { // 初始版本 新增 version 0
            anWorkFlowBase.setVersion(0L);

            anWorkFlowBase.initAddValue(workFlowBaseDefault);

            this.insert(anWorkFlowBase);

            workFlowNodeService.saveCopyWorkFlowNode(workFlowBaseDefault, anWorkFlowBase);
        }
        else {
            if (BetterStringUtils.equals(workFlowBaseLast.getIsLatest(), WorkFlowConstants.IS_LATEST)) { // 如果已存在最后版本 则为增加版本
                final Long version = workFlowBaseLast.getVersion() + 1;
                anWorkFlowBase.setVersion(version);

                anWorkFlowBase.initAddValue(workFlowBaseLast);

                this.insert(anWorkFlowBase);

                workFlowNodeService.saveCopyWorkFlowNode(workFlowBaseLast, anWorkFlowBase);
            }
            else {
                throw new BytterException("已经存在未发布流程，不允许继续添加！");
            }

        }
        return anWorkFlowBase;
    }

    /**
     * 校验传入的workflowbase 是否有效
     *
     * @param anWorkFlowBase
     */
    private void checkWorkFlowBase(final WorkFlowBase anWorkFlowBase) {
        BTAssert.notNull(anWorkFlowBase.getNickname());
        BTAssert.notNull(anWorkFlowBase.getOperRole());
        BTAssert.notNull(anWorkFlowBase.getCategoryId());
    }

    /**
     * 检查流程是否可操作
     *
     * @param anBaseId
     */
    public WorkFlowBase checkWorkFlowBase(final Long anBaseId) {
        // 检查是否已经存在已有版本
        BTAssert.notNull(anBaseId, "流程定义编号不允许为空");

        final WorkFlowBase workFlowBase = findWorkFlowBaseById(anBaseId);
        // 如果已有一个未发布的流程，则修改它

        if (BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.NOT_PUBLISHED)
                && BetterStringUtils.equals(workFlowBase.getIsLatest(), WorkFlowConstants.NOT_LATEST)
                && BetterStringUtils.equals(workFlowBase.getIsDefault(), WorkFlowConstants.NOT_DEFAULT)) {
            return workFlowBase;
        }
        throw new BytterException("流程状态不允许修改！");
    }

    /**
     * 修改一个基础流程
     *
     * @param anBaseId
     *            流程基础编号
     * @param anWorkFlowBase
     *            基础流程实例
     * @return
     */
    public WorkFlowBase saveWorkFlowBase(final Long anBaseId, final WorkFlowBase anWorkFlowBase) {
        // 检查是否已经存在已有版本
        final WorkFlowBase workFlowBase = checkWorkFlowBase(anBaseId);

        // 如果没有则产生一个新的未发布流程
        workFlowBase.initModifyValue();
        workFlowBase.setNickname(anWorkFlowBase.getNickname());
        workFlowBase.setCategoryId(anWorkFlowBase.getCategoryId());

        this.updateByPrimaryKeySelective(workFlowBase);

        return workFlowBase;
    }

    /**
     * 检查流程是否可操作
     *
     * @param anBaseId
     */
    public WorkFlowBase checkWorkFlowBaseByPublish(final Long anBaseId) {
        // 检查是否已经存在已有版本
        BTAssert.notNull(anBaseId, "流程定义编号不允许为空");

        final WorkFlowBase workFlowBase = findWorkFlowBaseById(anBaseId);
        // 如果已有一个未发布的流程，则修改它

        if (BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.NOT_PUBLISHED)
                && BetterStringUtils.equals(workFlowBase.getIsLatest(), WorkFlowConstants.NOT_LATEST)
                && BetterStringUtils.equals(workFlowBase.getIsDefault(), WorkFlowConstants.NOT_DEFAULT)) {
            return workFlowBase;
        }
        throw new BytterException("流程状态不允许修改！");
    }

    /**
     * 发布一个流程
     *
     * @param anWorkFlowBaseId
     *            待发布流程编号
     * @return
     */
    public WorkFlowBase savePublishWorkFlow(final Long anBaseId, final String anProcessId) {
        // 检查是否是已经存在的未发布流程
        // 检查是否已经存在已有版本
        final WorkFlowBase workFlowBase = checkWorkFlowBase(anBaseId);

        // 修改为已发布流程
        workFlowBase.setIsLatest(WorkFlowConstants.IS_LATEST);
        workFlowBase.setIsPublished(WorkFlowConstants.IS_PUBLISHED);
        workFlowBase.initModifyValue();

        // 将上一版本 is_last 改为 false
        if (workFlowBase.getVersion().equals(0L) == false) {
            final WorkFlowBase workFlowBasePrevious = findWorkFlowBaseByVersion(workFlowBase.getName(), workFlowBase.getCustNo(),
                    workFlowBase.getVersion() - 1L);// 获取上一版本
            BTAssert.notNull(workFlowBasePrevious, "没有找到上一版本流程！");

            workFlowBasePrevious.setIsLatest(WorkFlowConstants.NOT_LATEST);
            workFlowBasePrevious.initModifyValue();

            this.updateByPrimaryKeySelective(workFlowBasePrevious);
        }

        workFlowBase.setProcessId(anProcessId);

        this.updateByPrimaryKeySelective(workFlowBase);

        return workFlowBase;
    }

    /**
     * 停用一个流程 通过 processId
     *
     * @param anProcessId
     * @return
     */
    public WorkFlowBase saveDisableWorkFlow(final String anProcessId) {
        final WorkFlowBase workFlowBase = this.findWorkFlowBaseByProcessId(anProcessId);
        BTAssert.notNull(workFlowBase, "流程定义未找到!");

        return saveDisableWorkFlow(workFlowBase.getId());
    }

    /**
     * 停用一个流程 （已停用流程将不能再发起申请)
     *
     * @param anWorkFlowBaseId
     *            待停用已发布最新流程
     * @return
     */
    public WorkFlowBase saveDisableWorkFlow(final long anBaseId) {
        // 检查是否已经存在已有版本
        BTAssert.notNull(anBaseId, "流程定义编号不允许为空");

        final WorkFlowBase workFlowBase = findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到相应流程");
        // 如果已有一个未发布的流程，则修改它

        if (BetterStringUtils.equals(workFlowBase.getIsLatest(), WorkFlowConstants.IS_LATEST)
                && BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.IS_PUBLISHED)
                && BetterStringUtils.equals(workFlowBase.getIsDisabled(), WorkFlowConstants.NOT_DISABLED)) {
            // 修改流程为停用状态
            workFlowBase.setIsDisabled(WorkFlowConstants.IS_DISABLED);
            workFlowBase.initModifyValue();

            this.updateByPrimaryKeySelective(workFlowBase);
            return workFlowBase;
        }
        else {
            throw new BytterException("停用流程发生错误!");
        }
    }
}