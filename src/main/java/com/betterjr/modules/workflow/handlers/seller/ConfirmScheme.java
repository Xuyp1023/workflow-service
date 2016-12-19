package com.betterjr.modules.workflow.handlers.seller;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("sellerConfirmSchemeHandler")
public class ConfirmScheme implements INodeHandler {
    @Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;
   
    @Override
	public void processPass(Map<String, Object> context) {
    	scfSellerFlowService.confirmScheme(context, 1);
	}

	@Override
	public void processReject(Map<String, Object> context) {
		scfSellerFlowService.confirmScheme(context, 2);
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
