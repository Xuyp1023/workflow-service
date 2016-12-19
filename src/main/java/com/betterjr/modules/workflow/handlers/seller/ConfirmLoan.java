package com.betterjr.modules.workflow.handlers.seller;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("sellerConfirmLoanHandler")
public class ConfirmLoan implements INodeHandler{
	
    @Reference(interfaceClass = IScfSellerApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;
    
    @Override
	public void processPass(Map<String, Object> context) {
		scfSellerFlowService.confirmLoan(context, 1);
	}

	@Override
	public void processReject(Map<String, Object> context) {
		scfSellerFlowService.confirmLoan(context, 2);
	}
    
	@Override
	public void processHandle(Map<String, Object> anContext) {
		// 业务办理
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		// 暂存
		
	}


}
