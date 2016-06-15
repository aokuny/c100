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
public class ShowTemporaryInfoPage extends BasePage {
    private static Logger logger = Logger.getLogger(ShowTemporaryInfoPage.class);

    public ShowTemporaryInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();
       /*http://dm.axatp.com/showTemporaryInfo.do?isAgent=3212&cityCode=110100&ecInsureId=E1E65AD17E74DCCB1C7ED258401BBF1D17F2473954F26D29&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&processFlag=1&isJZ=1
       */
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
        StringBuffer param = new StringBuffer();
        param.append("isAgent="+paramMap.get("isAgent")+"&");
        param.append("cityCode="+paramMap.get("cityCode")+"&");
        param.append("ecInsureId="+paramMap.get("ecInsureId")+"&");

        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("processFlag="+paramMap.get("processFlag")+"&");
        param.append("ms="+paramMap.get("ms")+"&");
        param.append("isJZ="+paramMap.get("isJZ"));
        url=url+"?"+param.toString();
        String postParam="isVIP="+paramMap.get("isVIP")+"&isAgent="+paramMap.get("isAgent")+"&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&ecInsureId="+paramMap.get("ecInsureId")+"&applyPolicyNo=&lastPolicyNo=&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&isAgent="+paramMap.get("isAgent")+"&isSave="+paramMap.get("isSave")+"&isSendPolicy=1&oldCityCode="+paramMap.get("oldCityCode")+"&payMode=1&realPayMode=8&provincePCC=&cityPCC=&countyPCC=&provinceCode="+paramMap.get("provinceCode")+"&birthdayYY="+paramMap.get("birthdayYY")+"&birthdayMM="+paramMap.get("birthdayMM")+"&birthdayDD="+paramMap.get("birthdayDD")+"&birthdayYY_=&birthdayMM_=&birthdayDD_=&tempIds="+paramMap.get("tempIds")+"&offerType=1&setAddressFlag=0&pageInfo="+paramMap.get("pageInfo")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&processFlag="+paramMap.get("processFlag")+"&insuranceBeginTime="+paramMap.get("insuranceBeginTime")+"&insuranceEndTime="+paramMap.get("insuranceEndTime")+"&hasCountyListFlag="+paramMap.get("hasCountyListFlag")+"&isOpenBestdate ="+paramMap.get("isOpenBestdate")+"&minDate="+paramMap.get("minDate")+"&disabledDates="+paramMap.get("disabledDates")+"&tbsn="+paramMap.get("tbsn")+"&requestType="+paramMap.get("requestType")+"&pkgName="+paramMap.get("pkgName")+"&forcePolicyFlag="+paramMap.get("forcePolicyFlag")+"&bizPolicyFlag="+paramMap.get("bizPolicyFlag")+"&pagereferrer="+paramMap.get("pagereferrer")+"&cityName="+cityName+"&isBJInfoFirst="+paramMap.get("isBJInfoFirst")+"&OD="+paramMap.get("OD")+"&NDNE="+paramMap.get("NDNE")+"&FEDPC="+paramMap.get("FEDPC")+"&CBD="+paramMap.get("CBD")+"&LOSI="+paramMap.get("LOSI")+"&FLOOD="+paramMap.get("FLOOD")+"&select_NDNE_request="+paramMap.get("select_NDNE_request")+"&select_FEDPC_request="+paramMap.get("select_FEDPC_request")+"&superiorStatus="+paramMap.get("superiorStatus")+"&newbieStatus="+paramMap.get("newbieStatus")+"&isBjSuperviseOpen="+paramMap.get("isBjSuperviseOpen")+"&isJZ="+paramMap.get("isJZ")+"&personnelName_="+personnelName+"&certificateType_=1&certificateNo_="+paramMap.get("certificateNo_")+"&YYYY_="+paramMap.get("birthdayYY")+"&MM_="+paramMap.get("birthdayMM")+"&DD_="+paramMap.get("birthdayDD")+"&insuredAddress_="+paramMap.get("insuredAddress_")+"&email_="+paramMap.get("email_")+"&mobileTelephone_="+paramMap.get("mobileTelephone_")+"&sameAppFlag=1&personnelName="+personnelName+"&certificateType=1&certificateNo="+paramMap.get("certificateNo")+"&sexCode=M&YYYY=1998&MM=6&DD=13&insuredAddress=display&email=784506957%40qq.com&mobileTelephone="+paramMap.get("mobileTelePhone")+"&receiveNameInsured=&receiveName="+personnelName+"&receiveMobile="+paramMap.get("receiveMobile")+"&invoiceInsured="+paramMap.get("invoiceInsured")+"&invoice="+paramMap.get("invoice")+"&sendDate="+paramMap.get("sendDate")+"&sendTime=01&cityCode="+paramMap.get("cityCode")+"&receiveAddressInsured=&receiveAddress="+paramMap.get("receiveAddress")+"&payType =1";
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