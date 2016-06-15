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
        String tempIds = paramMap.get("tempIds").toString();
        try {
            licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
        }catch (Exception e){}
        try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}
        try {
            cityName = java.net.URLEncoder.encode(cityName , "utf-8");
        }catch (Exception e){}
        try {
            tempIds = java.net.URLEncoder.encode(tempIds , "gbk");
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
        param.append("isJZ="+paramMap.get("isJZ"));
        url=url+"?"+param.toString();
        String postParam="isVIP="+paramMap.get("isVIP")+"&isAgent="+paramMap.get("isAgent")+"&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&ecInsureId="+paramMap.get("ecInsureId")+"&applyPolicyNo="+paramMap.get("applyPolicyNo")+"&lastPolicyNo="+paramMap.get("lastPolicyNo")+"&selectPayChannel="+paramMap.get("selectPayChannel")+"&linkResource="+paramMap.get("linkResource")+"&isAgent="+paramMap.get("isAgent")+"&isSave="+paramMap.get("isSave")+"&isSendPolicy="+paramMap.get("isSendPolicy")+"&oldCityCode="+paramMap.get("oldCityCode")+"&payMode="+paramMap.get("payMode")+"&realPayMode="+paramMap.get("realPayMode")+"&provincePCC="+paramMap.get("provincePCC")+"&cityPCC="+paramMap.get("cityPCC")+"&countyPCC="+paramMap.get("countyPCC")+"&provinceCode="+paramMap.get("provinceCode")+"&birthdayYY="+paramMap.get("birthdayYY")+"&birthdayMM="+paramMap.get("birthdayMM")+"&birthdayDD="+paramMap.get("birthdayDD")+"&birthdayYY_="+paramMap.get("birthdayYY_")+"&birthdayMM_="+paramMap.get("birthdayMM_")+"&birthdayDD_="+paramMap.get("birthdayDD_")+"&tempIds="+paramMap.get("tempIds")+"&offerType="+paramMap.get("offerType1")+"&setAddressFlag="+paramMap.get("setAddressFlag")+"&pageInfo="+paramMap.get("pageInfo")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&processFlag="+paramMap.get("processFlag")+"&insuranceBeginTime="+paramMap.get("insuranceBeginTime")+"&insuranceEndTime="+paramMap.get("insuranceEndTime")+"&hasCountyListFlag="+paramMap.get("hasCountyListFlag")+"&isOpenBestdate="+paramMap.get("isOpenBestdate")+"&minDate="+paramMap.get("minDate")+"&disabledDates="+paramMap.get("disabledDates")+"&tbsn="+paramMap.get("tbsn")+"&requestType="+paramMap.get("requestType")+"&pkgName="+paramMap.get("pkgName")+"&forcePolicyFlag="+paramMap.get("forcePolicyFlag")+"&bizPolicyFlag="+paramMap.get("bizPolicyFlag")+"&pagereferrer="+paramMap.get("pagereferrer")+"&cityName="+cityName+"&isBJInfoFirst="+paramMap.get("isBJInfoFirst")+"&OD="+paramMap.get("OD")+"&NDNE="+paramMap.get("NDNE")+"&FEDPC="+paramMap.get("FEDPC")+"&CBD="+paramMap.get("CBD")+"&LOSI="+paramMap.get("LOSI")+"&FLOOD="+paramMap.get("FLOOD")+"&select_NDNE_request="+paramMap.get("select_NDNE_request")+"&select_FEDPC_request="+paramMap.get("select_FEDPC_request")+"&superiorStatus="+paramMap.get("superiorStatus")+"&newbieStatus="+paramMap.get("newbieStatus")+"&sameInsuredFlag_="+paramMap.get("sameInsuredFlag_")+"&isBjSuperviseOpen="+paramMap.get("isBjSuperviseOpen")+"&isJZ="+paramMap.get("isJZ")+"&personnelName_="+personnelName+"&certificateType_=1&certificateNo_="+paramMap.get("certificateNo_")+"&YYYY_="+paramMap.get("birthdayYY")+"&MM_="+paramMap.get("birthdayMM")+"&DD_="+paramMap.get("birthdayDD")+"&insuredAddress_=display&email_="+paramMap.get("email_")+"&mobileTelephone_="+paramMap.get("mobileTelephone_")+"&sameAppFlag="+paramMap.get("sameAppFlag")+"&personnelName="+personnelName+"&certificateType=1&certificateNo="+paramMap.get("certificateNo")+"&sexCode="+paramMap.get("sexCode")+"&YYYY="+paramMap.get("birthdayYY")+"&MM="+paramMap.get("birthdayMM")+"&DD="+paramMap.get("birthdayDD")+"&insuredAddress="+paramMap.get("insuredAddress")+"&email="+paramMap.get("email")+"&mobileTelephone="+paramMap.get("mobileTelephone")+"&receiveNameInsured="+paramMap.get("receiveNameInsured")+"&receiveName="+personnelName+"&receiveMobile="+paramMap.get("receiveMobile")+"&invoiceInsured="+paramMap.get("invoiceInsured")+"&invoice="+paramMap.get("invoice")+"&sendDate="+paramMap.get("sendDate")+"&sendTime=01&cityCode="+paramMap.get("cityCode")+"&receiveAddressInsured="+paramMap.get("receiveAddressInsured")+"&receiveAddress="+paramMap.get("receiveAddress")+"&payType=1";
       /* postParam="isVIP=false&isAgent=3212&planDefineId=3&planDefineId2=&ecInsureId=" +paramMap.get("ecInsureId")+
                "&applyPolicyNo=&lastPolicyNo=&selectPayChannel=undefined&linkResource=&isAgent=3212&isSave=true&isSendPolicy" +
                "=1&oldCityCode=110100&payMode=1&realPayMode=8&provincePCC=&cityPCC=&countyPCC=&provinceCode=110000&birthdayYY" +
                "=1969&birthdayMM=3&birthdayDD=13&birthdayYY_=&birthdayMM_=&birthdayDD_=&tempIds="+tempIds+
                "&offerType=1&setAddressFlag=0&pageInfo=insuredInfo&localProvinceCode=110000&isRenewal=0&rt=&ms=&processFlag" +
                "=1&insuranceBeginTime=2016-07-10%3E&insuranceEndTime=2017-07-09%3E&hasCountyListFlag=1&isOpenBestdate" +
                "=1&minDate=2016-06-14&disabledDates=&tbsn=&requestType=calc&pkgName=free&forcePolicyFlag=1&bizPolicyFlag" +
                "=1&pagereferrer=%25E5%259C%25B0%25E9%259D%25A2%25E8%2590%25A5%25E9%2594%2580-%25E6%258E%25A8%25E8%258D" +
                "%2590&cityName=%25E5%258C%2597%25E4%25BA%25AC%25E5%25B8%2582&isBJInfoFirst=1&OD=&NDNE=&FEDPC=&CBD=&LOSI" +
                "=&FLOOD=&select_NDNE_request=&select_FEDPC_request=&superiorStatus=&newbieStatus=&sameInsuredFlag_=1" +
                "&isBjSuperviseOpen=1&isJZ=1&personnelName_=%D5%C5%CE%C4%BA%A3&certificateType_=1&certificateNo_=132425196903135852" +
                "&YYYY_=1998&MM_=6&DD_=14&insuredAddress_=display&email_=784506957%40qq.com&mobileTelephone_=18810253437" +
                "&sameAppFlag=1&personnelName=%D5%C5%CE%C4%BA%A3&certificateType=1&certificateNo=132425196903135852&sexCode" +
                "=M&YYYY=1998&MM=6&DD=14&insuredAddress=display&email=784506957%40qq.com&mobileTelephone=18810253437&receiveNameInsured" +
                "=&receiveName=%D5%C5%CE%C4%BA%A3&receiveMobile=18810253437&invoiceInsured=&invoice=%D5%C5%CE%C4%BA%A3" +
                "&sendDate=2016-07-01&sendTime=01&cityCode=110100&receiveAddressInsured=&receiveAddress=mentougou2412" +
                "&payType=1";*/

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
