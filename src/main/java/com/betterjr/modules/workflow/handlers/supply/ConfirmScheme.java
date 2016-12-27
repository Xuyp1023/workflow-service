package com.betterjr.modules.workflow.handlers.supply;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("supplyConfirmSchemeHandler")
public class ConfirmScheme implements INodeHandler {
    @Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;
   
    @Override
	public void processPass(Map<String, Object> context) {
    	
	}

	@Override
	public void processReject(Map<String, Object> context) {
		scfSupplyFlowService.confirmScheme(context, 2);
	}
	
	@Override
	public void processHandle(Map<String, Object> anContext) {
		scfSupplyFlowService.confirmScheme((Map<String, Object>)anContext.get("INPUT"), 1);
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		// TODO Auto-generated method stub
		
	}

}