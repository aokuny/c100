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
    
    	
		
    
        // url = url+"?"+param;
        // String param =request.getRequestParam().get("nextParams").toString();
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
    public Response getResponse(String html, Request request) {
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
        Response response = getResponse(html, request);
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
            if(params.contains(keyName+"=&")){
            	if(keyName.equals("prpAnciInfo.discountRateBIUpAmount")){//discountRateBIUpAmountp
            		params = params.replace(keyName+"=&", keyName+"p="+keyValue+"&");
            	}
            	else if(keyName.equals("prpAnciInfo.discountRateCIUpAmount")||keyName.equals("prpAnciInfo.baseActBusiType")||keyName.equals("prpAnciInfo.baseExpBusiType")||keyName.equals("prpAnciInfo.businessCode")){
            		            //more  prpAnciInfo.discountRateCIUpAmount value=0   	          		 
            	        		//more  prpAnciInfo.baseActBusiType value=null
            	        		//more  prpAnciInfo.baseExpBusiType value=null
            	        		//more  prpAnciInfo.businessCode value=null
            	}else if(keyName.equals("prpAnciInfo.operCommRateBIUp")){
            		if(keyValue=="null"||keyValue.equals("")){
            			params = params.replace(keyName+"=&", keyName+"=0&");
            		}          		
            	}else if(keyName.equals("prpAnciInfo.actProCommRateUp")){
            		if(keyValue=="null"||keyValue.equals("")){
            			params = params.replace(keyName+"=&", keyName+"=&");
            		}          
            	}else if(keyName.equals("prpAnciInfo.standbyField1")){     
            		String  expRiskNote ="";
            		try {
						expRiskNote = URLEncoder.encode(keyValue, "gb2312");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		params = params.replace("expRiskNote=&", "expRiskNote="+expRiskNote+"&");     
            		params =  params.replace(keyName+"=&", keyName+"=23.23,0,44.88,-5.12,,A%CF%D5L%CF%D5%CA%F4%D3%DA%B8%DF%C5%E2%B8%B6%CF%D5%D6%D6,050100 050500 050600 050701 050702 050231 050912 050921 050928 050929 ,,,,050200 050911 &");
            	}
            	else{
            	   params = params.replace(keyName+"=&", keyName+"="+keyValue+"&");
            	   System.out.println("重新赋值  keyName = "+keyName +" and keyValue = "+keyValue+"\n");
            	}
            }else{
            	if(keyName.equals("prpAnciInfo.discountRateBIUpAmount")){//discountRateBIUpAmountp
           		    params = params+"&"+ keyName+"p="+keyValue;
             	}
                  // params=params+"&"+keyName+"="+keyValue;
                  //System.out.println("post data 中没有的参数  keyName = "+keyName +" and keyValue = "+keyValue+"\n");
            
                  newParamcount++;
            }
            
            if(keyName.equals("prpAnciInfo.strKindBusiTypeE")){
          	      params = params+"&kindBusiTypeE="+keyValue;
            }else if(keyName.equals("prpAnciInfo.strKindBusiTypeA")){
            	  params = params+"&kindBusiTypeA="+keyValue;
            }
        }
        System.out.println("newParamcount  count = "+newParamcount);
        compareStringDifference(right,params);
        // operCommRateCIUpAmount 赋值;
        // double  f = Double.parseDouble( nextMap.get("prpAnciInfo.operSellExpensesAmountCI").toString());
        String operCommRateCIUpAmount =nextMap.get("prpAnciInfo.operSellExpensesAmountCI").toString();
        if(!operCommRateCIUpAmount.equals("0")){
          String[] arr1 = operCommRateCIUpAmount.split("\\.");
          operCommRateCIUpAmount =  arr1[0]+"."+arr1[1].substring(0, 2);
        }
        String operateCommRateBI=nextMap.get("prpAnciInfo.operateCommRateBI").toString();
        if(!operateCommRateBI.equals("0")){
        	 String[] arr2 = operateCommRateBI.split("\\.");
        	 operateCommRateBI =  arr2[0]+"."+arr2[1].substring(0, 2);
        }
       
        params=params.replace("operCommRateCIUpAmount=&", "operCommRateCIUpAmount="+operCommRateCIUpAmount+"&");
        //operCommRateBIUpAmount 赋值;
        params=params.replace("operCommRateBIUpAmount=&", "operCommRateBIUpAmount="+operateCommRateBI+"&");
       
        
        returnMap.put("nextParams",params);
        
        System.out.println("new Params data = "+params);
        response.setResponseMap(returnMap);
        return response;
    }
    
    public void compareStringDifference(String right,String error){
        String[] paramArr1=right.split("&");
        String[] paramArr2=error.split("&");
        List<String> list = new LinkedList<String>();
        List<String> list1 = new LinkedList<String>();
        for(int j=0;j<paramArr2.length;j++){
        	list.add(paramArr2[j].split("=")[0]);
        	try{
        	list1.add(paramArr2[j].split("=")[1]);
        	}catch(Exception e){
        	list1.add(null);
        	}
        	}
    
        for(int i=0;i<paramArr1.length;i++){
            paramArr1[i].split("=");
            String[] key = {};   //创建空数组
            key = (String[]) list.toArray(key);
            String[] value = {};   //创建空数组
            value = (String[]) list1.toArray(value);
            if(error.contains(paramArr1[i].split("=")[0])){//包含
            	
                for(int j=0;j<key.length;j++){
               
                    if(key[j].equals(paramArr1[i].split("=")[0])){
                        String value1 ="";
                        String value2="";
                        try{
                            value1 = paramArr1[i].split("=")[1];
                            try{
                                value2 = value[j];
                                if(!value1.equals(value2)){                              
                                        System.out.println("key="+paramArr1[i].split("=")[0]+"   value1 =  "+value1+"       value2 =  "+value2);                                     
                                }
                                list.remove(key[j]); 
                                list1.remove(value[j]);   
                               break;
                            }catch(Exception e2){
                                System.out.println("key="+paramArr1[i].split("=")[0]+"   value1 = "+value1+"  value2 =null");
                                list.remove(key[j]); 
                                list1.remove(value[j]);  
                                break;
                            }
                        
                        }catch(Exception e){
                            try{
                                value2 = paramArr2[j].split("=")[1];
                                System.out.println("key="+paramArr1[i].split("=")[0]+"   value1 = null   value2 ="+value2);
                                list.remove(key[j]); 
                                list1.remove(value[j]);  
                                break;
                            }catch(Exception e2){
                                list.remove(key[j]); 
                                list1.remove(value[j]);  
                                // System.out.println("key="+paramArr1[i].split("=")[0]+"   value1 = null   value2 =null");
                                break;
                            }
                        }
                     
                    }//if end
                    
                    

                }//for end
            }else{
                try{
                    System.out.println("no  "+paramArr1[i].split("=")[0] +" value="+paramArr1[i].split("=")[1]);
                }catch(Exception e){
                    System.out.println("no  "+paramArr1[i].split("=")[0] +" value=null");
                }

            }//else end
            
          
        }//for end
        System.out.println("key  list length = "+list.size());
        System.out.println("value list length = "+list1.size());

    }
}
