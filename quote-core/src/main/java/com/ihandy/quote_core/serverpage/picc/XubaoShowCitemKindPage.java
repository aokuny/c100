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
public class XubaoShowCitemKindPage extends BasePage {
    @Override
    public String doRequest(Request request) {
        String htmlCitemKind = null;
        String urlCitemKind = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCitemKind = urlCitemKind + StringBaseUtils.Map2GetParam(paraMap);
        Map mapClaimsMsg = HttpsUtil.sendGet(urlCitemKind,super.piccSessionId);
        htmlCitemKind = mapClaimsMsg.get("html").toString();
        return htmlCitemKind;
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
