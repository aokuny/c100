package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.bean.SortedHSLinkedList;

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
