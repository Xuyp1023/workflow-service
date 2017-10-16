package com.betterjr.modules.workflow.handlers.seller;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSellerApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("sellerRequestTradingBackgrandHandler")
public class RequestTradingBackgrand implements INodeHandler {
    @Reference(interfaceClass = IScfSellerApprovalService.class)
    private IScfSellerApprovalService scfSellerFlowService;

    @Override
    public void processPass(Map<String, Object> anContext) {

    }

    @Override
    public void processReject(Map<String, Object> anContext) {
        scfSellerFlowService.requestTradingBackgrand((Map<String, Object>) anContext.get("INPUT"), 2);
    }

    @Override
    public void processHandle(Map<String, Object> anContext) {
        scfSellerFlowService.requestTradingBackgrand((Map<String, Object>) anContext.get("INPUT"), 1);
    }

    @Override
    public void processSave(Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

}
