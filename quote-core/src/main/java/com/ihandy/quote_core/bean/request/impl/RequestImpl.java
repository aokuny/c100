package com.ihandy.quote_core.bean.request.impl;

import com.ihandy.quote_common.HttpUtils;
import com.ihandy.quote_core.bean.linkedList.Request;
import com.ihandy.quote_core.bean.request.IRequest;
import com.ihandy.quote_core.bean.response.IResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
@Service
public class RequestImpl implements IRequest {

    @Autowired
    private IResponseService responseService;


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
            returnMap = HttpUtils.sendPost(request.getUrl(),request.getRequestParam().toString());
        }else{
            returnMap = HttpUtils.sendPost(request.getUrl(),null);
        }
        //2、解析返回Map,将Map信息分离出 下一页面需要参数和最终结果需要信息
        Map map = new HashMap();
        map = ParseCarInfo(nodeId,returnMap);
        return map;
    }

    private Map ParseCarInfo( int nodeId , Map<String, String> returnMap) {
        // TODO: 2016/5/8  将从页面解析出来的信息中分类
        // 1、返回信息中有下次页面请求需要的参数，返回nextParam map
        // 2、返回信息中有最终返回map中需要的结果, 返回response map
        Map map = responseService.parseCarInfoByNodeId(nodeId,returnMap);
        return map;
    }
}
