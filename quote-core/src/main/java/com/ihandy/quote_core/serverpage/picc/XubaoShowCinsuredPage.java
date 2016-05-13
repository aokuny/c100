package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoShowCinsuredPage extends BasePage {
    @Override
    public String doRequest(Request request) {
        String htmlCinsured = null;
        String urlCinsured = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCinsured = urlCinsured + StringBaseUtils.Map2GetParam(paraMap);
        Map mapCinsured = HttpsUtil.sendGet(urlCinsured,super.piccSessionId);
        htmlCinsured = mapCinsured.get("html").toString();
        return htmlCinsured;
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
