package com.ihandy.quote_core.bean.request;

import com.ihandy.quote_core.bean.linkedList.Request;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
public interface IRequest {

    public Request combineRequestBrowsePolicyNo();

    public Request combineRequestQueryPolicyList();

    Request combineRequest(String url);

    Map<String,Object> sendPostAndParseResponseForCarInfo( int nodeId,Request request);


}
