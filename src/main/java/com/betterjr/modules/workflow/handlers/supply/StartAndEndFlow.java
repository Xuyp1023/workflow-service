package com.betterjr.modules.workflow.handlers.supply;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.data.WorkFlowBusinessType;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.handler.IProcessHandler;

@Service("supplyEndFlowHandler")
public class StartAndEndFlow implements IProcessHandler {
    @Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;

    @Override
    public void processStart(final Map<String, Object> anContext) {
    	Map<String, Object> parmMap = formartToString((Map<String, Object>)anContext.get("INPUT"));
    	parmMap = scfSupplyFlowService.application(parmMap);
    	
    	anContext.put("businessId", parmMap.get("requestNo"));
    	anContext.put("businessType", WorkFlowBusinessType.SUPPLIER_FINANCING_REQUEST);
    	
    	Map<String, Object> flowMap = new HashMap<String, Object>();
    	flowMap.put("balance", parmMap.get("balance"));
    	
    	anContext.put("param", flowMap);
    }
	
	@Override
	public void processCancel(Map<String, Object> anContext) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) anContext.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSupplyFlowService.endFlow(parmMap, 1);
	}

	@Override
	public void processEnd(Map<String, Object> anContext) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) anContext.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSupplyFlowService.endFlow(parmMap, 2);
	}

	private Map<String, Object> formartToString(Map<String, Object> anContext){
		Map<String, Object> parm = new HashMap<String, Object>();
		for(Map.Entry<String, Object> entry: anContext.entrySet()){    
			parm.put(entry.getKey(), entry.getValue().toString().replaceAll("-", ""));
		}
		return parm;
	} 
}
