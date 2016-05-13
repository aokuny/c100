package com.ihandy.quote_core.bean;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/7.
 */
public class Response {
    private Map responseMap;
    private int ReturnCode;
    private String ErrMsg;

    public Map getResponseMap() {
        return responseMap;
    }

    public void setResponseMap(Map responseMap) {
        this.responseMap = responseMap;
    }

    public int getReturnCode() {
        return ReturnCode;
    }

    public void setReturnCode(int returnCode) {
        ReturnCode = returnCode;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        ErrMsg = errMsg;
    }
}
