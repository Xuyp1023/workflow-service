package com.betterjr.modules.workflow.handlers.seller;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("sellerConfirmTradingBackgrandHandler")
public class ConfirmTradingBackgrand implements INodeHandler  {
	@Reference(interfaceClass = IScfSellerApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;
	
	@Override
	public void processPass(Map<String, Object> context) {
		scfSellerFlowService.confirmTradingBackgrand(context, 1);
	}
	
	@Override
	public void processReject(Map<String, Object> context) {
		scfSellerFlowService.confirmTradingBackgrand(context, 2);
	}
	
	@Override
	public void processHandle(Map<String, Object> anContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		// TODO Auto-generated method stub
		
	}


}
