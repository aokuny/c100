package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by fengwen on 2016/6/6.
 */
public class SaveValidateCodePage extends BasePage{

    private static Logger logger = Logger.getLogger(SaveValidateCodePage.class);

    public SaveValidateCodePage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();


        String postParam="ecInsureId="+paramMap.get("ecInsureId")+"&mobileTelephone="+paramMap.get("mobileTelephone")+"&validateCode="+paramMap.get("validateCode");

        Map map = HttpsUtil.sendPost(url,postParam,super.cookieValue,"gb2312");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map nextParamsMap = request.getRequestParam();
            response.setResponseMap(nextParamsMap);
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
        Response response = getResponse(html, request);
        return response;
    }
}
