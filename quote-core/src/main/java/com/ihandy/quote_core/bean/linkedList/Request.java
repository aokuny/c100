package com.ihandy.quote_core.bean.linkedList;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
public class Request {
    private Map requestParam;
    private String Url;
    private String sessionId;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
