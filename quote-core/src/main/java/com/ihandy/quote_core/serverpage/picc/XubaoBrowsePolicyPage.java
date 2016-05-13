package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoBrowsePolicyPage extends BasePage {
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        url = url+ StringBaseUtils.Map2GetParam(paraMap);
        Map map = HttpsUtil.sendGet(url,super.piccSessionId);
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        if(null!=html){
            Map  returnMap  = new HashMap<>();
            Map mapNextParam = new HashMap<>();
            returnMap.put("nextParams",mapNextParam);
            returnMap.put("lastResult",null);
            response.setResponseMap(returnMap);
            response.setReturnCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
        }else{
            response.setResponseMap(null);
            response.setReturnCode(SysConfigInfo.ERROR404);
            response.setErrMsg(SysConfigInfo.ERROR404MSG);
        }
        return response;
    }

    @Override
    public Response run(Request request) {
        String html = doRequest(request);
        Response response = getResponse(html);
        Map map =(Map) response.getResponseMap().get("nextParams");
        map.put("bizNo",request.getRequestParam().get("bizNo")); //将保单号传到下一个请求页面参数中

        return response;
    }
}
