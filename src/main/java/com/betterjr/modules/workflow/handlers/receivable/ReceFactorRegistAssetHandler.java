package com.betterjr.modules.workflow.handlers.receivable;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.betterjr.modules.approval.IScfReceApprovalFlowDubboService;
import com.betterjr.modules.workflow.handler.INodeHandler;

/**
 * 融资-登记资产
 * 
 * @author tangzw
 *
 */
@Service("receFactorRegistAssetHandler")
public class ReceFactorRegistAssetHandler implements INodeHandler {

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
        receApprovalDubboService.registerAsset((Map<String, Object>) anContext.get("INPUT"));
    }

    @Override
    public void processSave(Map<String, Object> anContext) {

    }

}
