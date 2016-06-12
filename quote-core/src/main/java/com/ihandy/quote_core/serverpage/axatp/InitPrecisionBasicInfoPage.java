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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/6/7.
 */
public class InitPrecisionBasicInfoPage extends BasePage {

    private static Logger logger = Logger.getLogger(InitPrecisionBasicInfoPage.class);

    public InitPrecisionBasicInfoPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();
        String param = "";
        String cityName=paramMap.get("cityName").toString();
        try {
            cityName = java.net.URLEncoder.encode(cityName , "utf-8");
        }catch (Exception e){}
        param = param+"cityName="+cityName+"&";

        param = param+"ms="+paramMap.get("ms")+"&";
        param = param+"cityCode="+paramMap.get("cityCode")+"&";
        String licenceNo=paramMap.get("licenceNo").toString();
        try {
            licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
        }catch (Exception e){}
        param = param+"licenceNo="+licenceNo+"&";
        param = param+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        param = param+"transferFlag="+paramMap.get("transferFlagHidden")+"&";
        param = param+"pagereferrer="+paramMap.get("pagereferrer")+"&";
        param = param+"isAgent="+paramMap.get("isAgent")+"&";
        param = param+"pageInfo=selectCity&";
        String personnelName = paramMap.get("personnelName").toString();
        try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}
        param = param+"personnelName="+personnelName+"&";
        param = param+"localProvinceCode="+paramMap.get("localProvinceCode");
        url = url+"?"+param;

        Map map = HttpsUtil.sendGet(url,super.cookieValue,"gb2312");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map nextParamsMap = request.getRequestParam();

            Document doc = Jsoup.parse(html);
            Elements hideinputs = doc.select("input");
            for(int i=0;i<hideinputs.size();i++) {
                Element e = hideinputs.get(i);
                String type = e.attributes().get("type");
                if (type.equals("hidden")) {
                    String key = e.attributes().get("id");
                    String value = e.attributes().get("value");
                    nextParamsMap.put(key, value);
                }
            }

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
