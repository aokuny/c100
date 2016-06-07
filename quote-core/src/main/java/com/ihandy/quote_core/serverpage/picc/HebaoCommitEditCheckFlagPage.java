package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoCommitEditCheckFlagPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoCommitEditCheckFlagPage.class);

    public HebaoCommitEditCheckFlagPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        String param ="";
        try{
        	String TDAA = paraMap.get("TDAA").toString();
        	String TDZA = paraMap.get("TDZA").toString();
        	if(StringUtils.isNotBlank(TDAA)){
        		param ="bizNo=" +TDAA;
        	}else{
        		param ="bizNo=" +TDZA;
        	}
        }catch(Exception e) {
            logger.info("抓取机器人，【 PICC 核保提交1获取post参数失败】");
        }
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        //System.out.print("html = "+html);
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        //解析{"totalRecords":1,"data":[{"specialflag":null,"checkFlag":"0","agentCode":"110021100065","proposalNo":"TDAA201611010000953919","specialflagConnect":null,"jfeeFlag":"1","hourFlag":"2","connectUndWrtFlag":"0","unitedSaleflagZBW":"","dmFlag":null,"contractNo":null,"nowDate":"2016-05-25","checkUpCode":"020083","operatorCode1":"020083","checkStatus":"0","inputFlag":"0","underWriteFlag":"0","checkIsNewMakeCom":"","unitedSaleflagRisk":"","strStartDate":"2016-06-26","connectSerialNo":"TDZA201611010000976366","projectCode":"","businessNature":"2  ","specialflagRisk":"","othFlag":"0"}]}
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                JSONArray jsonArray1 =  (JSONArray) map1.get("data");
                Map  dataMap = (Map) jsonArray1.get(0);
                try{
                    nextParamsMap.put("specialflag",dataMap.get("specialflag"));
                    nextParamsMap.put("checkFlag",dataMap.get("checkFlag"));
                    nextParamsMap.put("agentCode",dataMap.get("agentCode"));
                    nextParamsMap.put("proposalNo",dataMap.get("proposalNo"));
                    nextParamsMap.put("specialflagConnect",dataMap.get("specialflagConnect"));
                    nextParamsMap.put("jfeeFlag",dataMap.get("jfeeFlag"));
                    nextParamsMap.put("hourFlag",dataMap.get("hourFlag"));

                    nextParamsMap.put("connectUndWrtFlag",dataMap.get("connectUndWrtFlag"));
                    nextParamsMap.put("unitedSaleflagZBW",dataMap.get("unitedSaleflagZBW"));
                    nextParamsMap.put("dmFlag",dataMap.get("dmFlag"));
                    nextParamsMap.put("contractNo",dataMap.get("contractNo"));
                    nextParamsMap.put("nowDate",dataMap.get("nowDate"));
                    nextParamsMap.put("checkUpCode",dataMap.get("checkUpCode"));
                    nextParamsMap.put("operatorCode1",dataMap.get("operatorCode1"));

                    nextParamsMap.put("checkStatus",dataMap.get("checkStatus"));
                    nextParamsMap.put("inputFlag",dataMap.get("inputFlag"));
                    nextParamsMap.put("underWriteFlag",dataMap.get("underWriteFlag"));
                    nextParamsMap.put("checkIsNewMakeCom",dataMap.get("checkIsNewMakeCom"));
                    nextParamsMap.put("unitedSaleflagRisk",dataMap.get("unitedSaleflagRisk"));
                    nextParamsMap.put("strStartDate",dataMap.get("strStartDate"));
                    nextParamsMap.put("connectSerialNo",dataMap.get("connectSerialNo"));

                    nextParamsMap.put("projectCode",dataMap.get("projectCode"));
                    nextParamsMap.put("businessNature",dataMap.get("businessNature"));
                    nextParamsMap.put("specialflagRisk",dataMap.get("specialflagRisk"));
                    nextParamsMap.put("othFlag",dataMap.get("othFlag"));
                }catch(Exception e){
                    logger.info("抓取机器人，PICC 核保提交1 解析参数错误");
                }
                returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);

            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保提交1失败】");
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
