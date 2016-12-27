package com.betterjr.modules.workflow.handlers.supply;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("supplyOfferSchemeHandler")
public class OfferScheme implements INodeHandler{
	@Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;
	
	@Override
	public void processPass(Map<String, Object> anContext) {
		
	}
	
	@Override
	public void processReject(Map<String, Object> anContext) {
		scfSupplyFlowService.offerScheme((Map<String, Object>)anContext.get("INPUT"), 2);
	}

	@Override
	public void processHandle(Map<String, Object> anContext) {
		Map<String, Object> parm = (Map<String, Object>)anContext.get("INPUT");
		System.out.println(parm.get("requestNo"));
		anContext = scfSupplyFlowService.offerScheme(formartToString(parm), 1);
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		// TODO Auto-generated method stub
		
	}

	private Map<String, Object> formartToString(Map<String, Object> anContext){
		Map<String, Object> parm = new HashMap<String, Object>();
		Set set = anContext.entrySet();
		for(Map.Entry<String, Object> entry: anContext.entrySet()){    
			parm.put(entry.getKey(), entry.getValue().toString());
		}
		return parm;
	} 
}
