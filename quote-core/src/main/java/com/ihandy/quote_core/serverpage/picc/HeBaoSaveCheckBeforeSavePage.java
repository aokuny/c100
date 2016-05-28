package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HeBaoSaveCheckBeforeSavePage extends BasePage {

    private static Logger logger = Logger.getLogger(HeBaoSaveCheckBeforeSavePage.class);

    public HeBaoSaveCheckBeforeSavePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap =(Map) request.getRequestParam().get("Map");
        String param ="";
        try{
            //prpCitemCar
            param = param + "prpCinsureds[2].insuredFlag="+paraMap.get("prpCinsureds[2].insuredFlag");
            param = param +"&"+ "prpCinsureds[2].insuredType="+paraMap.get("prpCinsureds[2].insuredType");
            param = param +"&"+ "prpCinsureds[2].insuredName="+paraMap.get("prpCinsureds[2].insuredName");
            param = param +"&"+ "prpCinsureds[2].identifyType="+paraMap.get("prpCinsureds[2].identifyType");
            param = param +"&"+ "prpCinsureds[2].identifyNumber="+paraMap.get("prpCinsureds[2].identifyNumber");
            param = param +"&"+ "prpCinsureds[2].mobile="+paraMap.get("prpCinsureds[2].mobile");
            param = param +"&"+ "prpCinsureds[2].flag="+paraMap.get("prpCinsureds[2].flag");
            param = param +"&"+ "prpCitemCar.engineNo="+paraMap.get("prpCitemCar.engineNo");
            param = param +"&"+ "prpCitemCar.carKindCode="+paraMap.get("prpCitemCar.carKindCode");
            param = param +"&"+ "prpCitemCar.clauseType="+paraMap.get("prpCitemCar.clauseType");
            param = param +"&"+ "prpCitemCar.frameNo="+paraMap.get("prpCitemCar.frameNo");
            param = param +"&"+ "prpCitemCar.licenseColorCode="+paraMap.get("prpCitemCar.licenseColorCode");
            param = param +"&"+ "prpCitemCar.licenseFlag="+paraMap.get("prpCitemCar.licenseFlag");
            param = param +"&"+ "prpCitemCar.licenseNo="+paraMap.get("prpCitemCar.licenseNo");
            param = param +"&"+ "prpCitemCar.licenseType="+paraMap.get("prpCitemCar.licenseType");
            param = param +"&"+ "prpCitemCar.modelCode="+paraMap.get("prpCitemCar.modelCode");
            param = param +"&"+ "prpCitemCar.vinNo="+paraMap.get("prpCitemCar.vinNo");
           //cmain
            param = param +"&"+ "prpCmain.endDate="+paraMap.get("prpCmain.endDate");
            param = param +"&"+ "prpCmain.endHour="+paraMap.get("prpCmain.endHour");
            param = param +"&"+ "prpCmain.proposalNo="+paraMap.get("prpCmain.proposalNo");
            param = param +"&"+ "prpCmain.riskCode="+paraMap.get("prpCmain.riskCode");
            param = param +"&"+ "prpCmain.startDate="+paraMap.get("prpCmain.startDate");
            param = param +"&"+ "prpCmain.startHour="+paraMap.get("prpCmain.startHour");
            param = param +"&"+ "prpCmain.sumPremium="+paraMap.get("prpCmain.sumPremium");
            // prpCmainCI
            param = param +"&"+ "prpCmainCI.endDate="+paraMap.get("prpCmainCI.endDate");
            param = param +"&"+ "prpCmainCI.endHour="+paraMap.get("prpCmainCI.endHour");
            param = param +"&"+ "prpCmainCI.proposalNo="+paraMap.get("prpCmainCI.proposalNo");
            param = param +"&"+ "prpCmainCI.startDate="+paraMap.get("prpCmainCI.startDate");
            param = param +"&"+ "prpCmainCI.startHour="+paraMap.get("prpCmainCI.startHour");
            param = param +"&"+ "prpCmainCI.sumPremium="+paraMap.get("prpCmain.sumPremium");
            param = param +"&"+ "prpCmainCommon.DBCFlag="+paraMap.get("prpCmainCommon.DBCFlag");
        }catch(Exception e) {
            logger.info("抓取机器人，【 PICC 核保保存5获取post参数失败】");
        }
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        //解析2,TDAA201611020000300881=TDZA201611020000308237,|
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存5失败】");
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
        Response response = getResponse(html);
        Map requestMap = request.getRequestParam();
        Map returnMap =  response.getResponseMap();
        Map nextMap =(Map) response.getResponseMap().get("nextParams");
        Map paramsMap = (Map) request.getRequestParam().get("Map");
        String  paramsStr = request.getRequestParam().get("String").toString();
        
       // paramsStr.replace("PayRefReason=", "");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it = key.iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            String keyValue = nextMap.get(keyName).toString();
            try{
            keyName = java.net.URLEncoder.encode(keyName, "gbk");
            }catch(Exception e){
            	
            }   
            if(paramsStr.contains(keyName+"=&")){
            	//paramsStr = paramsStr.replace(keyName+"=", keyName+"="+keyValue);
            }
            if(paramsMap.containsKey(keyName)){
            //	paramsMap.put(keyName, keyValue);
            }    
        }
        requestMap.put("String",paramsStr);
        requestMap.put("Map",paramsMap);
        
        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }
}
