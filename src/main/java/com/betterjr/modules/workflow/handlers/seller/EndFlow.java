package com.betterjr.modules.workflow.handlers.seller;

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
		scfSellerFlowService.endFlow(context, 1);
		
	}

	@Override
	public void processEnd(Map<String, Object> context) {
		scfSellerFlowService.endFlow(context, 2);
	}

}
