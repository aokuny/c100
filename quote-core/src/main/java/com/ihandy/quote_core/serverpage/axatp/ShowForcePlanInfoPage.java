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
public class ShowForcePlanInfoPage extends BasePage {
    private static Logger logger = Logger.getLogger(ShowForcePlanInfoPage.class);

    public ShowForcePlanInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
     /*   http://dm.axatp.com/showForcePlanInfo.do?isAgent=3212&ecInsureId=E1E65AD17E74DCCB1DD9B6E7CD5E07291A84D5FA2DE10B80&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&requestType=init&isShowForceFirst=undefined*/
        Map paramMap =request.getRequestParam();
        StringBuffer param = new StringBuffer();
        param.append("isAgent="+paramMap.get("isAgent")+"&");
        param.append("ecInsureId="+paramMap.get("ecInsureId")+"&");
        param.append("cityCode="+paramMap.get("cityCode")+"&");
        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("requestType=init"+"&");
        param.append("isShowForceFirst=undefined&");
        url=url+"?"+param.toString();
        String licenceNo=paramMap.get("licenceNo").toString();
        String personnelName=paramMap.get("personnelName").toString();
        String cityName=paramMap.get("cityName").toString();
        try {
            licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
        }catch (Exception e){}
        try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}
        try {
            cityName = java.net.URLEncoder.encode(cityName , "utf-8");
        }catch (Exception e){}

        String postParam="ecInsureId="+paramMap.get("ecInsureId")+"&isDesignatdDriverArea=0&isDesignatdDriver=0&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&selectPayChannel="+paramMap.get("selectPayChannel")+"&linkResource="+paramMap.get("linkResource")+"&pageInfo="+paramMap.get("pageInfo")+"&lastForcePolicyNo=&tbsn=&errorMessage=&isAgent="+paramMap.get("isAgent")+"&cityCode="+paramMap.get("cityCode")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&amount_OD="+paramMap.get("amount_OD")+"&insuredAmountminFloat=&insuredAmountmaxFloat=&flag=1&rt=&ms=&isJingHu=1&runCardCertificateDate=undefined&newPkgName=class&personnelName="+personnelName+"&vehicleLicenceCode="+licenceNo+"&cityName="+cityName+"&pagereferrer="+paramMap.get("pagereferrer")+"&infoValue="+paramMap.get("infoValue")+"&prohibitValue=&live800_URL_JSP="+paramMap.get("live800_URL_JSP")+"&isPresent=N&isJZ=1&initPremiumFlagBIZ=&businessPremium_biz=&bizStampTax_biz=&totalDiscountPremium_biz=&originalBusinessPremium_biz=&economic_FlagBIZ=&class_FlagBIZ=&disabledDates=&goodCustomerSwitch=N&bizInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&pkgSelect=class&businessFlag=Y&selectPkgType=free&select_OD=N&select_TP=N&select_DL=N&select_PL=N&select_THEFT=N&select_NICK=N&select_FEDPC=N";

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
                    if(key==null){
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
