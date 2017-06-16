package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

/**
 * 融资-签约审核
 * 
 * @author tangzw
 *
 */
@Service("receFactorSignReviewHandler")
public class ReceFactorSignReviewHandler implements INodeHandler {

	@Reference(interfaceClass = IScfReceApprovalFlowDubboService.class)
    private IScfReceApprovalFlowDubboService receApprovalDubboService;
	
	@Override
	public void processPass(Map<String, Object> anContext) {
		receApprovalDubboService.signReview((Map<String, Object>)anContext.get("INPUT"), 1);	
	}

	@Override
	public void processReject(Map<String, Object> anContext) {
		receApprovalDubboService.signReview((Map<String, Object>)anContext.get("INPUT"), 0);			
	}

	@Override
	public void processHandle(Map<String, Object> anContext) {
	}

	@Override
	public void processSave(Map<String, Object> anContext) {
		
	}

}
