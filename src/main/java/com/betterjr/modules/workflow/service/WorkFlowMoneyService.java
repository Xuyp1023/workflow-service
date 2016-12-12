// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.common.exception.BytterException;
import com.betterjr.common.service.BaseService;
import com.betterjr.common.utils.BTAssert;
import com.betterjr.common.utils.BetterStringUtils;
import com.betterjr.common.utils.Collections3;
import com.betterjr.modules.workflow.constants.WorkFlowConstants;
import com.betterjr.modules.workflow.dao.WorkFlowMoneyMapper;
import com.betterjr.modules.workflow.entity.WorkFlowBase;
import com.betterjr.modules.workflow.entity.WorkFlowMoney;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowMoneyService extends BaseService<WorkFlowMoneyMapper, WorkFlowMoney> {
    private final static String MONEY_SECTION_PATTERN_STR = "^0,[\\d,]+-1$";
    private final static Pattern MONEY_SECTION_PATTERN = Pattern.compile(MONEY_SECTION_PATTERN_STR);

    @Inject
    private WorkFlowBaseService workFlowBaseService;

    /**
     * 保存流程金额段
     *
     * @param anBaseId
     * @param anMoneySection
     * @return
     */
    public String saveWorkFlowMoneySection(final Long anBaseId, final String anMoneySection) {
        // 校验金额段字符串是否有效 并返回 workFlowMoneys
        final List<WorkFlowMoney> workFlowMoneys = checkMoneySection(anMoneySection);

        // 检查流程是否存在
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到流程");
        // 检查是否为未发布流程 (不能修改已发布的流程)
        if (BetterStringUtils.equals(workFlowBase.getIsPublished(), WorkFlowConstants.IS_PUBLISHED)) {
            throw new BytterException("已发布流程不允许修改！");
        }

        // 检查当前流程是否有金额段
        saveDelWorkFlowMoneyByBaseId(anBaseId);

        // 将当前金额段添加保存
        for (final WorkFlowMoney workFlowMoney : workFlowMoneys) {
            workFlowMoney.initAddValue(anBaseId);
            this.insert(workFlowMoney);
        }

        // 返回金额段字符串
        return anMoneySection;
    }

    /**
     * 删除金额段
     *
     * @param anBaseId
     */
    private void saveDelWorkFlowMoneyByBaseId(final Long anBaseId) {
        // 读取当前流程金额段
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("baseId", anBaseId);

        this.deleteByExample(conditionMap);
    }

    /**
     * @param anMoneySection
     */
    private List<WorkFlowMoney> checkMoneySection(final String anMoneySection) {
        if (BetterStringUtils.isBlank(anMoneySection)) {
            return Collections.EMPTY_LIST;
        }
        // 0,100,200,-1
        BTAssert.isTrue(MONEY_SECTION_PATTERN.matcher(anMoneySection).matches(), "金额段格式不正确！");

        // 检查大小顺序
        final String[] values = anMoneySection.split(",");

        Long previous = 0L;
        Long current = 0L;

        final List<WorkFlowMoney> workFlowMoneys = new ArrayList<>();
        for (int i = 1; i < values.length; i++) {
            final WorkFlowMoney workFlowMoney = new WorkFlowMoney();
            if (i == values.length - 1) {
                workFlowMoney.setBeginMoney(new BigDecimal(previous));
                workFlowMoney.setEndMoney(new BigDecimal(-1));
                workFlowMoney.setSeq(i - 1);
                workFlowMoneys.add(workFlowMoney);
                break;
            }
            if (i == 1) {
                previous = Long.valueOf(values[i]);
                workFlowMoney.setBeginMoney(new BigDecimal(0));
                workFlowMoney.setEndMoney(new BigDecimal(previous));
                workFlowMoney.setSeq(0);
                workFlowMoneys.add(workFlowMoney);
            }
            else {
                current = Long.valueOf(values[i]);
                if (current.longValue() <= previous) {
                    throw new BytterException("金额段数值顺序错误！");
                }
                workFlowMoney.setBeginMoney(new BigDecimal(previous));
                workFlowMoney.setEndMoney(new BigDecimal(current));
                workFlowMoney.setSeq(i - 1);
                workFlowMoneys.add(workFlowMoney);
                previous = current;
            }
        }

        return workFlowMoneys;
    }

    /**
     *
     * @param anBaseId
     * @return
     */
    public List<WorkFlowMoney> queryWorkFlowMoneyByBaseId(final Long anBaseId) {
        // 检查当前流程是否存在
        final WorkFlowBase workFlowBase = workFlowBaseService.findWorkFlowBaseById(anBaseId);
        BTAssert.notNull(workFlowBase, "没有找到流程");

        // 读取当前流程金额段
        final Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("baseId", anBaseId);

        // 将读取到的金额段 组成 金额段字符串
        final List<WorkFlowMoney> workFlowMoneys = this.selectByProperty(conditionMap, "seq ASC");

        return workFlowMoneys;
    }

    /**
     * 查询流程金额段 最后以 字符串 逗号分割形式返回
     *
     * @param anBaseId
     * @return
     */
    public String queryWorkFlowMoneySection(final Long anBaseId) {
        // 将读取到的金额段 组成 金额段字符串
        final List<WorkFlowMoney> workFlowMoneys = queryWorkFlowMoneyByBaseId(anBaseId);

        if (Collections3.isEmpty(workFlowMoneys))  {
            return "";
        }
        final StringBuilder moneySection = new StringBuilder("0,");
        for (final WorkFlowMoney workFlowMoney : workFlowMoneys) {
            moneySection.append(workFlowMoney.getEndMoney().toString()).append(",");
        }
        moneySection.delete(moneySection.length() - 1, moneySection.length());
        return moneySection.toString();
    }

    /**
     * 查询某一金额段
     *
     * @param anId
     * @return
     */
    public WorkFlowMoney findWorkFlowMoney(final Long anId) {
        BTAssert.notNull(anId, "金额段编号不允许为空！");
        return this.selectByPrimaryKey(anId);
    }

    /**
     * 查询流程金额段 以 SimpleDataEntity形式返回
     *
     * @param anBaseId
     * @return
     */
    public List<SimpleDataEntity> queryWorkFlowMoney(final Long anBaseId) {
        // 读取当前流程金额段
        return queryWorkFlowMoneyByBaseId(anBaseId).stream().map(workFlowMoney -> new SimpleDataEntity(String.valueOf(workFlowMoney.getId()),
                workFlowMoney.getBeginMoney() + " - " + workFlowMoney.getEndMoney())).collect(Collectors.toList());
    }

    /**
     * copy到新的流程上 并且将copy关系映射 保留下来
     *
     * @param anTargetBase
     * @param anSourceBase
     * @param anWorkFlowMoneyMapping
     */
    public void saveCopyWorkFlowMoney(final WorkFlowBase anSourceBase, final WorkFlowBase anTargetBase,
            final Map<Long, Long> anWorkFlowMoneyMapping) {
        queryWorkFlowMoneyByBaseId(anSourceBase.getId()).forEach(tempWorkFlowMoney -> {
            final WorkFlowMoney workFlowMoney = new WorkFlowMoney();
            workFlowMoney.initCopyMoney(tempWorkFlowMoney);
            workFlowMoney.setBaseId(anTargetBase.getId());

            this.insert(workFlowMoney);
            anWorkFlowMoneyMapping.put(tempWorkFlowMoney.getId(), workFlowMoney.getId());
        });
    }
}
