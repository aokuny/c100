package com.ihandy.quote_core.serverpage.axatp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * Created by fengwen on 2016/6/6.
 */
public class CalculaterForcePremiumPage extends BasePage{
    private static Logger logger = Logger.getLogger(CalculaterForcePremiumPage.class);

    public CalculaterForcePremiumPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();
       /* http://dm.axatp.com/forcePremiumCalculater.do?isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&rt=&pkgName=free&requestType=calc*/

        String personnelName=paramMap.get("personnelName").toString();
        try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}

        String urlparam ="";
        urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
        urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";

        urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
        urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
        urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
        urlparam = urlparam+"isRenewal="+paramMap.get("isRenewal")+"&";
        urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
        urlparam = urlparam+"forcePolicyFlag=Y&";
        urlparam = urlparam+"pkgName=free&";
        urlparam = urlparam+"requestType=calc";
        url = url+"?"+urlparam;
        String postParam="forcePolicyFlag=0&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2=&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&pageInfo=forcePlanInfo&ecInsureId="+paramMap.get("ecInsureId")+"&bizPolicyFlag=1&tbsn=&isAgent="+paramMap.get("isAgent")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt=&ms=&isCalculation=1&isJingHu=1&requestType=calc&initPremiumFlagTPF=1&radioTPF=Y&forceInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&fuelType=A&personnelName="+personnelName;
        Map map = HttpsUtil.sendPost(url,postParam,super.cookieValue,"gb2312");
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
            double businessPremium = Double.parseDouble(nextParamsMap.get("premium").toString());
            double forcePremium = Double.parseDouble(nextParamsMap.get("forcePremium").toString());
            double vehicleTaxPremium = Double.parseDouble(nextParamsMap.get("vehicleTaxPremium").toString());
            double forcePremiumTotal = StringBaseUtils.forDight(forcePremium+vehicleTaxPremium,2);
            double premium = StringBaseUtils.forDight(businessPremium+forcePremium+vehicleTaxPremium,2);
            nextParamsMap.put("businessPremium",businessPremium);
            nextParamsMap.put("forcePremiumTotal",forcePremiumTotal);
            nextParamsMap.put("premium",premium);
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
