package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

@Service("receCoreConfirmTradingBackgrand")
public class RreceCoreConfirmTradingBackgrand implements INodeHandler {
	@Reference(interfaceClass = IScfReceApprovalFlowDubboService.class)
    private IScfReceApprovalFlowDubboService receCoreApprovalDubboService;
	
	@Override
	public void processPass(Map<String, Object> anContext) {
		receCoreApprovalDubboService.confirmTradingBackgrand(anContext, 1);
	}

	@Override
	public void processReject(Map<String, Object> anContext) {
		receCoreApprovalDubboService.confirmTradingBackgrand(anContext, 0);
	}

	@Override
	public void processHandle(Map<String, Object> anContext) {

	}

	@Override
	public void processSave(Map<String, Object> anContext) {

	}

}
