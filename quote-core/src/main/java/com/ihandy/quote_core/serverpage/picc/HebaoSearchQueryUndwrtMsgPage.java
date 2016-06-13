package com.ihandy.quote_core.serverpage.picc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;

public class HebaoSearchQueryUndwrtMsgPage extends BasePage {
    private static Logger logger = Logger.getLogger(HebaoSearchQueryUndwrtMsgPage.class);

    public HebaoSearchQueryUndwrtMsgPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        String param="";
        param = StringBaseUtils.Map2GetParam(request.getRequestParam());
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"gb2312");
        html = map.get("html").toString();
        //System.out.println("html = "+html);
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            html = html.replaceAll("\r|\n|\t", "");

            Document doc = Jsoup.parse(html);


            Map  returnMap  = new HashMap<>();
            Map nextParamMap = new LinkedHashMap<>();
            try{
                Elements trs = doc.getElementById("insertUndwrtRow").select("tr");


                for(int i=1;i<trs.size();i++){
                    Elements tds = trs.get(i).select("td");
                    Element  no = tds.get(0);
                    Element  msg = tds.get(3);
                    Element time = tds.get(1);
                    nextParamMap.put("no", no.childNode(1).attributes().get("value"));
                    nextParamMap.put("time", time.childNode(1).attributes().get("value"));
                    Node node = msg.childNode(1);
                    String msg1 = node.toString();
                    msg1 =  msg1.split(">")[1].split("<")[0];
                    nextParamMap.put("msg", msg1);
                }
                returnMap.put("nextParams",nextParamMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                response.setResponseMap(null);
                response.setReturnCode(SysConfigInfo.ERROR404);
                response.setErrMsg(SysConfigInfo.ERROR404MSG);
            }
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
