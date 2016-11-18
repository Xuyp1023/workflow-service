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
     * 布署流程
     *
     * @param anId
     * @return
     */
    public WorkFlowBase saveDeploy(final Long anId) {
        // 检查是入参是否正确

        // 检查是否为待发布状态

        // 置为已发布

        // 回写已发布的流程id
        return null;
    }

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
     * 根据流程名称和公司编号查找流程定义
     *
     * @param anName
     *            流程名称
     * @param anCustNo
     *            公司编号
     * @return
     */
    public WorkFlowBase findWorkFlowBaseByName(final String anName, final Long anCustNo) {
        final Map<String, Object> conditionMap = new HashMap<>();

        conditionMap.put("name", anName);
        conditionMap.put("custNo", anCustNo);
        conditionMap.put("isLast", WorkFlowConstants.IS_LAST);

        final Collection<WorkFlowBase> workFlowBases = this.selectByProperty(conditionMap);

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

        // 检查是否已经存在已有版本
        final WorkFlowBase workFlowBaseLast = findWorkFlowBaseByName(anWorkFlowBase.getName(), anCustNo);
        // 如果有表明此流程已经存在自定义版本
        if (workFlowBaseLast == null) { // 初始版本 新增 version 0
            anWorkFlowBase.setVersion(0L);

            anWorkFlowBase.initAddValue(workFlowBaseDefault);

            this.insert(anWorkFlowBase);
            // 找default流程 TODO 做 COPY
            workFlowNodeService.saveCopyWorkFlowNode(workFlowBaseLast, anWorkFlowBase);
        }
        else { // 版本+1 查找，如果已经有了一个未发布的流程，不允许修改
            final Long version = workFlowBaseLast.getVersion() + 1;
            final WorkFlowBase workFlowBase = findWorkFlowBaseByVersion(anWorkFlowBase.getName(), anCustNo, version);
            if (workFlowBase == null) { // 如果已有使用最后版本+1 新增
                anWorkFlowBase.setVersion(version);

                anWorkFlowBase.initAddValue(workFlowBaseLast);

                this.insert(anWorkFlowBase);
                // 基于最后版本 TODO 做 COPY 基于最新节点来做 COPY 然后合并 最后发布节点
                workFlowNodeService.saveCopyWorkFlowNode(workFlowBase, anWorkFlowBase);
            }
            else { // 如果已有一个未发布的流程，则不允许新增，只允许修改
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
        BTAssert.notNull(anWorkFlowBase.getName());
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
                && BetterStringUtils.equals(workFlowBase.getIsLast(), WorkFlowConstants.NOT_LAST)
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
     * 发布一个流程
     *
     * @param anWorkFlowBaseId
     *            待发布流程编号
     * @return
     */
    public WorkFlowBase savePublishWorkFlow(final Long anBaseId) {
        // 检查是否是已经存在的未发布流程
        // 检查是否已经存在已有版本
        final WorkFlowBase workFlowBase = checkWorkFlowBase(anBaseId);

        // 修改为已发布流程
        workFlowBase.setIsLast(WorkFlowConstants.IS_LAST);
        workFlowBase.setIsPublished(WorkFlowConstants.IS_PUBLISHED);
        workFlowBase.initModifyValue();

        this.updateByPrimaryKeySelective(workFlowBase);

        // 将上一版本 is_last 改为 false
        if (workFlowBase.getVersion().equals(0L) == false) {
            final WorkFlowBase workFlowBasePrevious = findWorkFlowBaseByVersion(workFlowBase.getName(), workFlowBase.getCustNo(),
                    workFlowBase.getVersion() - 1L);// 获取上一版本
            BTAssert.notNull(workFlowBasePrevious, "没有找到上一版本流程！");

            workFlowBasePrevious.setIsLast(WorkFlowConstants.NOT_LAST);
            workFlowBasePrevious.initModifyValue();

            this.updateByPrimaryKeySelective(workFlowBasePrevious);
        }

        // TODO 发布到 snaker中

        return workFlowBase;
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

        if (BetterStringUtils.equals(workFlowBase.getIsLast(), WorkFlowConstants.IS_LAST)
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
