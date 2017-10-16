package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("receCustAppliactonHandler")
public class ReceCustAppliactonHandler implements INodeHandler {
    @Reference(interfaceClass = IScfReceApprovalFlowDubboService.class)
    private IScfReceApprovalFlowDubboService scfReceApprovalFlowDubboService;

    @Override
    public void processPass(Map<String, Object> anContext) {
        scfReceApprovalFlowDubboService.application((Map<String, Object>) anContext.get("INPUT"));
    }

    @Override
    public void processReject(Map<String, Object> anContext) {

    }

    @Override
    public void processHandle(Map<String, Object> anContext) {
        scfReceApprovalFlowDubboService.application((Map<String, Object>) anContext.get("INPUT"));
    }

    @Override
    public void processSave(Map<String, Object> anContext) {

    }

}
