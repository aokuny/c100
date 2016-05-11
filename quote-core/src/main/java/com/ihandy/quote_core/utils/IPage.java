package com.ihandy.quote_core.utils;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/11.
 */
public interface IPage {
    public String doRequest(Request request);

    public Map getResponse(String html);

    public Response run();

}
