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
public class XubaoShowCitemCarPage extends BasePage{
    @Override
    public String doRequest(Request request) {
        String htmlCitemCar = null;
        String urlCitemCar = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCitemCar = urlCitemCar + StringBaseUtils.Map2GetParam(paraMap);
        Map mapClaimsMsg = HttpsUtil.sendGet(urlCitemCar,super.piccSessionId);
        htmlCitemCar = mapClaimsMsg.get("html").toString();
        return htmlCitemCar;
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
