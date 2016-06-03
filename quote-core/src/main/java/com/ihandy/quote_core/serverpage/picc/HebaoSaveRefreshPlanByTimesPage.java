package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveRefreshPlanByTimesPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveRefreshPlanByTimesPage.class);

    public HebaoSaveRefreshPlanByTimesPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        String  paramsStr = request.getRequestParam().get("String").toString();
        Map map = HttpsUtil.sendPost(url,paramsStr,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html,Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = new JSONArray();
                jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                JSONArray data = (JSONArray) map1.get("data");


                //上个请求返回的参数继续传递下去

                String  paramsStr = request.getRequestParam().get("String").toString();
                // int ii=0;
                //1）组装 prpCplanTemps
                Map nextParamsMap1 = new LinkedHashMap<>();
                for(int i=0;i<data.size();i++){
                    Map mapPrpCplanTemps = (Map)data.get(i);
                    Map expireDate = (Map) mapPrpCplanTemps.get("planDate");
                    String year1 =Integer.parseInt((Integer.parseInt(expireDate.get("year").toString()) + 1900)+"")+"";
                    int month = Integer.parseInt(expireDate.get("month").toString()) + 1;
                    int day = Integer.parseInt(expireDate.get("date").toString());
                    String expireDateStr = year1 + "-" + month + "-" + day;
                    if(i==0){
                        nextParamsMap.put("prpCplanTemps["+i+"].currency",mapPrpCplanTemps.get("currency").toString().split("\\,")[0]);
                        nextParamsMap.put("prpCplanTemps["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
                        nextParamsMap.put("prpCplanTemps["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
                        nextParamsMap.put("prpCplanTemps["+i+"].flag",mapPrpCplanTemps.get("flag"));
                        nextParamsMap.put("prpCplanTemps["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
                        if(mapPrpCplanTemps.get("netPremium").toString().equals("")||mapPrpCplanTemps.get("netPremium").toString().equals("null")){
                            nextParamsMap.put("prpCplanTemps["+i+"].netPremium","");
                        }else{
                            nextParamsMap.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                        }
                        //nextParamsMap.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                        nextParamsMap.put("prpCplanTemps["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
                        nextParamsMap.put("prpCplanTemps["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
                        nextParamsMap.put("prpCplanTemps["+i+"].planDate",expireDateStr);
                        nextParamsMap.put("prpCplanTemps["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
                        nextParamsMap.put("prpCplanTemps["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
                        nextParamsMap.put("prpCplanTemps["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));

                    }else{
                        nextParamsMap1.put("prpCplanTemps["+i+"].currency",mapPrpCplanTemps.get("currency").toString().split("\\,")[0]);
                        nextParamsMap1.put("prpCplanTemps["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].flag",mapPrpCplanTemps.get("flag"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
                        if(mapPrpCplanTemps.get("netPremium").toString().equals("")||mapPrpCplanTemps.get("netPremium").toString().equals("null")){
                            nextParamsMap1.put("prpCplanTemps["+i+"].netPremium","");
                        }else{
                            nextParamsMap1.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                        }
                        //nextParamsMap.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].planDate",expireDateStr);
                        nextParamsMap1.put("prpCplanTemps["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
                        nextParamsMap1.put("prpCplanTemps["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));

                    }

                    if(mapPrpCplanTemps.get("taxPremium").toString().equals("")||mapPrpCplanTemps.get("taxPremium").toString().equals("null")){
                        nextParamsMap.put("prpCplanTemps["+i+"].taxPremium","");
                    }else{
                        nextParamsMap.put("prpCplanTemps["+i+"].taxPremium", mapPrpCplanTemps.get("taxPremium"));
                    }
                    String payReasonName = mapPrpCplanTemps.get("payReasonName").toString();
                    payReasonName =  java.net.URLEncoder.encode(payReasonName, "gb2312");
                    if(payReasonName.contains("%28")){
                        payReasonName = payReasonName.replace("%28", "(");
                        payReasonName = payReasonName.replace("%29", ")");
                    }
                    String currency ="人民币";
                    try{
                        currency = mapPrpCplanTemps.get("currency").toString().split(",")[1];
                    }catch(Exception e){}
                    currency = java.net.URLEncoder.encode(currency, "gb2312");

                    nextParamsMap.put("cplan["+i+"].payReasonC",payReasonName);// value=(%C7%BF%D6%C6)%CA%D5%B1%A3%B7%D1
                    nextParamsMap.put("description["+i+"].currency",currency);// value=%C8%CB%C3%F1%B1%D2

                    nextParamsMap.put("cplans["+i+"].planFee",mapPrpCplanTemps.get("planFee"));// value=580.83
                    nextParamsMap.put("cplans["+i+"].backPlanFee",mapPrpCplanTemps.get("planFee"));// value=580.83

                    if(mapPrpCplanTemps.get("isBICI").toString().equals("BI")){
                        nextParamsMap.put("cplans_[0].planFee",mapPrpCplanTemps.get("planFee"));
                        nextParamsMap.put("cplans_[0].backPlanFee",mapPrpCplanTemps.get("planFee"));
                        nextParamsMap.put("cplan_[0].payReasonC",payReasonName);
                        nextParamsMap.put("description_[0].currency",currency);

                        nextParamsMap.put("prpCplanTemps_[0].payNo", mapPrpCplanTemps.get("payNo"));
                        nextParamsMap.put("prpCplanTemps_[0].serialNo",mapPrpCplanTemps.get("serialNo"));
                        nextParamsMap.put("prpCplanTemps_[0].endorseNo",mapPrpCplanTemps.get("endorseNo"));

                        nextParamsMap.put("prpCplanTemps_[0].payReason",mapPrpCplanTemps.get("payReason"));
                        nextParamsMap.put("prpCplanTemps_[0].planDate",expireDateStr);

                        nextParamsMap.put("prpCplanTemps_[0].currency",mapPrpCplanTemps.get("currency").toString().split("\\,")[0]);
                        nextParamsMap.put("prpCplanTemps_[0].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));

                        nextParamsMap.put("prpCplanTemps_[0].flag",mapPrpCplanTemps.get("flag"));
                        nextParamsMap.put("prpCplanTemps_[0].isBICI",mapPrpCplanTemps.get("isBICI"));
                        nextParamsMap.put("prpCplanTemps_[0].netPremium",mapPrpCplanTemps.get("netPremium"));
                        nextParamsMap.put("prpCplanTemps_[0].planFee", mapPrpCplanTemps.get("planFee"));
                        nextParamsMap.put("prpCplanTemps_[0].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));
                        nextParamsMap.put("prpCplanTemps_[0].taxPremium", mapPrpCplanTemps.get("taxPremium"));
                    }
                }
                String starParam1="planfee_index=1";
                //先删除prpCcommissionsTemp[0]

                // String startParam33="prpCcommissionsTemp_%5B0%5D.costType=&prpCcommissionsTemp_%5B0%5D.riskCode=&prpCcommissionsTemp_%5B0%5D.currency=AED&prpCcommissionsTemp_%5B0%5D.adjustFlag=0&prpCcommissionsTemp_%5B0%5D.upperFlag=0&prpCcommissionsTemp_%5B0%5D.auditRate=&prpCcommissionsTemp_%5B0%5D.auditFlag=1&prpCcommissionsTemp_%5B0%5D.sumPremium=&prpCcommissionsTemp_%5B0%5D.costRate=&prpCcommissionsTemp_%5B0%5D.costRateUpper=&prpCcommissionsTemp_%5B0%5D.coinsRate=100&prpCcommissionsTemp_%5B0%5D.coinsDeduct=1&prpCcommissionsTemp_%5B0%5D.costFee=&prpCcommissionsTemp_%5B0%5D.agreementNo=&prpCcommissionsTemp_%5B0%5D.configCode=&";
                if(paramsStr.contains(starParam1)){
                    String param1 = StringBaseUtils.addParam(starParam1,nextParamsMap1)+"&";
                    //  System.out.println("param1 = "+param1);
                    paramsStr = paramsStr.replace(starParam1, param1);
                }else{
                    paramsStr = paramsStr+"&"+StringBaseUtils.Map2StringURLEncoder(nextParamsMap1)+"&";
                }

                returnMap.put("String",paramsStr);
                // System.out.println("paramsStr = "+paramsStr);
                returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存4失败】");
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
        Map returnMap =  response.getResponseMap();
        Map nextMap =(Map)returnMap.get("nextParams");
        Map paramsMap = (Map) request.getRequestParam().get("Map");
        // String  paramsStr = request.getRequestParam().get("String").toString();
        String  paramsStr = returnMap.get("String").toString();
        // System.out.println("paramsStr3="+paramsStr);
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it = key.iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            String keyValue = nextMap.get(keyName).toString();
            try{
                keyName = java.net.URLEncoder.encode(keyName, "gbk");
            }catch(Exception e){

            }

            if(keyName.equals("prpCplanTemps%5B0%5D.serialNo")||keyName.equals("prpCplanTemps%5B0%5D.subsidyRate")||keyName.equals("prpCplanTemps%5B0%5D.delinquentFee")||keyName.equals("prpCplanTemps%5B0%5D.planFee") || keyName.equals("prpCplanTemps%5B0%5D.planDate") ||keyName.equals("prpCplanTemps%5B0%5D.isBICI")||keyName.equals("prpCplanTemps_%5B0%5D.isBICI")||keyName.equals("prpCplanTemps%5B0%5D.payReason")){
                if(paramsMap.containsKey(keyName)){

                    String oldValue ="";
                    try{oldValue = paramsMap.get(keyName).toString();}
                    catch(Exception e){}
                    String old1 = keyName+"="+oldValue+"&";
                    String new1 = keyName+"="+keyValue+"&";
                    paramsStr = paramsStr.replace(old1, new1);
                }else{
                    String oldValue ="";

                    String old1 = keyName+"="+oldValue+"&";
                    String new1 = keyName+"="+keyValue+"&";
                    paramsStr = paramsStr.replace(old1, new1);
                }
            }
            else if(keyName.equals("PayRefReason")){
                if(paramsMap.containsKey(keyName)){

                    String oldValue ="";
                    try{oldValue = paramsMap.get(keyName).toString();}
                    catch(Exception e){}
                    String old1 = keyName+"="+oldValue+"&";

                    paramsStr = paramsStr.replace(old1, "");// delete PayRefReason
                }
            }
            else{
                if(paramsStr.contains(keyName+"=&")){
                    if(keyValue.equals("null")||keyValue.equals("")){

                    }else{
                        paramsStr = paramsStr.replace(keyName+"=&", keyName+"="+keyValue+"&");
                    }

                }else if(paramsStr.contains(keyName+"="+keyValue+"&")){

                }else{
                    String oldStr ="&planStr=";
                    paramsStr = paramsStr.replace(oldStr, "&"+keyName+"="+keyValue+oldStr);
                }
                if(paramsMap.containsKey(keyName)){
                    paramsMap.put(keyName, keyValue);
                }  else{
                    paramsMap.put(keyName, keyValue);
                }
            }
        }
        paramsStr =paramsStr.replace("&planFlag=0&", "&planFlag=1&");// planFlag
        paramsStr =paramsStr.replace("&prpCmainCar.agreeDriverFlag=&", "&prpCmainCar.agreeDriverFlag=0&");//prpCmainCar.agreeDriverFlag
        paramsStr =paramsStr.replace("&prpCmainCommon.groupFlag=0&", "&prpCmainCommon.groupFlag=2&");//prpCmainCommon.groupFlag
        try{
            paramsStr =paramsStr.replace("&prpCsettlement.flag=&", "&prpCsettlement.flag=0&"); //prpCsettlement.flag value
        }catch(Exception e){
            paramsStr = paramsStr+"&prpCsettlement.flag=0";
        }

        requestMap.put("String",paramsStr);
        requestMap.put("Map",paramsMap);

        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }


}
