package com.ihandy.quote_core.utils;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/11.
 */
public abstract  class BasePage  implements  IPage{
    public String loginSessionId;
    public String yewuSessionId;

    public String doRequest(Request request) {
        return  null;
    }

    public Map getResponse(String html) {
        return null;
    }

    public Response run() {
        yewuSessionId = "";
        return null;
    }
}
