package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;

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
       /*http://dm.axatp.com/businessPremiumCalculater.do?isAgent=3212&ecInsureId=E1E65AD17E74DCCB6C352AC9787FF160A1DEBE80C4C8798B&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&id=_TP&bizInsureBeignTime=2016-07-10*/
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
        String postParam="ecInsureId="+paramMap.get("ecInsureId")+"&isDesignatdDriverArea=0&isDesignatdDriver=0&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2=&selectPayChannel=undefined&linkResource=&pageInfo=businessPlanInfo&lastForcePolicyNo=&tbsn=&errorMessage=&isAgent=3212&cityCode=110100&localProvinceCode=110000&isRenewal=0&amount_OD=137800&insuredAmountminFloat=&insuredAmountmaxFloat=&flag=1&rt=&ms=&isJingHu=1&runCardCertificateDate=undefined&newPkgName=class&personnelName=%D5%C5%CE%C4%BA%A3&vehicleLicenceCode=%BE%A9MM3767&cityName=%25E5%258C%2597%25E4%25BA%25AC%25E5%25B8%2582&pagereferrer=%25E5%259C%25B0%25E9%259D%25A2%25E8%2590%25A5%25E9%2594%2580-%25E6%258E%25A8%25E8%258D%2590&infoValue=userId%253D7124003%2526loginname%253Djtl_bj%2526grade%253D1%2526name%253Djtl_bj%2526mobileNo%253D%25E4%25BA%25ACMM3767%2526memo%253Dnull%2526timestamp%253D1465724171671%2526hashCode%253D3a0c788ae39b4ce7b379442ddbd3802b&prohibitValue=&live800_URL_JSP=http%3A%2F%2Fonlinecs.axatp.com%2Flive800%2FchatClient%2Fchatbox.jsp&isPresent=N&isJZ=1&initPremiumFlagBIZ=1&businessPremium_biz=&bizStampTax_biz=&totalDiscountPremium_biz=&originalBusinessPremium_biz=&economic_FlagBIZ=1&class_FlagBIZ=1&disabledDates=&goodCustomerSwitch=N&bizInsureBeignTime=2016-07-10&businessFlag=Y&selectPkgType=class&pkgSelect=free&select_OD=OD&select_TP=200000&select_DL=10000&select_PL=10000&select_THEFT=THEFT&select_GLASS=1&select_NICK=N&select_NDNE=NDNE&select_FEDPC=FEDPC";
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
