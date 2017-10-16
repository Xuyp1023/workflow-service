// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月14日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.common.service.BaseService;
import com.betterjr.modules.workflow.dao.WorkFlowCategoryMapper;
import com.betterjr.modules.workflow.entity.WorkFlowCategory;

/**
 * @author liuwl
 *
 */
@Service
public class WorkFlowCategoryService extends BaseService<WorkFlowCategoryMapper, WorkFlowCategory> {
    /**
     * 查询流程分类
     *
     * @return
     */
    public List<SimpleDataEntity> queryWorkFlowCategory() {
        // 查询分类
        return this.selectAll().stream()
                .map(category -> new SimpleDataEntity(String.valueOf(category.getId()), category.getName()))
                .collect(Collectors.toList());
    }
}
