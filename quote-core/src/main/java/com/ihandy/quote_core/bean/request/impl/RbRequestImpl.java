package com.ihandy.quote_core.bean.request.impl;

import com.ihandy.quote_common.HttpsUtil;
import com.ihandy.quote_core.bean.linkedList.Request;
import com.ihandy.quote_core.bean.request.IRbRequest;
import com.ihandy.quote_core.bean.response.IRbResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
@Service
public class RbRequestImpl implements IRbRequest {

    @Autowired
    private IRbResponseService rbResponseService;


    public Request combineRequest(String url) {
        Request request =new Request();
        request.setUrl(url);
        request.setRequestParam(null);
        return request;
    }

    public Map<String, Object> sendPostAndParseResponseForCarInfo(int nodeId , Request request) {
        Map<String, String> returnMap = null;
        //1、httpUtils 请求页面,返回map;
        if(null!=request && null!=request.getRequestParam()){//参数非空
            returnMap = HttpsUtil.sendPostHttps(request.getUrl(),request.getRequestParam().toString(),request.getSessionId());
        }else{
            returnMap = HttpsUtil.sendPostHttps(request.getUrl(),null,request.getSessionId());
        }
        // 2、返回信息中有下次页面请求需要的参数，返回nextParam map
        // 3、返回信息中有最终返回map中需要的结果, 返回response map
        Map map = rbResponseService.parseCarInfoByNodeId(nodeId,returnMap);
        return map;

    }


}
