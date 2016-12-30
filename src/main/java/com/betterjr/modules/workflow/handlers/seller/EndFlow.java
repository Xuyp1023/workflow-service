package com.betterjr.modules.workflow.handlers.seller;

import java.util.HashMap;
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
    public void processCancel(final Map<String, Object> context) {
        final Map<String, Object> parmMap = new HashMap<String, Object>();
        parmMap.put("requestNo", context.get("businessId"));
        scfSellerFlowService.endFlow(parmMap, 1);
    }

    @Override
    public void processEnd(final Map<String, Object> context) {
        final Map<String, Object> parmMap = new HashMap<String, Object>();
        parmMap.put("requestNo", context.get("businessId"));
        scfSellerFlowService.endFlow(parmMap, 2);
    }

    /* (non-Javadoc)
     * @see com.betterjr.modules.workflow.handler.IProcessHandler#processStart(java.util.Map)
     */
    @Override
    public void processStart(final Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

}
