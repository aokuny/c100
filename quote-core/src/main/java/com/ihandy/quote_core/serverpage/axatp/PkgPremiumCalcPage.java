package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/6/12.
 */
public class PkgPremiumCalcPage extends BasePage {
    private static Logger logger = Logger.getLogger(PkgPremiumCalcPage.class);

    public PkgPremiumCalcPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
/*        http://dm.axatp.com/pkgPremiumCalc.do?_=1465722522360&&pkgName=class&calcFlag=true&isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&bizInsureBeignTime=2016-07-10*/
        Map paramMap =request.getRequestParam();

        StringBuffer param = new StringBuffer();

        param.append("ecInsureId="+paramMap.get("ecInsureId")+"&");

        param.append("tbsn="+paramMap.get("tbsn")+"&");
        param.append("isAgent="+paramMap.get("isAgent")+"&");
        param.append("cityCode="+paramMap.get("cityCode")+"&");
        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("linkResource="+paramMap.get("linkResource")+"&");
        url=url+"?"+param.toString();
        url = url+"?"+param;
        Map map = HttpsUtil.sendGet(url,super.cookieValue,"UTF-8");
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
