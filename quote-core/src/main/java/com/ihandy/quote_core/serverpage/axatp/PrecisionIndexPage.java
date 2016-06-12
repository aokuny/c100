package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/6/6.
 */
public class PrecisionIndexPage extends BasePage {
    private static Logger logger = Logger.getLogger(PrecisionIndexPage.class);
   /* String cookieValue="s_sess=%20s_cc%3Dtrue%3B%20s_ppv%3D100%252C9%252C1530%3B%20s_sq%3Daxatp-prd%253D%252526pid%25253D%252525E7%25252599%252525BB%252525E5%252525BD%25252595%252526pidt%25253D1%252526oid%25253D%252525E7%25252599%252525BB%252525E5%252525BD%25252595%252526oidt%25253D3%252526ot%25253DSUBMIT%3B;s_pers=%20s_nr%3D1465357522775-New%7C1496893522775%3B; 3J6CYNPiAN=MDAwM2IyNTIwMDgwMDAwMDA1MjYwEV5+GjYxNDY1Mzg0MTM4;JSESSIONID=8B46F57699BD9814DEA86636A24E5FBE; tianpingSessionID=mqHjJUDyqN1iUzqAAe1rNJYIA7Y=;platformType=005;tpVisitorId=3a24337f-0644-4633-8ab3-70be08cdb7a5;title=%u7528%u6237%u767B%u5F55_%u5B89%u76DB%u5929%u5E73_%u4E2D%u56FD%u7B2C%u4E00%u5BB6%u4E13%u4E1A%u6C7D%u8F66%u4FDD%u9669%u516C%u53F8;operationgSystem=other;isVip=false;loginPass=4QrcOUm6Wau+VuBX8g+IPg==;loginedTime=08114347838;loginedName=jtl_bj;";*/
    public PrecisionIndexPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = new LinkedHashMap<>();
        Map loginParamMap = super.axatpMap;
        for (Object key : loginParamMap.keySet()) {
            paramMap.put(key, loginParamMap.get(key));
        }
        String param="";
        param = StringBaseUtils.Map2GetParam(paramMap);
        url = url+"?"+param;
        String cookieValue = super.cookieValue;
        Map map = HttpsUtil.sendGetForAxatp(url,cookieValue,"UTF-8");
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
