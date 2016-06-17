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

import java.util.Map;

/**
 * Created by fengwen on 2016/6/13.
 */
public class ApplyUnderwritePage  extends BasePage{
    private static Logger logger = Logger.getLogger(ApplyUnderwritePage.class);

    public ApplyUnderwritePage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();

        String personnelName=paramMap.get("personnelName").toString();
        String cityName=paramMap.get("cityName").toString();
        String countyName = paramMap.get("countyName").toString();
        String countyPCC = paramMap.get("countyPCC").toString();
        String tempIds = paramMap.get("tempIds").toString();
      /*  try {
            licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
        }catch (Exception e){}*/
      /*  try {
            personnelName = java.net.URLEncoder.encode(personnelName , "gbk");
        }catch (Exception e){}*/
        try {
            cityName = java.net.URLEncoder.encode(cityName , "gbk");
        }catch (Exception e){}
        try {
            countyName = java.net.URLEncoder.encode(countyName , "gbk");
        }catch (Exception e){}
        try {
            countyPCC = java.net.URLEncoder.encode(countyPCC , "gbk");
        }catch (Exception e){}
        try {
            tempIds = java.net.URLEncoder.encode(tempIds , "gbk");
        }catch (Exception e){}

       /* http://dm.axatp.com/applyUnderwrite.do?isAgent=3212&ecInsureId=E1E65AD17E74DCCB1C7ED258401BBF1D78EB43D6293FE5BF&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&ms=*/
        String param = "";
        param = param+"isAgent="+paramMap.get("isAgent")+"&";
        param = param+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        param = param+"cityCode="+paramMap.get("cityCode")+"&";
        param = param+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
        param = param+"planDefineId="+paramMap.get("planDefineId")+"&";
        param = param+"isRenewal="+paramMap.get("isRenewal")+"&";
        param = param+"rt="+paramMap.get("rt")+"&";
        param = param+"ms="+paramMap.get("ms");
        url = url+"?"+param;
        String postParams="planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&ecInsureId="+paramMap.get("ecInsureId")+"&applyPolicyNo=&policyNo=&lastPolicyNo=&isTaxSuspend=&pageInfo="+paramMap.get("pageInfo")+"&isSave="+paramMap.get("isSave")+"&preToInsuredInfo="+paramMap.get("preToInsuredInfo")+"&isAgent="+paramMap.get("isAgent")+"&selectPayChannel=undefined&linkResource="+paramMap.get("linkResource")+"&isRenewal="+paramMap.get("isRenewal")+"&tempIds="+tempIds+"&flag="+paramMap.get("flag")+"&phone="+paramMap.get("phone")+"&password="+paramMap.get("password")+"&payType="+paramMap.get("payType")+"&payMode="+paramMap.get("payMode")+"&realPayMode="+paramMap.get("realPayMode")+"&provinceName="+paramMap.get("provinceName")+"&provincePCC="+paramMap.get("provincePCC")+"&cityName="+cityName+"&cityPCC="+cityName+"&countyName="+countyName+"&countyPCC="+countyPCC+"&provinceCode="+paramMap.get("provinceCode")+"&oldCityCode="+paramMap.get("oldCityCode")+"&cityCode="+paramMap.get("cityCode")+"&countyCode="+paramMap.get("countyCode")+"&postCode=100000&setAddress="+paramMap.get("setAddress")+"&insuredPersonnelAddress=display&receiveName="+paramMap.get("receiveName")+"&receiveAddress="+paramMap.get("receiveAddress")+"&invoice="+paramMap.get("invoice")+"&receiveMobile="+paramMap.get("receiveMobile")+"&sendDate="+paramMap.get("sendDate")+"&sendTime=01&isSendPolicy="+paramMap.get("isSendPolicy")+"&localProvinceCode="+paramMap.get("localProvinceCode")+"&isRenewal="+paramMap.get("isRenewal")+"&rt="+paramMap.get("rt")+"&ms="+paramMap.get("ms")+"&tbsn="+paramMap.get("tbsn")+"&processFlag="+paramMap.get("processFlag")+"&requestType="+paramMap.get("requestType")+"&pkgName="+paramMap.get("pkgName")+"&forcePolicyFlag="+paramMap.get("forcePolicyFlag")+"&bizPolicyFlag="+paramMap.get("bizPolicyFlag")+"&OD="+paramMap.get("OD")+"&NDNE="+paramMap.get("NDNE")+"&FEDPC="+paramMap.get("FEDPC")+"&CBD="+paramMap.get("CBD")+"&LOSI="+paramMap.get("LOSI")+"&FLOOD="+paramMap.get("FLOOD")+"&select_NDNE_request="+paramMap.get("select_NDNE_request")+"&select_FEDPC_request="+paramMap.get("select_FEDPC_request")+"&superiorStatus="+paramMap.get("superiorStatus")+"&newbieStatus="+paramMap.get("newbieStatus")+"&isBjSuperviseOpen="+paramMap.get("isBjSuperviseOpen")+"&isJZ="+paramMap.get("isJZ")+"&personnelName="+personnelName+"&certificateType=1&certificateNo="+paramMap.get("certificateNo")+"&mobileTelephone="+paramMap.get("mobileTelephone")+"&email="+paramMap.get("email");

        /* postParams="planDefineId=3&planDefineId2=1&ecInsureId="+paramMap.get("ecInsureId")+"&applyPolicyNo\n" +
                "=&policyNo=&lastPolicyNo=&isTaxSuspend=&pageInfo=temporaryInfo&isSave=false&preToInsuredInfo=false&isAgent\n" +
                "=3212&selectPayChannel=undefined&linkResource=&isRenewal=0&tempIds="+tempIds+"&flag=&phone=\n" +
                "&password=&payType=1&payMode=1&realPayMode=8&provinceName=&provincePCC=&cityName=%B1%B1%BE%A9%CA%D0&cityPCC\n" +
                "=%B1%B1%BE%A9%CA%D0&countyName=%CA%D0%C7%F8&countyPCC=%CA%D0%C7%F8&provinceCode=110000&oldCityCode=110100\n" +
                "&cityCode=110100&countyCode=110100&postCode=100000&setAddress=0&insuredPersonnelAddress=display&receiveName\n" +
                "=%D5%C5%CE%C4%BA%A3&receiveAddress=mentougou2412&invoice=%D5%C5%CE%C4%BA%A3&receiveMobile=18810253437\n" +
                "&sendDate=2016-07-01&sendTime=01&isSendPolicy=1&localProvinceCode=110000&isRenewal=0&rt=&ms=&tbsn=&processFlag\n" +
                "=1&requestType=calc&pkgName=free&forcePolicyFlag=1&bizPolicyFlag=1&OD=&NDNE=&FEDPC=&CBD=&LOSI=&FLOOD\n" +
                "=&select_NDNE_request=&select_FEDPC_request=&superiorStatus=&newbieStatus=&isBjSuperviseOpen=1&isJZ=1\n" +
                "&personnelName=%D5%C5%CE%C4%BA%A3&certificateType=1&certificateNo=132425196903135852&mobileTelephone\n" +
                "=18810253437&email=784506957%40qq.com";*/
        Map map = HttpsUtil.sendPost(url,postParams,super.cookieValue,"gb2312");
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
