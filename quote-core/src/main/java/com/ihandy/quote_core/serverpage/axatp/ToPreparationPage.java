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
 * Created by fengwen on 2016/6/6.
 */
public class ToPreparationPage extends BasePage{

    private static Logger logger = Logger.getLogger(ToPreparationPage.class);

    public ToPreparationPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();
        /*http://dm.axatp.com/toPreparation.do?cityCode=110100&cityCode_ajax=&localProvinceCode=110000&localCityCode=&departmentCode=33&linkResource=&selectPayChannel=&isAgent=3212&isRenewal=0&ecInsureId=&planDefineId=3&rt=0&ms=&renewalFlag=&selectCityPage=selectCityPage&insuranceBeginTimeEnd=&pageInfo=selectCity&carcity=&pagereferrer=%E5%9C%B0%E9%9D%A2%E8%90%A5%E9%94%80-%E6%8E%A8%E8%8D%90&cityName=&infoValue=userId%3D7124003%26loginname%3Djtl_bj%26grade%3D1%26name%3Djtl_bj%26mobileNo%3Dnull%26memo%3Dnull%26timestamp%3D1465350347117%26hashCode%3Dda369b539938739a30999811279bbb90&prohibitValue=&isJZ=1&select_city=±±¾©ÊÐ&licenceNo=¾©MM3767&personnelName=ÕÅÎÄº£*/
        String urlparam ="";
        urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
        urlparam = urlparam+"cityCode_ajax="+paramMap.get("cityCode_ajax")+"&";
        urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
        urlparam = urlparam+"localCityCode="+paramMap.get("localCityCode")+"&";
        urlparam = urlparam+"departmentCode="+paramMap.get("departmentCode")+"&";
        urlparam = urlparam+"linkResource="+paramMap.get("linkResource")+"&";
        urlparam = urlparam+"selectPayChannel="+paramMap.get("selectPayChannel")+"&";
        urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
        urlparam = urlparam+"isRenewal="+paramMap.get("isRenewal")+"&";
        urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
        urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
        urlparam = urlparam+"ms="+paramMap.get("ms")+"&";
        urlparam = urlparam+"renewalFlag="+paramMap.get("renewalFlag");
        urlparam = urlparam+"selectCityPage="+paramMap.get("selectCityPage")+"&";
        urlparam = urlparam+"insuranceBeginTimeEnd="+paramMap.get("insuranceBeginTimeEnd")+"&";
        urlparam = urlparam+"pageInfo="+paramMap.get("pageInfo")+"&";
        urlparam = urlparam+"carcity="+paramMap.get("carcity")+"&";
        urlparam = urlparam+"pagereferrer="+paramMap.get("pagereferrer")+"&";
        urlparam = urlparam+"cityName="+paramMap.get("cityName")+"&";
        urlparam = urlparam+"infoValue="+paramMap.get("infoValue")+"&";
        urlparam = urlparam+"prohibitValue="+paramMap.get("prohibitValue")+"&";
        urlparam = urlparam+"isJZ="+paramMap.get("isJZ")+"&";

        urlparam = urlparam+"select_city="+paramMap.get("select_city")+"&";
        String licenceNo = paramMap.get("licenceNo").toString();
        try {
            licenceNo = java.net.URLEncoder.encode(licenceNo , "gbk");
        }catch (Exception e){}
        urlparam = urlparam+"licenceNo="+licenceNo+"&";
        String name = paramMap.get("personnelName").toString();
        try {
            name = java.net.URLEncoder.encode(name , "gbk");
        }catch (Exception e){}
        urlparam = urlparam+"personnelName="+name;

        url = url+"?"+urlparam;

        Map map = HttpsUtil.sendGet(url,super.cookieValue,"gb2312");
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
