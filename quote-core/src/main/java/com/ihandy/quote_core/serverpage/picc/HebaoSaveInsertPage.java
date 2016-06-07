package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveInsertPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveInsertPage.class);

    public HebaoSaveInsertPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap =(Map) request.getRequestParam().get("Map");
        String param ="";
        try{
            param = request.getRequestParam().get("String").toString();
            param = param.replace("[", "%5B");
            param = param.replace("]", "%5D");
            System.out.println("ErrorParam = "+param);
        }catch(Exception e) {
            logger.info("抓取机器人，【 PICC 核保保存6获取post参数失败】");
        }
        // System.out.println("rightParam = "+rightParam);
        //StringBaseUtils.compareErrorStringLess(rightParam,param);
        //StringBaseUtils.compareErrorStringMore(rightParam,param);
        // String newParams = StringBaseUtils.combineStringByRightOrder(rightParam,param);
        //prpAnciInfo.profitRateBIUp=, prpAnciInfo.discountRateBIUpAmountp=, kindBusiTypeA=, kindBusiTypeE=, prpCcommissionsTemp_%5B0%5D.coinsRate=
        //prpAnciInfo.profitRateBIUp=

        //param =  compareStringDifference(rightParam,param);
        // compareStringDifference1(rightParam,param);
        // compareStringDifference2(rightParam,param);
        
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html,Request request) {
        //解析TDAA201611010000936876,TDZA201611010000959123
    	System.err.println("-----------" + html);
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
            	if(html.contains(",")){
                    String[] noArr = html.split(",");
                    nextParamsMap.put("TDAA",noArr[0]);//投保单号
                    nextParamsMap.put("TDZA",noArr[1]);//关联单号
            	}else{
            		if(html.contains("TDAA")){
            			nextParamsMap.put("TDAA",html);
            		}else{
            			 nextParamsMap.put("TDZA",html);
            		}
            	}
            	returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存6失败】");
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
        return response;
    }


}
