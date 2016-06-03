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
import java.util.*;

/**
 * Created by fengwen on 2016/5/23.
 */
public class HebaoCalAnciInfoPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoCalAnciInfoPage.class);

    public HebaoCalAnciInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= null;
        String url = request.getUrl();
        String param =  request.getRequestParam().get("nextParams").toString();
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }
    /**************** 解析json 字符串
     * {
     "totalRecords": 1,
     "data": [
     {
     "discountRateBI": 31.1499,
     "origBusiType": "B",
     "averProfitRate": 5,
     "busiTypeCommBIUp": 0,
     "operSellExpensesAmountBI": 378.6482,
     "sellExpensesRateCIUp": 4,
     "discountRateCIUp": 30,
     "sellExpensesAmount": 131.4184,
     "discountRateBIAmount": 1223.6584,
     "operatePayRate": 0.3829,
     "actProCommRate": 50,
     "operateProfitRate": 67.3486,
     "operCommRateBIUp": null,
     "expProCommRateUp": 56.15,
     "profitRateBIUp": 31.15,
     "operateCommCI": 0,
     "standbyField1": "A险L险属于高赔付险种",
     "discountRateUpAmount": 2439.145,
     "sellExpensesCIUpAmount": 23.2332,
     "discountRateCIAmount": 369.17,
     "operSellExpensesRateBI": 14,
     "discountRateUp": 50,
     "sumPremium": 3285.46,
     "minNetSumPremium": 1865.3557,
     "breakEvenValue": 0.01,
     "operCommRate": 0,
     "operCommRateAmount": 0,
     "discountRateCI": 38.86,
     "busiRiskRate": 81.4679,
     "baseActBusiType": null,
     "anciIndiConfQueryVoList": [],
     "standPayRate": 0.2579,
     "busiTypeCommCIUp": 0,
     "busiBalanRate": 76.9879,
     "operSellExpensesRateCI": 4,
     "discountRateAmount": 1592.83,
     "operSellExpensesAmount": 401.8814,
     "sumPremiumCI": 580.83,
     "discountRate": 32.6514,
     "operSellExpensesRate": 12.2321,
     "sellExpensesBIUpAmount": 378.6482,
     "operateCommRateCI": 0,
     "strKindBusiTypeC": "",
     "businessCode": null,
     "strKindBusiTypeB": "",
     "strKindBusiTypeA": "050100 050500 050600 050701 050702 050231 050912 050921 050928 050929 ",
     "busiStandardBalanRate": 62.02,
     "discountRateBIUpAmount": 1223.6623,
     "strKindBusiTypeE": "050200 050911 ",
     "operCommRateCIUp": 4,
     "strKindBusiTypeD": "",
     "minNetSumPremiumBI": 1504.5429,
     "baseExpBusiType": null,
     "actBusiType": "A",
     "sellExpensesRate": 4,
     "sumPremiumBI": 2704.63,
     "operSellExpensesAmountCI": 23.2332,
     "operateCommRateBI": 0,
     "discountRateCIUpAmount": 285,
     "operateCommBI": 0,
     "actProCommRateUp": null,
     "averageRate": 10.78,
     "sellExpensesRateBIUp": 14,
     "minNetSumPremiumCI": 741.4997,
     "proCommRateBIUp": 46.15,
     "expBusiType": "A"
     }
     ]
     }* **************
     */
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
                JSONArray jsonArray2 = (JSONArray) map1.get("data");
                Map dataMap = (Map) jsonArray2.get(0);
                Set<String> key2 = dataMap.keySet();
                for (Iterator it2 = key2.iterator(); it2.hasNext();) {
                    String keyName2 = (String) it2.next();
                    String keyValue2 = dataMap.get(keyName2).toString();
                    String keyName3 = "prpAnciInfo."+keyName2;
                    nextParamsMap.put(keyName3,keyValue2);
                }
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保计算辅助核保失败】");
            }
            returnMap.put("nextParams",nextParamsMap);
            response.setResponseMap(returnMap);
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
        Response response = getResponse(html,request);
        //上个请求返回的参数继续传递下去
        Map requestMap = request.getRequestParam();
        Map returnMap =  response.getResponseMap();

        String params = requestMap.get("nextParams").toString();
        String right =params;
        //compareStringDifference(right,params);
        System.out.println("params1 = "+params);
        Map nextMap =(Map) returnMap.get("nextParams");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数字符串中
        int newParamcount = 0;

        for (Iterator it2 = key.iterator(); it2.hasNext();) {
            String keyName = (String) it2.next();
            String keyValue = nextMap.get(keyName).toString();
            if(params.contains(keyName+"=&")){//参数字符串中含有这些辅助计算参数
                if(keyName.equals("prpAnciInfo.operateCommCI")||keyName.equals("prpAnciInfo.discountRateBIUpAmountp")||keyName.equals("prpAnciInfo.expProCommRateUp")||keyName.equals("prpAnciInfo.discountRateCIUpAmount")||keyName.equals("prpAnciInfo.baseActBusiType")||keyName.equals("prpAnciInfo.baseExpBusiType")||keyName.equals("prpAnciInfo.businessCode")){
                    //后面请求参数没有这些信息或修改了这些信息，不添加进去
                    //more  prpAnciInfo.discountRateCIUpAmount value=0
                    //more  prpAnciInfo.baseActBusiType value=null
                    //more  prpAnciInfo.baseExpBusiType value=null
                    //more  prpAnciInfo.businessCode value=null
                    System.out.println("不需要修改的参数"+keyName);
                }else if(keyName.equals("prpAnciInfo.operCommRateBIUp")){

                    if(keyValue=="null"||keyValue.equals("")){
                        params = params.replace(keyName+"=&", keyName+"=0&");
                        //  nextMap.put("prpAnciInfo.operCommRateBIUp","0");
                    }
                }else if(keyName.equals("prpAnciInfo.actProCommRateUp")){
                    if(keyValue=="null"||keyValue.equals("")){
                        params = params.replace(keyName+"=&", keyName+"=&");
                        // nextMap.put("prpAnciInfo.actProCommRateUp","");
                    }
                }else if(keyName.equals("prpAnciInfo.standbyField1")){
                    //TODO 2016/5/30   删除了expRiskNote 和 prpAnciInfo.standbyField1(不影响)
                    String  expRiskNote ="";
                    try {
                        expRiskNote = java.net.URLEncoder.encode(keyValue, "gb2312");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    params = params.replace("expRiskNote=&", "expRiskNote="+expRiskNote+"&");
                    //  nextMap.put("expRiskNote",expRiskNote);
                    String value = "23.23,0,44.88,-5.12,,A%CF%D5L%CF%D5%CA%F4%D3%DA%B8%DF%C5%E2%B8%B6%CF%D5%D6%D6,050100 050500 050600 050701 050702 050231 050912 050921 050928 050929 ,,,,050200 050911";
                    params =  params.replace(keyName+"=&", keyName+"="+value+"&");
                    // nextMap.put("prpAnciInfo.standbyField1",value);
                }
                else if(keyName.equals("prpAnciInfo.profitRateBIUp")){
                    params = params.replace(keyName+"=&", keyName+"="+keyValue+"&");
                    //nextMap.put(keyName,keyValue);
                    if(!params.contains("prpAnciInfo.breakEvenValue=&")){
                        params = params.replace("prpAnciInfo.breakEvenValue="+nextMap.get("prpAnciInfo.breakEvenValue").toString()+"&prpAnciInfo.profitRateBIUp="+keyValue+"&", "prpAnciInfo.breakEvenValue="+nextMap.get("prpAnciInfo.breakEvenValue").toString()+"&prpAnciInfo.profitRateBIUp=&");
                    }else{
                        params = params.replace("prpAnciInfo.breakEvenValue=&prpAnciInfo.profitRateBIUp="+keyValue+"&", "prpAnciInfo.breakEvenValue=&prpAnciInfo.profitRateBIUp=&");
                    }
                    // nextMap.put("prpAnciInfo.profitRateBIUp",keyValue);
                }
                else{
                    params = params.replace(keyName+"=&", keyName+"="+keyValue+"&");

                    // nextMap.put(keyName,keyValue);
                    System.out.println("重新赋值  keyName = "+keyName +" and keyValue = "+keyValue+"\n");
                }
            }else{//参数字符串中不包含有这些辅助计算参数
                if(keyName.equals("prpAnciInfo.discountRateBIUpAmount")){//discountRateBIUpAmountp
                    params = params.replace("prpAnciInfo.discountRateBIUpAmountp=",keyName+"p="+keyValue);
                }else  if(keyName.equals("prpAnciInfo.strKindBusiTypeE")){
                    params = params.replace("kindBusiTypeE=","kindBusiTypeE="+keyValue);
                    // nextMap.put("kindBusiTypeE",keyValue);
                }else if(keyName.equals("prpAnciInfo.strKindBusiTypeA")){
                    params = params.replace("kindBusiTypeA=","kindBusiTypeA="+keyValue);
                    // nextMap.put("kindBusiTypeA",keyValue);
                }else if(keyName.equals("prpAnciInfo.actProCommRate")){
                    //expProCommRateUp赋值  取actProCommRate  50 不是 56.15
                    params = params.replace("prpAnciInfo.expProCommRateUp=&","prpAnciInfo.expProCommRateUp="+keyValue+"&");
                    // nextMap.put("prpAnciInfo.expProCommRateUp",keyValue);
                }
                //System.out.println("post data 中没有的参数  keyName = "+keyName +" and keyValue = "+keyValue+"\n");
                newParamcount++;
            }

        }
        //actProfitRate赋值 actProfitRate = discountRate+operSellExpenseRate;
        if(params.contains("&actProfitRate=&")){
            String actProfitRate ="";
            double discountRate = 0;
            double operSellExpenseRate = 0;
            try {
                discountRate = Double.parseDouble(nextMap.get("prpAnciInfo.discountRate").toString());
                operSellExpenseRate = Double.parseDouble(nextMap.get("prpAnciInfo.operSellExpensesRate").toString());
            }catch (Exception e){
            }
            actProfitRate =  StringBaseUtils.forDight(discountRate+operSellExpenseRate,2)+"";
            params = params.replace("&actProfitRate=&", "&actProfitRate="+actProfitRate+"&");
            nextMap.put("actProfitRate",actProfitRate);
            //actProfitRate=44.88
        }
        if(params.contains("&expProCommRateUp_Disc=&")){//expProCommRateUp_Disc   -5.12
            double actProfitRate =0;
            double discountRate = 0;
            double operSellExpenseRate = 0;
            double expProCommRateUp_Disc=0;
            try {
                discountRate = Double.parseDouble(nextMap.get("prpAnciInfo.discountRate").toString());
                operSellExpenseRate = Double.parseDouble(nextMap.get("prpAnciInfo.operSellExpensesRate").toString());
            }catch (Exception e){
            }
            actProfitRate =  StringBaseUtils.forDight(discountRate+operSellExpenseRate,2);
            expProCommRateUp_Disc = actProfitRate - Double.parseDouble(nextMap.get("prpAnciInfo.actProCommRate").toString());
            params =params.replace("&expProCommRateUp_Disc=&", "&expProCommRateUp_Disc="+ StringBaseUtils.forDight(expProCommRateUp_Disc,2)+"&");//actProfitRate=44.88
            nextMap.put("expProCommRateUp_Disc",StringBaseUtils.forDight(expProCommRateUp_Disc,2));
        }
        if(params.contains("&operCommRateCIUpAmount=&")) {//operCommRateCIUpAmount   23.23
            double operCommRateCIUpAmount = Double.parseDouble(nextMap.get("prpAnciInfo.operSellExpensesAmountCI").toString());
            params = params.replace("operCommRateCIUpAmount=&", "operCommRateCIUpAmount="+ StringBaseUtils.forDight(operCommRateCIUpAmount, 2) + "&");
            nextMap.put("operCommRateCIUpAmount",StringBaseUtils.forDight(operCommRateCIUpAmount,2));

        }
        if(params.contains("&operCommRateBIUpAmount=&")){//operCommRateBIUpAmount 0  operCommRateBIUpAmount 赋值;
            Double operateCommRateBI= Double.parseDouble(nextMap.get("prpAnciInfo.operateCommRateBI").toString());
            if(StringBaseUtils.forDight(operateCommRateBI,2)==0){
                nextMap.put("operCommRateBIUpAmount","0");
                params=params.replace("operCommRateBIUpAmount=&", "operCommRateBIUpAmount=0&");

            }else{
                nextMap.put("operCommRateBIUpAmount",StringBaseUtils.forDight(operateCommRateBI,2));
                params=params.replace("operCommRateBIUpAmount=&", "operCommRateBIUpAmount="+StringBaseUtils.forDight(operateCommRateBI,2)+"&");

            }
        }
        if(params.contains("&prpAnciInfo.operateCommCI=&")) {//operateCommCI 23.23
            double operateCommCI = Double.parseDouble(nextMap.get("prpAnciInfo.operSellExpensesAmountCI").toString());
            params = params.replace("prpAnciInfo.operateCommCI=&", "prpAnciInfo.operateCommCI="+ StringBaseUtils.forDight(operateCommCI, 2) + "&");
            nextMap.put("prpAnciInfo.operateCommCI",StringBaseUtils.forDight(operateCommCI,2));

        }
        if(params.contains("&prpAnciInfo.operateCommRateCI=0&")) {//取 operCommRateCIUp 4
            params =params.replace("&prpAnciInfo.operateCommRateCI=0&", "&prpAnciInfo.operateCommRateCI="+nextMap.get("prpAnciInfo.operCommRateCIUp")+"&");
            nextMap.put("prpAnciInfo.operateCommRateCI",nextMap.get("prpAnciInfo.operCommRateCIUp"));

        }
        if(params.contains("&prpAnciInfo.operateCommRateBI=0&")){// 56.15-31.15 = 25  expProCommRateUp-profitRateBIUp
            double operateCommRateBI=0;
            double operateCommBI = 0;
            operateCommRateBI =  Double.parseDouble( nextMap.get("prpAnciInfo.expProCommRateUp").toString())- Double.parseDouble( nextMap.get("prpAnciInfo.profitRateBIUp").toString());
            params =params.replace("&prpAnciInfo.operateCommRateBI=0&", "&prpAnciInfo.operateCommRateBI="+operateCommRateBI+"&");//
            nextMap.put("prpAnciInfo.operateCommRateBI",operateCommRateBI);
            operateCommBI = StringBaseUtils.forDight(Double.parseDouble( nextMap.get("prpAnciInfo.sumPremiumBI").toString())*operateCommRateBI/100,2);
            if(params.contains("&prpAnciInfo.operateCommBI=0&")) {//取 operateCommRateBI 676.16 = 2704.63*25/100
                params =params.replace("&prpAnciInfo.operateCommBI=0&", "&prpAnciInfo.operateCommBI="+operateCommBI+"&");
                nextMap.put("prpAnciInfo.operateCommBI",operateCommBI);
            }
            if(params.contains("&prpAnciInfo.operCommRateAmount=0&")){// 699.39 = 676.16+23.23
                double operCommRateAmount=0;
                double operCommRate=0;
                double sumPremium=Double.parseDouble( nextMap.get("prpAnciInfo.sumPremium").toString());
                operCommRateAmount  = StringBaseUtils.forDight(operateCommBI + Double.parseDouble( nextMap.get("prpAnciInfo.operSellExpensesAmountCI").toString()),2);
                params =params.replace("&prpAnciInfo.operCommRateAmount=0&", "&prpAnciInfo.operCommRateAmount="+operCommRateAmount+"&");//
                nextMap.put("prpAnciInfo.operCommRateAmount",operCommRateAmount);
                operCommRate = StringBaseUtils.forDight(operCommRateAmount/sumPremium*100,3);
                if(params.contains("&prpAnciInfo.operCommRate=0&")) {//取 operateCommRateBI 21.287 = 3285.46/699.39*100
                    params =params.replace("&prpAnciInfo.operCommRate=0&", "&prpAnciInfo.operCommRate="+operCommRate+"&");
                    nextMap.put("prpAnciInfo.operCommRate",operCommRate);
                }
            }
        }
        // System.out.println("params = "+params);
        returnMap.put("nextParams",params);
        response.setResponseMap(returnMap);
        return response;
    }


}
