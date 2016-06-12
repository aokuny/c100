package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/6/6.
 */
public class ShowBusinessPlanInfoPage extends BasePage {

    private static Logger logger = Logger.getLogger(ShowBusinessPlanInfoPage.class);

    public ShowBusinessPlanInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();
        StringBuffer param = new StringBuffer();
        param.append("ecInsureId="+paramMap.get("ecInsureId")+"&");
        param.append("pageInfo=carPrecisionInfo"+"&");
        param.append("selectPayChannel=undefined&");
        param.append("linkResource="+paramMap.get("linkResource")+"&");
        param.append("lastForcePolicyNo="+paramMap.get("lastForcePolicyNo")+"&");
        param.append("tbsn=&");
        param.append("isAgent="+paramMap.get("isAgent")+"&");
        param.append("cityCode="+paramMap.get("cityCode")+"&");
        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("ms="+paramMap.get("ms")+"&");
        param.append("runCardCertificateDate=undefined&");
        param.append("isJZ="+paramMap.get("isJZ")+"&");
        param.append("isSameVehicle=");
        url=url+"?"+param.toString();

        Map map = HttpsUtil.sendGet(url, super.cookieValue, "GBK");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map nextParamsMap = new HashMap<>();
            nextParamsMap = request.getRequestParam();

            Document doc = Jsoup.parse(html);
            System.out.print("html="+html);
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
