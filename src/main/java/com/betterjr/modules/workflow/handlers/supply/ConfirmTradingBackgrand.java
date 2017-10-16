package com.betterjr.modules.workflow.handlers.supply;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfSupplyApprovalService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("supplyConfirmTradingBackgrandHandler")
public class ConfirmTradingBackgrand implements INodeHandler {
    @Reference(interfaceClass = IScfSupplyApprovalService.class)
    private IScfSupplyApprovalService scfSupplyFlowService;

    @Override
    public void processPass(Map<String, Object> anContext) {

    }

    @Override
    public void processReject(Map<String, Object> anContext) {
        scfSupplyFlowService.confirmTradingBackgrand((Map<String, Object>) anContext.get("INPUT"), 2);
    }

    @Override
    public void processHandle(Map<String, Object> anContext) {
        scfSupplyFlowService.confirmTradingBackgrand((Map<String, Object>) anContext.get("INPUT"), 1);
    }

    @Override
    public void processSave(Map<String, Object> anContext) {
        // TODO Auto-generated method stub

    }

}
