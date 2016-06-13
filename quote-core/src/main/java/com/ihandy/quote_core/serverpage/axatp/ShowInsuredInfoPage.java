package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
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
public class ShowInsuredInfoPage extends BasePage {
    private static Logger logger = Logger.getLogger(ShowInsuredInfoPage.class);

    public ShowInsuredInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();
       /* http://dm.axatp.com/showInsuredInfo.do?isAgent=3212&ecInsureId=E1E65AD17E74DCCB1C7ED258401BBF1DA4DB34E885257363&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&ms=&processFlag=1&pkgName=free&premium=4505.90&radioBiz=Y&bizInsureBeignTime=2016-07-10&isJZ=1&nonAutoPageInfo=undefined&suddenessPlanDefineId=undefined
       */
        String personnelName=paramMap.get("personnelName").toString();
        try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}
        StringBuffer param = new StringBuffer();
        param.append("isAgent="+paramMap.get("isAgent")+"&");
        param.append("ecInsureId="+paramMap.get("ecInsureId")+"&");
        param.append("cityCode="+paramMap.get("cityCode")+"&");
        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("ms="+paramMap.get("ms")+"&");
        param.append("processFlag=1&");
        param.append("pkgName=free&");
        param.append("premium="+paramMap.get("premium")+"&");
        param.append("radioBiz=Y&");
        param.append("bizInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&");
        param.append("isJZ="+paramMap.get("isJZ")+"&");
        param.append("nonAutoPageInfo=&");
        param.append("suddenessPlanDefineId=");
        url=url+"?"+param.toString();
        String postParam="forcePolicyFlag=0&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&pageInfo="+paramMap.get("pageInfo")+"&ecInsureId="+paramMap.get("ecInsureId")+"&bizPolicyFlag=1&tbsn=&isAgent="+paramMap.get("isAgent")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt"+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&isCalculation=1&isJingHu=1&requestType=calc&initPremiumFlagTPF=1&radioTPF=Y&forceInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&fuelType=A&personnelName="+personnelName;
        Map map = HttpsUtil.sendPost(url,postParam, super.cookieValue, "GBK");
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
