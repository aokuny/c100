package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.utils.BasePage;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/11.
 */
public class XubaoAPage extends BasePage {

    public String doRequest(Request request) {
        String id = super.yewuSessionId;
        String url = request.getUrl();
        Map param = request.getRequestParam();
        //TODO
        return  null;
    }

    public Map getResponse(String html) {
        return null;
    }

    public Map run(Request request) {
        String html = doRequest(request);
        return getResponse(html);
    }

}
