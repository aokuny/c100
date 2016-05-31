package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HeBaoSaveCheckAgentTypePage extends BasePage {

    private static Logger logger = Logger.getLogger(HeBaoSaveCheckAgentTypePage.class);

    public HeBaoSaveCheckAgentTypePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = (Map)request.getRequestParam().get("Map");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String param ="businessNature="+paraMap.get("prpCmain.businessNature").toString() +
                "validDate="+ sdf.format(new Date())+
                "&agentCode="+paraMap.get("agentCode").toString() +
                "&comCode="+paraMap.get("comCode").toString()+
                "&riskCode=DAA,DZA";
        url = url+"?"+param;
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html,Request ruquest) {
        //解析{"msg":"SINGLE,,66692393-6,","totalRecords":0,"data":[]}
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                String msg =  map1.get("msg").toString();
                String[] strArr = msg.split(",");
                if(strArr[0].equals("SINGLE")){
                    nextParamsMap.put("certificateNo",strArr[2]);
                    returnMap.put("nextParams",nextParamsMap);
                    response.setResponseMap(returnMap);
                    response.setReturnCode(SysConfigInfo.SUCCESS200);
                    response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
                }else{
                    logger.info("抓取机器人，【 PICC 核保保存2失败】");
                    response.setResponseMap(null);
                    response.setReturnCode(SysConfigInfo.ERROR404);
                    response.setErrMsg(SysConfigInfo.ERROR404MSG);
                }
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存2失败】");
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
        Response response = getResponse(html,request);
        //上个请求返回的参数继续传递下去
        Map requestMap = request.getRequestParam();
        String paramstr = requestMap.get("String").toString();
        Map map = (Map)requestMap.get("Map");

        Map returnMap =  response.getResponseMap();
        Map nextMap =(Map) returnMap.get("nextParams");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it = key.iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            String keyValue = nextMap.get(keyName).toString();

            map.put(keyName,keyValue);
            if(paramstr.contains(keyName+"=&")){
                paramstr = paramstr.replace(keyName+"=", keyName+"="+keyValue);
            }
        }
        requestMap.put("String", paramstr);
       // System.out.println("paramsData = "+paramstr);
        requestMap.put("Map",map);
        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }
}
