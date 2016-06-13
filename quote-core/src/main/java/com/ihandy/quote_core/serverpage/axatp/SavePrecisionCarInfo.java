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

import java.util.Map;

/**
 * Created by fengwen on 2016/6/12.
 */
public class SavePrecisionCarInfo extends BasePage {
    private static Logger logger = Logger.getLogger(SavePrecisionCarInfo.class);
    public SavePrecisionCarInfo(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();

      /*licenceNo=¾©MM3767&autoModelCode=XDABJD0010&vehicleCode=XDABJD0010*/

        Map paramMap =request.getRequestParam();
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
        param.append("isRenewal="+paramMap.get("isRenewal")+"&");
        param.append("localProvinceCode="+paramMap.get("localProvinceCode")+"&");
        param.append("planDefineId="+paramMap.get("planDefineId")+"&");
        param.append("rt="+paramMap.get("rt")+"&");
        param.append("licenceNo="+licenceNo+"&");
        param.append("autoModelCode="+paramMap.get("rbCode")+"&");
        param.append("vehicleCode="+paramMap.get("rbCode"));
        url=url+"?"+param.toString();
       // url = url+"?"+param;
        String postParam1="newCarPrice="+paramMap.get("newCarPrice")+"&autoModeCode="+paramMap.get("rbCode")+"&autoModelChnName="+paramMap.get("autoModelChnName")+"&brandChnName="+paramMap.get("brandChnName")+"&engineNo="+paramMap.get("engineNo")+"&vehicleFrameNo="+paramMap.get("vehicleFrameNo")+"&exhaustCapability="+paramMap.get("exhaustCapability")+"&tons="+paramMap.get("tons")+"&seats="+paramMap.get("seats")+"&firstRegisterDate="+paramMap.get("firstRegisterDate")+"&curbWeightMax=&licenceTypeCode="+paramMap.get("licenceTypeCode")+"&power=&vehicleType=&newCarPrice_price="+paramMap.get("newCarPrice")+"&firstSaleDate=&remark=&transferDate=&usageAttributeCodePM=&vehicleColor=&approvedCapacity=&ineffectualDate=&maker=&brandCN=&brandEN=&vehicleStyle=&vehicleStyleDesc=&lastCheckDate=rejectDate=&status=&haulage=&producerType=&pmQueryNo=&pmFullType&vehicleCategory=&";
        String postParam2 = "defaultAgentCode="+paramMap.get("defaultAgentCode")+"&isVIP="+paramMap.get("isVIP")+"&birthdayYY="+paramMap.get("birthdayYY")+"&birthdayMM="+paramMap.get("birthdayMM")+"&birthdayDD="+paramMap.get("birthdayDD")+"&newVehicleFlagHidden="+paramMap.get("newVehicleFlagHidden")+"&lastForcePolicyNo="+paramMap.get("lastForcePolicyNo")+"&ecInsureId="+paramMap.get("ecInsureId")+"&isRenewal="+paramMap.get("isRenewal")+"&linkResource="+paramMap.get("linkResource")+"&isAgent="+paramMap.get("isAgent")+"&cityCode="+paramMap.get("cityCode")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&planDefineId="+paramMap.get("planDefineId")+"&selectPayChannel=&pageInfo=carPrecisionInfo&tbsn=&buyerNick=&orderId=&auctionId="+paramMap.get("auctionId")+"&cityPlat="+paramMap.get("cityPlat")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&timedefault="+paramMap.get("timedefault")+"&mark="+paramMap.get("mark")+"&licenceLimits="+paramMap.get("licenceLimits")+"&licenceLimitsMsg="+paramMap.get("licenceLimitsMsg")+"&city_beijing="+paramMap.get("city_beijing")+"&bizInsureBeignTime="+paramMap.get("bizInsureBeignTime")+"&forceInsureBeignTime="+paramMap.get("forceInsureBeignTime")+"&pagereferrer="+paramMap.get("pagereferrer")+"&cityName="+cityName+"&carcity="+licenceNo+"&live800_URL_JSP="+paramMap.get("live800_URL_JSP")+"&gztFlag="+paramMap.get("gztFlag")+"&viewMode="+paramMap.get("viewMode")+"&hideFamilyName="+paramMap.get("hideFamilyName")+"&connectType&isFirstIframe="+paramMap.get("isFirstIframe")+"&vehicleId="+paramMap.get("vehicleId")+"&newCarPriceOld="+paramMap.get("newCarPriceOld")+"&newCarPrice="+paramMap.get("newCarPrice")+"&newCarSeats="+paramMap.get("newCarSeats")+"&isJZ="+paramMap.get("isJZ")+"&ecEnginNo="+paramMap.get("ecEnginNo")+"&searchType="+paramMap.get("searchType")+"&flag="+paramMap.get("flag")+"&messages=&infoValue="+paramMap.get("infoValue")+"&prohibitValue=&blackListFlag="+paramMap.get("blackListFlag")+"&renewalProtectFlag="+paramMap.get("renewalProtectFlag")+"&knmdFlag=&isCompleteData="+paramMap.get("isCompleteData")+"&isBeiJingFlag="+paramMap.get("isBeiJingFlag")+"&madeDate="+paramMap.get("madeDate")+"&engineNo="+paramMap.get("engineNo")+"&vehicleFrameNo="+paramMap.get("vehicleFrameNo")+"&personnelName="+personnelName+"&certificateNo="+paramMap.get("certificateNo")+"&mobileTelephone="+paramMap.get("mobileTelePhone")+"&isMortgage=&BeneficiaryName=";

        String postParam=postParam1+postParam2;
        Map map = HttpsUtil.sendPost(url,postParam,super.cookieValue,"gb2312");
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
