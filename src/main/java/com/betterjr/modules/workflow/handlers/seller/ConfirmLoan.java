package com.betterjr.modules.workflow.handlers.seller;

import java.util.HashMap;
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
	public void processPass(Map<String, Object> anContext) {
	}

	@Override
	public void processReject(Map<String, Object> anContext) {
		scfSellerFlowService.confirmLoan(formartToString((Map<String, Object>)anContext.get("INPUT")), 2);
	}
    
	@Override
	public void processHandle(Map<String, Object> anContext) {
		scfSellerFlowService.confirmLoan(formartToString((Map<String, Object>)anContext.get("INPUT")), 1);
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		// 暂存
		
	}

	private Map<String, Object> formartToString(Map<String, Object> anContext){
		Map<String, Object> parm = new HashMap<String, Object>();
		for(Map.Entry<String, Object> entry: anContext.entrySet()){    
			parm.put(entry.getKey(), entry.getValue().toString().replaceAll("-", ""));
		}
		return parm;
	} 


}
