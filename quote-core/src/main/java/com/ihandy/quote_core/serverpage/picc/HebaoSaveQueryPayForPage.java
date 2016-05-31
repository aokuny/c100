package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveQueryPayForPage  extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveQueryPayForPage.class);

    public HebaoSaveQueryPayForPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = (Map) request.getRequestParam().get("Map");
        String  paramsStr = request.getRequestParam().get("String").toString();
        //agreementNo=&riskCode=DAA&comCode=11010286&chgCostRate=1
        String param ="agreementNo=" +
                "&chgCostRate=1" +
                "&comCode="+paraMap.get("comCode").toString()+
                "&riskCode="+paraMap.get("prpCmain.riskCode").toString();
        url = url+"?"+param;
        //System.out.println("rightData"+param1);
        //System.out.println("errorData"+paramsStr);
        // compareStringDifference(param1,paramsStr);
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

            //上个请求返回的参数继续传递下去
            Map paramsPreMap = (Map) request.getRequestParam().get("Map");
            //  paramsPreMap
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = new JSONArray();
                jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                JSONArray data = (JSONArray) map1.get("data");
                Map dataMap = (Map) data.get(0);
                //1）组装prpDdismantleDetails
                JSONArray jsonArrayPrpDdismantleDetails =JSONArray.fromObject(dataMap.get("prpDdismantleDetails"));
                for(int i=0;i<jsonArrayPrpDdismantleDetails.size();i++){
                    Map mapPrpDdismantleDetails = (Map)jsonArrayPrpDdismantleDetails.get(i);

                    JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpDdismantleDetails.get("id"));
                    Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.agreementNo", mapId.get("agreementNo").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.roleCode", mapId.get("roleCode").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.configCode",mapId.get("configCode").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.assignType",mapId.get("assignType").toString());

                    nextParamsMap.put("prpDdismantleDetails["+i+"].flag",mapPrpDdismantleDetails.get("flag").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].costRate",mapPrpDdismantleDetails.get("costRate").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].businessNature",mapPrpDdismantleDetails.get("businessNature").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleFlag",mapPrpDdismantleDetails.get("roleFlag").toString());
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleName",mapPrpDdismantleDetails.get("roleName").toString());
                    if(mapPrpDdismantleDetails.get("flag").toString().equals("DZA")){
                        String roleName ="";
                        try{
                            roleName =mapPrpDdismantleDetails.get("roleName").toString();
                            roleName = java.net.URLEncoder.encode(roleName, "gbk");
                        }catch(Exception e){}

                        nextParamsMap.put("prpDdismantleDetails_[0].flag",mapPrpDdismantleDetails.get("flag").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].costRate",mapPrpDdismantleDetails.get("costRate").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].businessNature",mapPrpDdismantleDetails.get("businessNature").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].roleFlag",mapPrpDdismantleDetails.get("roleFlag").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].roleName",roleName);
                        nextParamsMap.put("prpDdismantleDetails_[0].id.agreementNo", mapId.get("agreementNo").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].id.roleCode", mapId.get("roleCode").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].id.configCode",mapId.get("configCode").toString());
                        nextParamsMap.put("prpDdismantleDetails_[0].id.assignType",mapId.get("assignType").toString());
                    }
                }
                //2）组装prpCsaless(6)
                JSONArray jsonArrayPrpCsaless =JSONArray.fromObject(dataMap.get("prpCsaless"));
                for(int i=0;i<jsonArrayPrpCsaless.size();i++){
                    Map mapPrpCsaless = (Map)jsonArrayPrpCsaless.get(i);
                    JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpCsaless.get("id"));
                    Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
                    nextParamsMap.put("prpCsaless["+i+"].id.proposalNo", mapId.get("proposalNo").toString());
                    nextParamsMap.put("prpCsaless["+i+"].id.salesCode", mapId.get("salesCode").toString());
                    nextParamsMap.put("prpCsaless["+i+"].id.salesDetailCode",mapId.get("salesDetailCode").toString());

                    nextParamsMap.put("prpCsaless["+i+"].agreementNo",mapPrpCsaless.get("agreementNo").toString());
                    nextParamsMap.put("prpCsaless["+i+"].flag",mapPrpCsaless.get("flag").toString());
                    nextParamsMap.put("prpCsaless["+i+"].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber").toString());
                    nextParamsMap.put("prpCsaless["+i+"].remark",mapPrpCsaless.get("remark").toString());
                    nextParamsMap.put("prpCsaless["+i+"].riskCode",mapPrpCsaless.get("riskCode"));
                    nextParamsMap.put("prpCsaless["+i+"].salesDetailName",mapPrpCsaless.get("salesDetailName").toString());
                    nextParamsMap.put("prpCsaless["+i+"].salesName",mapPrpCsaless.get("salesName").toString());
                    nextParamsMap.put("prpCsaless["+i+"].splitFee",mapPrpCsaless.get("splitFee").toString());
                    nextParamsMap.put("prpCsaless["+i+"].splitRate",mapPrpCsaless.get("splitRate").toString());
                    nextParamsMap.put("prpCsaless["+i+"].splitWay",mapPrpCsaless.get("splitWay").toString());
                    nextParamsMap.put("prpCsaless["+i+"].totalRate",mapPrpCsaless.get("totalRate").toString());
                    nextParamsMap.put("prpCsaless["+i+"].totalRateMax",mapPrpCsaless.get("totalRateMax").toString());
                    if(mapPrpCsaless.get("riskCode").toString().equals("DAA")){

                        String salesName ="";
                        String salesDetailName="";
                        try{
                            salesName =mapPrpCsaless.get("salesName").toString();
                            salesDetailName = mapPrpCsaless.get("salesDetailName").toString();
                            salesName = java.net.URLEncoder.encode(salesName, "gbk");
                            salesDetailName =  java.net.URLEncoder.encode(salesDetailName, "gbk");
                        }catch(Exception e){}


                        nextParamsMap.put("prpCsaless_[0].agreementNo",mapPrpCsaless.get("agreementNo").toString());
                        nextParamsMap.put("prpCsaless_[0].flag",mapPrpCsaless.get("flag").toString());
                        nextParamsMap.put("prpCsaless_[0].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber").toString());
                        nextParamsMap.put("prpCsaless_[0].remark",mapPrpCsaless.get("remark").toString());
                        nextParamsMap.put("prpCsaless_[0].riskCode",mapPrpCsaless.get("riskCode").toString());
                        nextParamsMap.put("prpCsaless_[0].salesDetailName",salesDetailName);
                        nextParamsMap.put("prpCsaless_[0].salesName",salesName);
                        nextParamsMap.put("prpCsaless_[0].splitFee",mapPrpCsaless.get("splitFee").toString());
                        nextParamsMap.put("prpCsaless_[0].splitRate",mapPrpCsaless.get("splitRate").toString());
                        nextParamsMap.put("prpCsaless_[0].splitWay",mapPrpCsaless.get("splitWay").toString());
                        nextParamsMap.put("prpCsaless_[0].totalRate",mapPrpCsaless.get("totalRate").toString());
                        nextParamsMap.put("prpCsaless_[0].totalRateMax",mapPrpCsaless.get("totalRateMax").toString());
                        nextParamsMap.put("prpCsaless_[0].id.proposalNo", mapId.get("proposalNo").toString());
                        nextParamsMap.put("prpCsaless_[0].id.salesCode", mapId.get("salesCode").toString());
                        nextParamsMap.put("prpCsaless_[0].id.salesDetailCode",mapId.get("salesDetailCode").toString());
                    }
                }
                //3)
                nextParamsMap.put("maxRateScmCi",dataMap.get("maxRateScmCi").toString());
                nextParamsMap.put("maxRateScm",dataMap.get("maxRateScm").toString());
                nextParamsMap.put("levelMaxRateCi",dataMap.get("levelMaxRateCi").toString());
                nextParamsMap.put("levelMaxRate",dataMap.get("levelMaxRate").toString());

                //4)组装prpDpayForPolicies
                JSONArray jsonArrayPrpDpayForPolicies =JSONArray.fromObject(dataMap.get("prpDpayForPolicies"));
                for(int i=0;i<jsonArrayPrpDpayForPolicies.size();i++){
                    Map mapPrpDpayForPolicies = (Map)jsonArrayPrpDpayForPolicies.get(i);
                  /*  "prpDagreement": null,
                    "agentCode_uni": "",
                    "customerGroupCode": "",
                    "coinsDeduct": "",
                    "auditFlag": "",
                    "costType": "2",
                    "flag": "",
                    "id": {
                        "agreementNo": "RULE20130000000023071",
                        "seriseNo": 1,
                        "configCode": "PUB"
                    },
                    "kindFlag": "",
                    "currency": "",
                    "costRate": 25,
                    "costRateUpper": 35,
                    "operateTimeForHis": null,
                    "adjustFlag": "0",
                    "riskCode": "DAA",
                    "costUpper": 0,
                    "remark": "",
                    "upperFlag": "",
                    "insertTimeForHis": null*/
                    JSONArray jsonArrayPrpDpayForPoliciesId =JSONArray.fromObject(mapPrpDpayForPolicies.get("id"));
                    Map mapId = (Map)jsonArrayPrpDpayForPoliciesId.get(0);

                    nextParamsMap.put("prpCcommissionsTemp["+i+"].costType",mapPrpDpayForPolicies.get("costType").toString());  // value=2
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].riskCode",mapPrpDpayForPolicies.get("riskCode").toString());  // value=DZA
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].adjustFlag",mapPrpDpayForPolicies.get("adjustFlag").toString());  // value=0
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].auditRate","");  // value=null
                    if(mapPrpDpayForPolicies.get("riskCode").equals("DZA")){//CI
                        nextParamsMap.put("prpCcommissionsTemp["+i+"].sumPremium", paramsPreMap.get("prpCmainCI.sumPremium").toString());  // value=580.83
                        nextParamsMap.put("prpCcommissionsTemp["+i+"].costFee",StringBaseUtils.forDight(Double.parseDouble(paramsPreMap.get("prpAnciInfo.operSellExpensesAmountCI").toString()),2));  // value=23.23
                    }else{
                        nextParamsMap.put("prpCcommissionsTemp["+i+"].sumPremium",  paramsPreMap.get("prpCmain.sumPremium").toString());  // value=2704.63
                        nextParamsMap.put("prpCcommissionsTemp["+i+"].costFee",StringBaseUtils.forDight(Double.parseDouble(paramsPreMap.get("prpAnciInfo.operateCommBI").toString()),2));  // value=676.16
                    }

                    nextParamsMap.put("prpCcommissionsTemp["+i+"].costRate",mapPrpDpayForPolicies.get("costRate").toString());  // value=4
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].costRateUpper",mapPrpDpayForPolicies.get("costRateUpper").toString());  // value=4
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].coinsRate","100");  // value=100 TODO test

                    nextParamsMap.put("prpCcommissionsTemp["+i+"].agreementNo",mapId.get("agreementNo").toString());  // value=RULE20130000000023072
                    nextParamsMap.put("prpCcommissionsTemp["+i+"].configCode",mapId.get("configCode").toString());  // value=PUB

                    if(mapPrpDpayForPolicies.get("riskCode").equals("DZA")) {
                        nextParamsMap.put("prpCcommissionsTemp_[0].costType", mapPrpDpayForPolicies.get("costType"));  // value=2
                        nextParamsMap.put("prpCcommissionsTemp_[0].riskCode", mapPrpDpayForPolicies.get("riskCode"));  // value=DZA
                        nextParamsMap.put("prpCcommissionsTemp_[0].adjustFlag", mapPrpDpayForPolicies.get("adjustFlag"));  // value=0
                        nextParamsMap.put("prpCcommissionsTemp_[0].auditRate", "");  // value=null  test 不用赋值了
                        nextParamsMap.put("prpCcommissionsTemp_[0].sumPremium", paramsPreMap.get("prpCmainCI.sumPremium").toString());  // value=580.83
                        nextParamsMap.put("prpCcommissionsTemp_[0].costRate", mapPrpDpayForPolicies.get("costRate"));  // value=4
                        nextParamsMap.put("prpCcommissionsTemp_[0].costRateUpper", mapPrpDpayForPolicies.get("costRateUpper"));  // value=4
                        //nextParamsMap.put("prpCcommissionsTemp_[0].coinsRate","");  // value=100 参数中已带
                        nextParamsMap.put("prpCcommissionsTemp_[0].costFee",StringBaseUtils.forDight(Double.parseDouble(paramsPreMap.get("prpAnciInfo.operSellExpensesAmountCI").toString()),2));  // value=23.23
                        nextParamsMap.put("prpCcommissionsTemp_[0].agreementNo", mapId.get("agreementNo"));  // value=RULE20130000000023072
                        nextParamsMap.put("prpCcommissionsTemp_[0].configCode", mapId.get("configCode"));  // value=PUB

                    }
                }

                returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e1){
                logger.info("抓取机器人，【 PICC 核保保存3失败】");
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
        Map paramsMap = (Map) request.getRequestParam().get("Map");
        String  paramsStr = request.getRequestParam().get("String").toString();

        Map returnMap =  response.getResponseMap();
        Map nextMap =(Map) returnMap.get("nextParams");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it = key.iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            String keyValue ="";
            try{
                keyValue = nextMap.get(keyName).toString();
            }catch(Exception e){
            }
            try{
                keyName = java.net.URLEncoder.encode(keyName, "gbk");
            }catch(Exception e){
            }
            if(paramsStr.contains(keyName+"=&")){
                if(keyName.equals("levelMaxRateCi")||keyName.equals("levelMaxRate")){
                    if(keyValue.equals("null")||keyValue.equals("")){
                    }
                }else{
                    paramsStr = paramsStr.replace(keyName+"=&", keyName+"="+keyValue+"&");
                    System.out.println("savequerypayfor  post data 中已经存在参数  keyName = "+keyName +" and keyValue = "+keyValue);
                }
            }else{
                try {
                    keyValue  = java.net.URLEncoder.encode(keyValue, "gbk");
                } catch (Exception e) {
                }
                paramsStr = paramsStr+"&"+keyName+"="+keyValue;
                System.out.println("savequerypayfor  post data 中添加参数  keyName = "+keyName +" and keyValue = "+keyValue);
            }
            if(paramsMap.containsKey(keyName)){
                paramsMap.put(keyName, keyValue);
            }     else{
                paramsMap.put(keyName, keyValue);
            }
        }
        paramsStr = paramsStr+"&switchFlag=1";
        //删除多余的参数
        try{  paramsMap.get("prpCcommissionsTemp_[0].coinsRate").toString();
        }catch(Exception e){}

        if(paramsStr.contains("prpCcommissionsTemp_%5B0%5D.currency=AED&")){
            paramsStr = paramsStr.replace("prpCcommissionsTemp_%5B0%5D.currency=AED&", "") ;
        }
        if(paramsStr.contains("prpCcommissionsTemp_%5B0%5D.auditFlag=1&")){
            paramsStr = paramsStr.replace("prpCcommissionsTemp_%5B0%5D.auditFlag=1&", "") ;
        }
        if(paramsStr.contains("PayRefReason=%CA%D5%B1%A3%B7%D1&")){
            paramsStr = paramsStr.replace("PayRefReason=%CA%D5%B1%A3%B7%D1&", "") ;
        }
        if(paramsStr.contains("prpCcommissionsTemp_%5B0%5D.upperFlag=0&")){
            paramsStr = paramsStr.replace("prpCcommissionsTemp_%5B0%5D.upperFlag=0&", "") ;
        }



        requestMap.put("String",paramsStr);
        requestMap.put("Map",paramsMap);

        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }


}
