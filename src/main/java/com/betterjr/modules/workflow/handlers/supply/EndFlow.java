package com.betterjr.modules.workflow.handlers.supply;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.handler.IProcessHandler;

@Service("supplyEndFlowHandler")
public class EndFlow implements IProcessHandler{
	@Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;
	
	@Override
	public void processCancel(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) context.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSupplyFlowService.endFlow(parmMap, 1);
	}

	@Override
	public void processEnd(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) context.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSupplyFlowService.endFlow(parmMap, 2);
	}
	
}
