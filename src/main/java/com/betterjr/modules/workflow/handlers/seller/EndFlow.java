package com.betterjr.modules.workflow.handlers.seller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.workflow.entity.WorkFlowBusiness;
import com.betterjr.modules.workflow.handler.IProcessHandler;

@Service("sellerEndFlowHandler")
public class EndFlow implements IProcessHandler{
    @Reference(interfaceClass = IScfSellerApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.IProcessHandler#processStart(java.util.Map)
     */
    @Override
    public void processStart(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

	
	@Override
	public void processCancel(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) context.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSellerFlowService.endFlow(parmMap, 1);
	}

	@Override
	public void processEnd(Map<String, Object> context) {
		Map<String, Object> parmMap = new HashMap<String, Object>();
		WorkFlowBusiness business = (WorkFlowBusiness) context.get("BUSINESS");
		parmMap.put("requestNo", business.getBusinessId());
		scfSellerFlowService.endFlow(parmMap, 2);
	}
	
}
