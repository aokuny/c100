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
public class PayRequestInitPage extends BasePage {
    private static Logger logger = Logger.getLogger(PayRequestInitPage.class);

    public PayRequestInitPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();

        String cityName=paramMap.get("cityName").toString();
        try {
            cityName = java.net.URLEncoder.encode(cityName , "utf-8");
        }catch (Exception e){}
   /*     http://dm.axatp.com/payRequestInit.do?planDefineId=3&linkResource=&cityName=%E5%8C%97%E4%BA%AC%E5%B8%82&ms=&cityCode=110100&ecInsureId=E1E65AD17E74DCCB1C7ED258401BBF1D78EB43D6293FE5BF&pagereferrer=%E5%9C%B0%E9%9D%A2%E8%90%A5%E9%94%80-%E6%8E%A8%E8%8D%90&isAgent=3212&rt=&localProvinceCode=110000*/

        String urlparam = "";
        urlparam = urlparam+"planDefineId="+paramMap.get("planDefineId")+"&";
        urlparam = urlparam+"linkResource="+paramMap.get("linkResource")+"&";
        urlparam = urlparam+"cityName="+cityName+"&";
        urlparam = urlparam+"ms="+paramMap.get("ms")+"&";
        urlparam = urlparam+"cityCode="+paramMap.get("cityCode")+"&";
        urlparam = urlparam+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        urlparam = urlparam+"pagereferrer="+paramMap.get("pagereferrer")+"&";
        urlparam = urlparam+"isAgent="+paramMap.get("isAgent")+"&";
        urlparam = urlparam+"rt="+paramMap.get("rt")+"&";
        urlparam = urlparam+"localProvinceCode="+paramMap.get("localProvinceCode")+"&";
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
