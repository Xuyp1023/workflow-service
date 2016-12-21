package com.betterjr.modules.workflow.handlers.supply;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("supplyConfirmLoanHandler")
public class ConfirmLoan implements INodeHandler{
	
    @Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;
    
    @Override
	public void processPass(Map<String, Object> context) {
		scfSupplyFlowService.confirmLoan(context, 1);
	}

	@Override
	public void processReject(Map<String, Object> context) {
		scfSupplyFlowService.confirmLoan(context, 2);
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
