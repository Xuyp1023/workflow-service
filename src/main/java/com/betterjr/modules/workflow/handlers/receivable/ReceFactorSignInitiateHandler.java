package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

/**
 * 融资-发起签约
 * @author tangzw
 *
 */
@Service("receFactorSignInitiateHandler")
public class ReceFactorSignInitiateHandler implements INodeHandler{
	@Reference(interfaceClass = IScfReceApprovalFlowDubboService.class)
    private IScfReceApprovalFlowDubboService receApprovalDubboService;
	
	@Override
	public void processPass(Map<String, Object> anContext) {
		
	}

	@Override
	public void processReject(Map<String, Object> anContext) {
		
	}

	@Override
	public void processHandle(Map<String, Object> anContext) {
		receApprovalDubboService.signInitiate(anContext);
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		
	}
	

}
