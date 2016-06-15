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
public class CarBasicVehiclePriceQuery extends BasePage{
        private static Logger logger = Logger.getLogger(CarBasicVehiclePriceQuery.class);

        public CarBasicVehiclePriceQuery(int type) {
            super(type);
        }
        @Override
        public String doRequest(Request request) {
            String html= "";
            String url = request.getUrl();
            Map paramMap = request.getRequestParam();

            /* isRenewal=0&cityCode=110100&localProvinceCode=110000&planDefineId=3&rt=&licenceNo=¾©MM3767&personnelName=ÕÅÎÄº£*/
            String urlparam ="";
            urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
            urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";
            urlparam = urlparam+"isRenewal="+paramMap.get("isRenewal")+"&";
            urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
            urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
            urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
            urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
            String licenceNo=paramMap.get("licenceNo").toString();
            String personnelName=paramMap.get("personnelName").toString();
            try {
                licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
            }catch (Exception e){}
            try {
                personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
            }catch (Exception e){}
            urlparam = urlparam+"licenceNo="+licenceNo+"&";
            urlparam = urlparam+"personnelName="+personnelName;
            url = url+"?"+urlparam;
            String cityName=paramMap.get("cityName").toString();
            try {
                cityName = java.net.URLEncoder.encode(cityName , "utf-8");
            }catch (Exception e){}
            String  postParam1= "defaultAgentCode="+paramMap.get("defaultAgentCode")+"&isVIP="+paramMap.get("isVIP")+"&birthdayYY="+paramMap.get("birthdayYY")+"&birthdayMM="+paramMap.get("birthdayMM")+"&birthdayDD="+paramMap.get("birthdayDD")+"&newVehicleFlagHidden="+paramMap.get("newVehicleFlagHidden")+"&lastForcePolicyNo="+paramMap.get("lastForcePolicyNo")+"&ecInsureId="+paramMap.get("ecInsureId")+"&isRenewal="+paramMap.get("isRenewal")+"&linkResource="+paramMap.get("linkResource")+"&isAgent="+paramMap.get("isAgent")+"&cityCode="+paramMap.get("cityCode")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&planDefineId="+paramMap.get("planDefineId")+"&selectPayChannel=&pageInfo=carPrecisionInfo&tbsn=&buyerNick=&orderId=&auctionId="+paramMap.get("auctionId")+"&cityPlat="+paramMap.get("cityPlat")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&timedefault="+paramMap.get("timedefault")+"&mark="+paramMap.get("mark")+"&licenceLimits="+paramMap.get("licenceLimits")+"&licenceLimitsMsg="+paramMap.get("licenceLimitsMsg")+"&city_beijing="+paramMap.get("city_beijing")+"&bizInsureBeignTime="+paramMap.get("bizInsureBeignTime")+"&forceInsureBeignTime="+paramMap.get("forceInsureBeignTime")+"&pagereferrer="+paramMap.get("pagereferrer")+"&cityName="+cityName+"&carcity="+licenceNo+"&live800_URL_JSP="+paramMap.get("live800_URL_JSP")+"&gztFlag="+paramMap.get("gztFlag")+"&viewMode="+paramMap.get("viewMode")+"&hideFamilyName="+paramMap.get("hideFamilyName")+"&connectType&isFirstIframe="+paramMap.get("isFirstIframe")+"&vehicleId="+paramMap.get("vehicleId")+"&newCarPriceOld="+paramMap.get("newCarPriceOld")+"&newCarPrice="+paramMap.get("newCarPrice")+"&newCarSeats="+paramMap.get("newCarSeats")+"&isJZ="+paramMap.get("isJZ")+"&ecEnginNo="+paramMap.get("ecEnginNo")+"&searchType="+paramMap.get("searchType")+"&flag="+paramMap.get("flag")+"&messages=&infoValue="+paramMap.get("infoValue")+"&prohibitValue=&blackListFlag="+paramMap.get("blackListFlag")+"&renewalProtectFlag="+paramMap.get("renewalProtectFlag")+"&knmdFlag=&isCompleteData="+paramMap.get("isCompleteData")+"&isBeiJingFlag="+paramMap.get("isBeiJingFlag")+"&madeDate="+paramMap.get("madeDate")+"&engineNo="+paramMap.get("engineNo")+"&vehicleFrameNo="+paramMap.get("vehicleFrameNo")+"&personnelName="+personnelName+"&certificateNo="+paramMap.get("certificateNo")+"&mobileTelephone="+paramMap.get("mobileTelePhone")+"&isMortgage=&BeneficiaryName=";



            Map map = HttpsUtil.sendPost(url,postParam1,super.cookieValue,"gb2312");
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
                Element radioInput = doc.getElementById("carRdo");
                String value = radioInput.attributes().get("value").toString();
                String[] valueArr = value.split(",");
                //nextParamsMap.put("oldEcInsureId", nextParamsMap.get("ecInsureId"));
                try {
                    nextParamsMap.put("newCarPrice", valueArr[0]);
                    nextParamsMap.put("amount_OD", valueArr[0]);
                    nextParamsMap.put("rbCode", valueArr[1]);
                    String autoModelChnName=valueArr[2];
                    try {
                        autoModelChnName = java.net.URLEncoder.encode(autoModelChnName , "gbk");
                    }catch (Exception e){}
                    nextParamsMap.put("autoModelChnName",autoModelChnName);
                    String brandChnName=valueArr[3];
                    try {
                        brandChnName = java.net.URLEncoder.encode(brandChnName , "gbk");
                    }catch (Exception e){}
                    nextParamsMap.put("brandChnName",brandChnName);
                    nextParamsMap.put("engineNo",valueArr[4]);
                    nextParamsMap.put("vehicleFrameNo",valueArr[5]);
                    nextParamsMap.put("exhaustCapability", valueArr[6]);
                    nextParamsMap.put("tons",valueArr[7]);
                    nextParamsMap.put("seats",valueArr[8]);
                    nextParamsMap.put("firstRegisterDate",valueArr[9]);
                    nextParamsMap.put("licenceTypeCode", valueArr[11]);


                }catch (Exception e){
                  /*  nextParamsMap.put("rbCode","");
                    nextParamsMap.put("newCarPrice", "");
                    nextParamsMap.put("tons",0);
                    nextParamsMap.put("exhaustCapability", "");*/
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
