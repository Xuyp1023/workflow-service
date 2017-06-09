package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

/**
 * 融资-风控审批
 * @author tangzw
 *
 */
@Service("receFactorRiskControlHandler")
public class ReceFactorRiskControlHandler implements INodeHandler {
	@Reference(interfaceClass = IScfReceApprovalFlowDubboService.class)
    private IScfReceApprovalFlowDubboService receApprovalDubboService;
	
	@Override
	public void processPass(Map<String, Object> anContext) {
		receApprovalDubboService.riskControl((Map<String, Object>)anContext.get("INPUT"), 1);
	}

	@Override
	public void processReject(Map<String, Object> anContext) {
		receApprovalDubboService.riskControl((Map<String, Object>)anContext.get("INPUT"), 1);
	}

	@Override
	public void processHandle(Map<String, Object> anContext) {
		
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		
	}


}