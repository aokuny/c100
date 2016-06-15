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
        param.append("nonAutoPageInfo=undefined&");
        param.append("suddenessPlanDefineId=undefined");
        url=url+"?"+param.toString();
        String postParam="forcePolicyFlag="+paramMap.get("forcePolicyFlag")+"&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2=&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&pageInfo="+paramMap.get("pageInfo")+"&ecInsureId="+paramMap.get("ecInsureId")+"&bizPolicyFlag=1&tbsn=&isAgent="+paramMap.get("isAgent")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&isCalculation=1&isJingHu="+paramMap.get("isJingHu")+"&requestType="+paramMap.get("requestType")+"&initPremiumFlagTPF="+paramMap.get("initPremiumFlagTPF")+"&radioTPF=Y&forceInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&fuelType=A&personnelName="+personnelName;
     /*   postParam="forcePolicyFlag=0&planDefineId=3&planDefineId2=&selectPayChannel=undefined&linkResource=&pageInfo=forcePlanInfo\n" +
                "&ecInsureId="+paramMap.get("ecInsureId")+"&bizPolicyFlag=1&tbsn=&isAgent=3212&localProvinceCode\n" +
                "=110000&isRenewal=0&rt=&ms=&isCalculation=1&isJingHu=1&requestType=calc&initPremiumFlagTPF=1&radioTPF\n" +
                "=Y&forceInsureBeignTime=2016-07-10&fuelType=A&personnelName=%D5%C5%CE%C4%BA%A3";*/
        String cookieValue=super.cookieValue+";s_sess=%20s_cc%3Dtrue%3B%20s_ppv%3D100%252C75%252C1814%3B%20s_sq%3D%3B; s_pers=%20s_nr%3D1465898477282-New%7C1497434477282%3B;title=%u7F51%u4E0A%u6295%u4FDD-%u5B89%u76DB%u5929%u5E73-%u60A8%u8EAB%u8FB9%u7684%u8F66%u9669%u4E13%u5BB6; operationgSystem=other; browserType=Firefox; browserVersion=firefox47.0; screenResolution=1366x768; javaSupport=0; flashSupport=0; flashVersion=; cookiesSupport=1; browserLanguage=; browserEncoding=gbk; clientType=1; isVip=false;  ecInsureId110100="+paramMap.get("ecInsureId")+"#%E4%BA%ACMM3767; continueInsuranceData_personnelName=%E5%BC%A0*%E6%B5%B7; continueInsuranceData=ecInsureId="+paramMap.get("ecInsureId")+"&&isAgent=3212&&cityCode=110100&&isRenewal=0&&localProvinceCode=110000&&planDefineId=3&&rt=&&ms=&&vehicleLicenceCode=%E4%BA%ACMM3767&&personnelName=%E5%BC%A0*%E6%B5%B7&&totalActualPremium="+paramMap.get("premium");
        Map map = HttpsUtil.sendPost(url,postParam, cookieValue, "GBK");
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
                    if(key.equals("")){
                        key = e.attributes().get("name");
                    }
                    String value = e.attributes().get("value");
                    nextParamsMap.put(key, value);
                }
                else if(type.equals("radio")){
                    if(e.attributes().hasKey("checked")){
                        String key = e.attributes().get("name");
                        String value = e.attributes().get("value");
                        nextParamsMap.put(key, value);
                    }
                }
                else if(type.equals("checkbox")){
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
