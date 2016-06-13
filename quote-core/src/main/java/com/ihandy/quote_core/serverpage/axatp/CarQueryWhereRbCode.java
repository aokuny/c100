package com.ihandy.quote_core.serverpage.axatp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * Created by fengwen on 2016/6/8.
 */
public class CarQueryWhereRbCode extends BasePage {
    private static Logger logger = Logger.getLogger(CarQueryWhereRbCode.class);

    public CarQueryWhereRbCode(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();

        String urlparam ="";

        urlparam = urlparam+"rbCode="+paramMap.get("rbCode")+"&";
       // urlparam = urlparam+"rbCode=XDABJD0010&";
        urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        urlparam = urlparam+"isRenewal="+paramMap.get("isRenewal")+"&";
        urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
        urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
        urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
        urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
        urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
        urlparam = urlparam+"ms="+paramMap.get("ms");




        url = url+"?"+urlparam;
        Map map = HttpsUtil.sendGet(url,super.cookieValue,"gb2312");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map nextParamsMap = new HashMap<>();
            nextParamsMap = request.getRequestParam();
            JSONObject carInfo =JSON.parseObject(html);
            JSONArray carInfoArr =(JSONArray) carInfo.get("data");
            JSONObject car = (JSONObject)carInfoArr.get(0);
            String vehicleId = car.get("id").toString();
            nextParamsMap.put("vehicleId",vehicleId);

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
