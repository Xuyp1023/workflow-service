// Copyright (c) 2014-2016 Bytter. All rights reserved.
// ============================================================================
// CURRENT VERSION
// ============================================================================
// CHANGE LOG
// V2.0 : 2016年11月18日, liuwl, creation
// ============================================================================
package com.betterjr.modules.workflow;

import java.util.List;

import org.junit.Test;

import com.betterjr.common.data.SimpleDataEntity;
import com.betterjr.modules.BasicServiceTest;
import com.betterjr.modules.workflow.service.WorkFlowCategoryService;

/**
 * @author liuwl
 *
 */
public class WorkFlowCategoryServiceTestCase extends BasicServiceTest<WorkFlowCategoryService> {

    /*
     * (non-Javadoc)
     *
     * @see com.betterjr.modules.BasicServiceTest#getTargetServiceClass()
     */
    @Override
    public Class<WorkFlowCategoryService> getTargetServiceClass() {
        return WorkFlowCategoryService.class;
    }

    @Test
    public void queryCategoryTest() {
        final WorkFlowCategoryService workFlowCategoryService = this.getServiceObject();

        final List<SimpleDataEntity> categories = workFlowCategoryService.queryWorkFlowCategory();

        categories.forEach(System.out::println);
    }
}
