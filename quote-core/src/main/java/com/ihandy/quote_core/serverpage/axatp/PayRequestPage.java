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
public class PayRequestPage extends BasePage {

    private static Logger logger = Logger.getLogger(PayRequestPage.class);

    public PayRequestPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();
        String requestDoc=paramMap.get("requestDoc").toString();
        String postParam="postpay="+paramMap.get("postpay")+"&requestDoc="+requestDoc+"&ecInsureId="+paramMap.get("ecInsureId")+"&isVech="+paramMap.get("isVech")+"&needValidCode="+paramMap.get("needValidCode")+"&imageUrl="+paramMap.get("imageUrl")+"&mobileTelephone="+paramMap.get("mobileTelephone")+"&validateCode="+paramMap.get("validateCode");
        String cookieValue="s_pers= s_nr=1465970200611-Repeat|1497506200611; s_sess= s_cc=true; s_sq=; s_ppv=100%2C100%2C3536; JSESSIONID=BA8E7ECFE98336CAE31888D9F8AE63BF";
       // String iCookie = new Cookie("captcha", cookieValue);
        String cookieValue1 = super.cookieValue;
        String newCookieValue="";
        String[] cookieValueArr = cookieValue1.split(";");
        for(int i=0;i<cookieValueArr.length;i++){
            if(cookieValueArr[i].contains("JSESSIONID")){
                newCookieValue =  newCookieValue+cookieValueArr[i]+";";
            }else if(cookieValueArr[i].contains("s_pers")){
                newCookieValue =  newCookieValue+cookieValueArr[i]+";";
            }else if(cookieValueArr[i].contains("s_sess")){
                newCookieValue =  newCookieValue+cookieValueArr[i]+";";
            }
        }
        Map map = HttpsUtil.sendPostHttps(url,postParam,newCookieValue);
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
