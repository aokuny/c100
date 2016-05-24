package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import org.apache.log4j.Logger;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveCheckEngageTimePage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveCheckEngageTimePage.class);

    public HebaoSaveCheckEngageTimePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        return null;
    }

    @Override
    public Response getResponse(String html) {
        return null;
    }

    @Override
    public Response run(Request request) {
        String html = doRequest(request);
        Response response = getResponse(html);
        return response;
    }
}
