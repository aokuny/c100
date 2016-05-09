package com.ihandy.quote_core.bean;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
public class Request {
    private Map requestParam;
    private String Url;

    public Map getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(Map requestParam) {
        this.requestParam = requestParam;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

}
