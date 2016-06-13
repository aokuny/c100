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
public class ApplyQueryPage extends BasePage {
    private static Logger logger = Logger.getLogger(ApplyQueryPage.class);

    public ApplyQueryPage(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap =request.getRequestParam();
        String param = "";
       /* http://dm.axatp.com/applyQuery.do?isJZ=1&ecInsureId=E1E65AD17E74DCCB6C352AC9787FF1604954DCB8087C8760&cityCode=110100&autoModeCode=XDABJD0010&tbsn=*/
        param = param+"isJZ="+paramMap.get("isJZ")+"&";
        param = param+"ecInsureId="+paramMap.get("ecInsureId")+"&";
        param = param+"cityCode="+paramMap.get("cityCode")+"&";
        param = param+"autoModeCode="+paramMap.get("rbCode")+"&";
        param = param+"tbsn="+paramMap.get("tbsn")+"&";

        url = url+"?"+param;

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
           // String newEcInsureId =  doc.getElementById("ecInsureId").attributes().get("value").toString();
           // nextParamsMap.put("ecInsureId", newEcInsureId);


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
