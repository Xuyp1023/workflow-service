package com.betterjr.modules.workflow.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import com.betterjr.common.annotation.BetterjrMapper;
import com.betterjr.mapper.common.Mapper;
import com.betterjr.mapper.pagehelper.Page;
import com.betterjr.modules.workflow.data.WorkFlowBaseData;
import com.betterjr.modules.workflow.entity.WorkFlowBase;

@BetterjrMapper
public interface WorkFlowBaseMapper extends Mapper<WorkFlowBase> {
    @Select("SELECT C_NAME as name, L_CUSTNO as custNo, COUNT(*) as count, MAX(N_VERSION) as maxVersion FROM t_sys_wf_base WHERE L_CUSTNO=#{custNo} AND ID > 0 GROUP BY C_NAME")
    @ResultType(WorkFlowBaseData.class)
    public Page<WorkFlowBaseData> queryWorkFlowBaseByCustNo(@Param("custNo") Long custNo);
}