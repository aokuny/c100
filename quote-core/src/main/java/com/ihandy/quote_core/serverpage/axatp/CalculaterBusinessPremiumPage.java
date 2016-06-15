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
public class CalculaterBusinessPremiumPage extends BasePage {
    private static Logger logger = Logger.getLogger(CalculaterBusinessPremiumPage.class);

    public CalculaterBusinessPremiumPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();
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
        String urlparam ="";
        urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
        urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
        urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
        urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
        urlparam = urlparam+"isRenewal="+paramMap.get("isRenewal")+"&";
        urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
        urlparam = urlparam+"id=_TP&";

        urlparam = urlparam+"bizInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&";

        url = url+"?"+urlparam;
        String postParam="ecInsureId="+paramMap.get("ecInsureId")+"&isDesignatdDriverArea=0&isDesignatdDriver=0&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2=&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&pageInfo=businessPlanInfo&lastForcePolicyNo=&tbsn=&errorMessage=&isAgent="+paramMap.get("isAgent")+"&cityCode="+paramMap.get("cityCode")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&amount_OD="+paramMap.get("amount_OD")+"&insuredAmountminFloat=&insuredAmountmaxFloat=&flag="+paramMap.get("flag")+"&rt=&ms=&isJingHu="+paramMap.get("isJingHu")+"&runCardCertificateDate=undefined&newPkgName=class&personnelName="+personnelName+"&vehicleLicenceCode="+licenceNo+"&cityName="+cityName+"&pagereferrer="+paramMap.get("pagereferrer")+"&infoValue="+paramMap.get("infoValue")+"&prohibitValue=&live800_URL_JSP="+paramMap.get("live800_URL_JSP")+"&isPresent=N&isJZ=1&initPremiumFlagBIZ=1&businessPremium_biz=&bizStampTax_biz=&totalDiscountPremium_biz=&originalBusinessPremium_biz=&economic_FlagBIZ=1&class_FlagBIZ=1&disabledDates=&goodCustomerSwitch=N&bizInsureBeignTime="+paramMap.get("bizInsureBeignDate")+"&businessFlag=Y&selectPkgType=class&pkgSelect=free&select_OD=OD&select_TP=300000&select_DL=20000&select_PL=20000&select_THEFT=THEFT&select_GLASS=1&select_NICK=N&select_NDNE=NDNE&select_FEDPC=FEDPC";

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
            double premium = Double.parseDouble(nextParamsMap.get("businessPremium").toString());
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
