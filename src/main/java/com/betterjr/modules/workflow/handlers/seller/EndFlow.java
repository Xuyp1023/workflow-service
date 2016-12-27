package com.betterjr.modules.workflow.handlers.seller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.workflow.handler.IProcessHandler;

@Service("sellerEndFlowHandler")
public class EndFlow implements IProcessHandler{
	@Reference(interfaceClass = IScfSellerApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;
	
	@Override
	public void processCancel(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("requestNo", context.get("businessId"));
		scfSellerFlowService.endFlow(parmMap, 1);
	}

	@Override
	public void processEnd(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		parmMap.put("requestNo", context.get("businessId"));
		scfSellerFlowService.endFlow(parmMap, 2);
	}

}
